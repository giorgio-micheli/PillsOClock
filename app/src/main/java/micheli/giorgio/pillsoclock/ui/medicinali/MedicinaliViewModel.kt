package micheli.giorgio.pillsoclock.ui.medicinali

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.domain.model.Medicinale
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

data class MedicinaliUiState(
    val medicinali: List<MedicinaleConPianoEOrari> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MedicinaliViewModel(
    private val medicinaleRepository: MedicinaleRepository,
    private val utenteRepository: UtenteRepository,
    private val assunzioneRepository: AssunzioneRepository,
    private val promemoriaRepository: PromemoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicinaliUiState())
    val uiState: StateFlow<MedicinaliUiState> = _uiState.asStateFlow()

    init {
        osservaMedicinali()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun osservaMedicinali() {
        viewModelScope.launch(Dispatchers.IO) {
            utenteRepository.getUtente()
                .flatMapLatest { utente ->
                    if (utente == null) {
                        flowOf(MedicinaliUiState(isLoading = true))
                    } else {
                        medicinaleRepository.getTuttiMedicinaliConPianoEOrari(utente.id)
                            .map { medicinali ->
                                MedicinaliUiState(
                                    medicinali = medicinali.sortedBy { it.medicinale.nome.lowercase() },
                                    isLoading = false
                                )
                            }
                    }
                }
                .catch { e ->
                    Log.d("MedicinaliViewModel", e.message ?: "Eccezione")
                    emit(MedicinaliUiState(isLoading = false, errorMessage = e.message))
                }
                .collect { nuovoStato ->
                    _uiState.value = nuovoStato
                }
        }
    }

    fun onToggleClick(medicinale: Medicinale) {
        viewModelScope.launch(Dispatchers.IO) {
            val idPiani = uiState.value.medicinali
                .firstOrNull { it.medicinale.id == medicinale.id }
                ?.piani?.map { it.piano.id }
                ?: emptyList()
            val oggi = LocalDate.now()

            if (medicinale.attivo) {
                medicinaleRepository.disattivaMedicinale(medicinale.id)
                // Un medicinale appena disattivato non deve continuare a far
                // scattare un promemoria oggi (non risolve il ricalcolo delle
                // AssunzionePrevista, bug noto e separato).
                idPiani.forEach { idPiano ->
                    assunzioneRepository.getAssunzioniPrevisteInAttesaOggiPerPiano(idPiano, oggi)
                        .forEach { promemoriaRepository.cancella(it.id) }
                }
            } else {
                medicinaleRepository.attivaMedicinale(medicinale.id)
                // Ripianifica subito i promemoria di oggi ancora in attesa, altrimenti
                // resterebbero persi fino al prossimo ricalcolo di mezzanotte.
                idPiani.forEach { idPiano ->
                    assunzioneRepository.getAssunzioniPrevisteInAttesaOggiPerPiano(idPiano, oggi)
                        .forEach {
                            promemoriaRepository.pianifica(it.id, it.data, it.orarioPrevisto, medicinale.nome, medicinale.dosaggio)
                        }
                }
            }
        }
    }

    fun onEliminaClick(medicinale: Medicinale) {
        viewModelScope.launch(Dispatchers.IO) {
            val idPiani = uiState.value.medicinali
                .firstOrNull { it.medicinale.id == medicinale.id }
                ?.piani?.map { it.piano.id }
                ?: emptyList()
            val oggi = LocalDate.now()

            medicinaleRepository.eliminaMedicinale(medicinale.id)
            idPiani.forEach { idPiano ->
                assunzioneRepository.getAssunzioniPrevisteInAttesaOggiPerPiano(idPiano, oggi)
                    .forEach { promemoriaRepository.cancella(it.id) }
            }
        }
    }
}
