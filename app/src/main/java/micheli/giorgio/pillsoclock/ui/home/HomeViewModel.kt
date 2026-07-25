package micheli.giorgio.pillsoclock.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private const val FINESTRA_ASSUNZIONE_MINUTI = 5L
private const val INTERVALLO_TICK_MS = 15_000L

data class HomeUiState(
    val prossimaAssunzione: AssunzioneGiornaliera? = null,
    val puoAssumereOra: Boolean = false,
    val prossimaInRitardo: AssunzioneGiornaliera? = null,
    val minutiAllaProssima: Long? = null,
    val minutiRitardo: Long? = null,
    val inRitardo: List<AssunzioneGiornaliera> = emptyList(),
    val prossimeAssunzioni: List<AssunzioneGiornaliera> = emptyList(),
    val esisteAlmenoUnMedicinale: Boolean = false,
    val assunteOggi: Int = 0,
    val totaliOggi: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val medicinaliRepository: MedicinaleRepository,
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository,
    private val promemoriaRepository: PromemoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        osservaAssunzioniDiOggi()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun osservaAssunzioniDiOggi() {
        viewModelScope.launch(Dispatchers.IO) {
            utenteRepository.getUtente()
                .flatMapLatest { utente ->
                    if (utente == null) {
                        Log.d("osservaAssunzioniDiOggi", "Utente non trovato")
                        flowOf(HomeUiState(isLoading = true))
                    } else {
                        combine(
                            assunzioneRepository.getAssunzioniGiornaliere(utente.id, LocalDate.now()),
                            ticker(),
                            medicinaliRepository.checkIfAMedicinaleExist(utente.id)
                        ) { assunzioni, now, exist->
                            costruisciUiState(assunzioni, now, exist)
                        }
                    }
                }
                .catch { e ->
                    Log.d("osservaAssunzioniDiOggi", e.message ?: "Eccezione")
                    emit(HomeUiState(isLoading = false, errorMessage = e.message))
                }
                .collect { nuovoStato ->
                    // Il ticker ricalcola lo stato ogni 15s anche quando nulla è
                    // realmente cambiato (es. tra un boundary di minuto e l'altro):
                    // evitare di riassegnare un HomeUiState uguale evita di far
                    // ricomporre inutilmente l'intera LazyColumn della home.
                    if (_uiState.value != nuovoStato) {
                        _uiState.value = nuovoStato
                    }
                }
        }
    }

    /**
     * Emette il tempo corrente ogni [INTERVALLO_TICK_MS] ms, così lo stato
     * (in ritardo / nella finestra / futura) si ricalcola anche se la lista
     * di assunzioni sottostante non cambia.
     */
    private fun ticker() = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(INTERVALLO_TICK_MS)
        }
    }

    private fun costruisciUiState(
        assunzioni: List<AssunzioneGiornaliera>,
        now: LocalDateTime,
        medicinaleExist: Boolean
    ): HomeUiState {
        val inAttesa = assunzioni.filter {
            it.assunzionePrevista.stato == StatoAssunzione.IN_ATTESA
        }

        val (inRitardo, restanti) = inAttesa.partition {
            èInRitardo(it.assunzionePrevista.orarioPrevisto, now)
        }

        val restantiOrdinate = restanti.sortedBy { it.assunzionePrevista.orarioPrevisto }
        val inRitardoOrdinate = inRitardo.sortedBy { it.assunzionePrevista.orarioPrevisto }

        val prossima = restantiOrdinate.firstOrNull()
        val prossimaInRitardo = inRitardoOrdinate.firstOrNull()

        val inCoda = restantiOrdinate.drop(1)
        // Il riquadro principale mostra prossimaInRitardo solo quando non c'è una
        // prossima assunzione futura (vedi soloAssunzioniInRitardoRimaste in HomeScreen).
        // Se invece prossima != null il riquadro mostra quella, quindi prossimaInRitardo
        // non va scartato qui: andrebbe altrimenti perso senza comparire da nessuna parte.
        val restantiInRitardo = if (prossima == null) inRitardoOrdinate.drop(1) else inRitardoOrdinate

        val minutiAllaProssima = prossima?.let {
            ChronoUnit.MINUTES.between(now, now.toLocalDate().atTime(it.assunzionePrevista.orarioPrevisto))
        }?.coerceAtLeast(0)
        val minutiRitardo = prossimaInRitardo?.let {
            ChronoUnit.MINUTES.between(now.toLocalDate().atTime(it.assunzionePrevista.orarioPrevisto), now)
        }?.coerceAtLeast(0)

        return HomeUiState(
            prossimaAssunzione = prossima,
            prossimaInRitardo = prossimaInRitardo,
            minutiAllaProssima = minutiAllaProssima,
            minutiRitardo = minutiRitardo,
            puoAssumereOra = prossima?.let {
                èNellaFinestra(it.assunzionePrevista.orarioPrevisto, now)
            } ?: false,
            inRitardo = restantiInRitardo,
            prossimeAssunzioni = inCoda,
            assunteOggi = assunzioni.size - inAttesa.size,
            totaliOggi = assunzioni.size,
            esisteAlmenoUnMedicinale = medicinaleExist,
            isLoading = false
        )
    }

    // orarioPrevisto è sempre riferito a "oggi" (le AssunzionePrevista sono generate
    // con data = LocalDate.now()), quindi lo ancoriamo alla data di `now` e confrontiamo
    // LocalDateTime pieni: a differenza di LocalTime, plusMinutes/minusMinutes qui
    // attraversa correttamente la mezzanotte invece di avvolgere ciclicamente il quadrante.
    private fun èInRitardo(orarioPrevisto: LocalTime, now: LocalDateTime): Boolean {
        val previsto = now.toLocalDate().atTime(orarioPrevisto)
        return previsto.plusMinutes(FINESTRA_ASSUNZIONE_MINUTI).isBefore(now)
    }

    private fun èNellaFinestra(orarioPrevisto: LocalTime, now: LocalDateTime): Boolean {
        val previsto = now.toLocalDate().atTime(orarioPrevisto)
        val inizioFinestra = previsto.minusMinutes(FINESTRA_ASSUNZIONE_MINUTI)
        val fineFinestra = previsto.plusMinutes(FINESTRA_ASSUNZIONE_MINUTI)
        return !now.isBefore(inizioFinestra) && !now.isAfter(fineFinestra)
    }

    fun onAssumiClick(assunzionePrevista: AssunzionePrevista) {
        viewModelScope.launch {
            // firstOrNull() ritorna il primo elemento del flow o altrimenti null nel caso il flow sia vuoto
            val utente = utenteRepository.getUtente().firstOrNull() ?: return@launch
            assunzioneRepository.registraAssunzione(assunzionePrevista, utente.id)
            promemoriaRepository.cancella(assunzionePrevista.id)
        }
    }

    // Per le dosi "in ritardo" prese puntualmente ma confermate tardi sull'app:
    // registra l'assunzione con l'orario previsto invece di quello del click.
    fun onAssumiPuntualeClick(assunzionePrevista: AssunzionePrevista) {
        viewModelScope.launch {
            val utente = utenteRepository.getUtente().firstOrNull() ?: return@launch
            val timestamp = assunzionePrevista.data.atTime(assunzionePrevista.orarioPrevisto)
            assunzioneRepository.registraAssunzione(assunzionePrevista, utente.id, timestamp)
            promemoriaRepository.cancella(assunzionePrevista.id)
        }
    }

    fun onAnnullaClick(assunzioneGiornaliera: AssunzioneGiornaliera) {
        viewModelScope.launch {
            val assunzionePrevista = assunzioneGiornaliera.assunzionePrevista
            assunzioneRepository.annullaAssunzione(assunzionePrevista)
            // Se l'orario previsto è già passato (dose in ritardo annullata), pianifica()
            // se ne accorge da sola e non fa scattare un allarme immediato per il passato.
            promemoriaRepository.pianifica(
                assunzionePrevista.id,
                assunzionePrevista.data,
                assunzionePrevista.orarioPrevisto,
                assunzioneGiornaliera.nomeMedicinale,
                assunzioneGiornaliera.dosaggio
            )
        }
    }
}