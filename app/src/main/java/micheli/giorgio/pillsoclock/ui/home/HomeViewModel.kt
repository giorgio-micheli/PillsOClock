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
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val FINESTRA_ASSUNZIONE_MINUTI = 5L
private const val INTERVALLO_TICK_MS = 15_000L

data class HomeUiState(
    val prossimaAssunzione: AssunzioneGiornaliera? = null,
    val puoAssumereOra: Boolean = false,
    val prossimaInRitardo: AssunzioneGiornaliera? = null,
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
    private val utenteRepository: UtenteRepository
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
                    _uiState.value = nuovoStato
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
        val oraCorrente = now.toLocalTime()

        val inAttesa = assunzioni.filter {
            it.assunzionePrevista.stato == StatoAssunzione.IN_ATTESA
        }

        val (inRitardo, restanti) = inAttesa.partition {
            èInRitardo(it.assunzionePrevista.orarioPrevisto, oraCorrente)
        }

        val restantiOrdinate = restanti.sortedBy { it.assunzionePrevista.orarioPrevisto }
        val inRitardoOrdinate = inRitardo.sortedBy { it.assunzionePrevista.orarioPrevisto }

        val prossima = restantiOrdinate.firstOrNull()
        val prossimaInRitardo = inRitardoOrdinate.firstOrNull()

        val inCoda = restantiOrdinate.drop(1)
        val restantiInRitardo = inRitardoOrdinate.drop(1)

        return HomeUiState(
            prossimaAssunzione = prossima,
            prossimaInRitardo = prossimaInRitardo,
            puoAssumereOra = prossima?.let {
                èNellaFinestra(it.assunzionePrevista.orarioPrevisto, oraCorrente)
            } ?: false,
            inRitardo = restantiInRitardo,
            prossimeAssunzioni = inCoda,
            assunteOggi = assunzioni.size - inAttesa.size,
            totaliOggi = assunzioni.size,
            esisteAlmenoUnMedicinale = medicinaleExist,
            isLoading = false
        )
    }

    private fun èInRitardo(orarioPrevisto: LocalTime, oraCorrente: LocalTime): Boolean =
        orarioPrevisto.plusMinutes(FINESTRA_ASSUNZIONE_MINUTI).isBefore(oraCorrente)

    private fun èNellaFinestra(orarioPrevisto: LocalTime, oraCorrente: LocalTime): Boolean {
        val inizioFinestra = orarioPrevisto.minusMinutes(FINESTRA_ASSUNZIONE_MINUTI)
        val fineFinestra = orarioPrevisto.plusMinutes(FINESTRA_ASSUNZIONE_MINUTI)
        return !oraCorrente.isBefore(inizioFinestra) && !oraCorrente.isAfter(fineFinestra)
    }

    fun onAssumiClick(assunzionePrevista: AssunzionePrevista) {
        viewModelScope.launch {
            // firstOrNull() ritorna il primo elemento del flow o altrimenti null nel caso il flow sia vuoto
            val utente = utenteRepository.getUtente().firstOrNull() ?: return@launch
            assunzioneRepository.registraAssunzione(assunzionePrevista, utente.id)
        }
    }

    fun onAnnullaClick(assunzionePrevista: AssunzionePrevista) {
        viewModelScope.launch {
            assunzioneRepository.annullaAssunzione(assunzionePrevista)
        }
    }
}