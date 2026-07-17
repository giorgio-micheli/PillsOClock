package micheli.giorgio.pillsoclock.ui.addMedicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.data.local.entity.TipoFrequenza
import micheli.giorgio.pillsoclock.domain.model.Medicinale
import micheli.giorgio.pillsoclock.domain.model.OrarioAssunzione
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate
import java.time.LocalTime

data class AggiungiMedicinaleUiState(
    val nome: String = "",
    val dosaggio: String = "",
    val note: String = "",
    val orari: List<LocalTime> = emptyList(),
    val tipoFrequenza: TipoFrequenza = TipoFrequenza.GIORNALIERA,
    val intervalloGiorni: Int = 2,
    val giorniSettimana: List<Int> = emptyList(),
    val dataInizio: LocalDate = LocalDate.now(),
    val dataFine: LocalDate? = null,
    val isLoading: Boolean = false,
    val isModifica: Boolean = false,
    val errorMessage: String? = null,
    val salvatagioCompletato: Boolean = false,
    // errori di validazione per singolo campo
    val nomeError: Boolean = false,
    val orariError: Boolean = false,
    val giorniSettimanaError: Boolean = false,
    val dataError: Boolean = false
)

class AddMedicinaleViewModel(
    val medicinaleRepository: MedicinaleRepository,
    val utenteRepository: UtenteRepository,
    val assunzioneRepository: AssunzioneRepository,
    private val idMedicinale: Int? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AggiungiMedicinaleUiState(isModifica = idMedicinale != null))
    val uiState = _uiState.asStateFlow()

    // Dati del medicinale in modifica non esposti alla UI ma necessari per l'update:
    // idUtente e attivo non sono modificabili dal bottom sheet (attivo si gestisce
    // dalla lista con lo switch), ma vanno preservati quando si salva.
    private var idPianoInModifica: Int? = null
    private var idUtenteInModifica: Int? = null
    private var attivoInModifica: Boolean = true

    init {
        if (idMedicinale != null) {
            caricaMedicinaleEsistente(idMedicinale)
        }
    }

    private fun caricaMedicinaleEsistente(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val medicinaleConPiano = medicinaleRepository.getMedicinaleConPianoEOrari(id).firstOrNull()
            if (medicinaleConPiano == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Medicinale non trovato") }
                return@launch
            }

            val medicinale = medicinaleConPiano.medicinale
            val pianoConOrari = medicinaleConPiano.piani.firstOrNull()

            idUtenteInModifica = medicinale.idUtente
            attivoInModifica = medicinale.attivo
            idPianoInModifica = pianoConOrari?.piano?.id

            _uiState.update {
                it.copy(
                    nome = medicinale.nome,
                    dosaggio = medicinale.dosaggio ?: "",
                    note = medicinale.note ?: "",
                    orari = pianoConOrari?.orari?.map { orario -> orario.orario } ?: emptyList(),
                    tipoFrequenza = pianoConOrari?.piano?.tipoFrequenza ?: TipoFrequenza.GIORNALIERA,
                    intervalloGiorni = pianoConOrari?.piano?.intervalloGiorni ?: 2,
                    giorniSettimana = pianoConOrari?.piano?.giorniSettimana ?: emptyList(),
                    dataInizio = medicinale.dataInizio,
                    dataFine = medicinale.dataFine,
                    isLoading = false
                )
            }
        }
    }

    fun onNomeChange(value: String) {
        if (value.isNotBlank()) _uiState.update { it.copy(nome = value, nomeError = false) }
        else _uiState.update { it.copy(nome = value, nomeError = true) }
    }

    fun onDosaggioChange(value: String) {
        _uiState.update { it.copy(dosaggio = value) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onOrarioAggiunto(orario: LocalTime) {
        // Se l'orario non è già presente
        if (!_uiState.value.orari.contains(orario)) {
            _uiState.update { it.copy(orari = it.orari + orario, orariError = false) }
        }
    }

    fun onOrarioRimosso(orario: LocalTime) {
        _uiState.update { it.copy(orari = it.orari - orario) }
    }

    fun onTipoFrequenzaChange(tipo: TipoFrequenza) {
        _uiState.update { it.copy(tipoFrequenza = tipo) }
    }

    fun onIntervalloGiorniChange(valore: Int) {
        _uiState.update { it.copy(intervalloGiorni = valore) }
    }

    fun onGiornoSettimanaToggle(giorno: Int) {
        val giorniAttuali = _uiState.value.giorniSettimana
        // Se clicco un giorno già selezionato, lo rimuovo, altrimenti lo aggiungo
        val nuoviGiorni = if (giorniAttuali.contains(giorno)) {
            giorniAttuali - giorno
        } else {
            giorniAttuali + giorno
        }
        _uiState.update { it.copy(giorniSettimana = nuoviGiorni, giorniSettimanaError = false) }
    }

    fun onDataInizioChange(data: LocalDate) {
        _uiState.update { it.copy(dataInizio = data) }
    }

    fun onDataFineChange(data: LocalDate?) {
        if (data == null) {
            _uiState.update { it.copy(dataFine = data, dataError = false) }
        } else _uiState.update { it.copy(dataFine = data) }
    }

    fun onSave(): Boolean {

        if(!valida()) return false

        viewModelScope.launch(Dispatchers.IO) {
            // Notifico la ui che è partita un'operazione asincrona attivando il flag di loading
            _uiState.update { it.copy(isLoading = true) }

            try {
                val stato = _uiState.value
                // Creo la lista di orari utilizzando quelli presenti nello stato del viewModel
                val orari = stato.orari.map { orario ->
                    OrarioAssunzione(
                        id = 0,
                        idPianoAssunzione = 0,
                        orario = orario
                    )
                }

                if (idMedicinale != null) {
                    // Modalità modifica: aggiorno il medicinale e il piano esistenti,
                    // preservando idUtente e attivo (non gestiti da questo bottom sheet).
                    val medicinale = Medicinale(
                        id = idMedicinale,
                        idUtente = idUtenteInModifica ?: return@launch,
                        nome = stato.nome.trim(),
                        dosaggio = stato.dosaggio.trim().ifEmpty { null },
                        note = stato.note.trim().ifEmpty { null },
                        attivo = attivoInModifica,
                        dataInizio = stato.dataInizio,
                        dataFine = stato.dataFine
                    )
                    val piano = PianoAssunzione(
                        id = idPianoInModifica ?: return@launch,
                        idMedicinale = idMedicinale,
                        tipoFrequenza = stato.tipoFrequenza,
                        intervalloGiorni = if (stato.tipoFrequenza == TipoFrequenza.OGNI_N_GIORNI) stato.intervalloGiorni else null,
                        giorniSettimana = if (stato.tipoFrequenza == TipoFrequenza.GIORNI_SETTIMANA) stato.giorniSettimana else null,
                        dataInizio = stato.dataInizio,
                        dataFine = stato.dataFine
                    )
                    medicinaleRepository.aggiornaMedicinale(medicinale)
                    medicinaleRepository.aggiornaPianoEOrari(piano, orari)
                } else {
                    val utente = utenteRepository.getUtente().firstOrNull()
                    if (utente == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Utente non trovato") }
                        return@launch
                    }

                    // Creo l'oggetto Medicinale con i dati presenti nello stato del viewModel
                    val medicinale = Medicinale(
                        id = 0,
                        idUtente = utente.id,
                        nome = stato.nome.trim(),
                        dosaggio = stato.dosaggio.trim().ifEmpty { null },
                        note = stato.note.trim().ifEmpty { null },
                        attivo = true,
                        dataInizio = stato.dataInizio,
                        dataFine = stato.dataFine
                    )
                    // Creo l'oggetto PianoAssunzione con i dati presenti nello stato del ViewModel
                    val piano = PianoAssunzione(
                        id = 0,
                        idMedicinale = 0,
                        tipoFrequenza = stato.tipoFrequenza,
                        intervalloGiorni = if (stato.tipoFrequenza == TipoFrequenza.OGNI_N_GIORNI) stato.intervalloGiorni else null,
                        giorniSettimana = if (stato.tipoFrequenza == TipoFrequenza.GIORNI_SETTIMANA) stato.giorniSettimana else null,
                        dataInizio = stato.dataInizio,
                        dataFine = stato.dataFine
                    )
                    // Inserisco tutto nel database
                    medicinaleRepository.inserisciMedicinaleConPianoEOrari(medicinale, piano, orari)

                    if (medicinale.dataInizio == LocalDate.now()) {

                        val medicinali = medicinaleRepository
                            .getMedicinaliAttiviConPianoEOrari(utente.id)
                            .firstOrNull() ?: emptyList()

                        if (medicinali.isNotEmpty()) {
                            assunzioneRepository.generaAssunzioniPerGiorno(
                                utente.id,
                                LocalDate.now(),
                                medicinali
                            )
                        }
                    }
                }

                // Notifico la UI che l'operazione asincrona è finita e che il salvataggio è stato completato
                _uiState.update { it.copy(isLoading = false, salvatagioCompletato = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
        return true
    }

    private fun valida(): Boolean {
        val stato = _uiState.value
        var valido = true

        if (stato.nome.isBlank()) {
            _uiState.update { it.copy(nomeError = true) }
            valido = false
        }

        if (stato.orari.isEmpty()) {
            _uiState.update { it.copy(orariError = true) }
            valido = false
        }

        if (stato.tipoFrequenza == TipoFrequenza.GIORNI_SETTIMANA && stato.giorniSettimana.isEmpty()) {
            _uiState.update { it.copy(giorniSettimanaError = true) }
            valido = false
        }

        val dataFine = stato.dataFine

        dataFine?.let {
            if (stato.dataInizio.isAfter(dataFine)) {
                _uiState.update { it.copy(dataError = true) }
                valido = false
            }
        }

        return valido
    }

    fun reset() {
        _uiState.value = AggiungiMedicinaleUiState()
    }

}