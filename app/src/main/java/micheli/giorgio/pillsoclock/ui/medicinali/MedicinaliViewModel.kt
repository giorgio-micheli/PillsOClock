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
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

data class MedicinaliUiState(
    val medicinali: List<MedicinaleConPianoEOrari> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MedicinaliViewModel(
    private val medicinaleRepository: MedicinaleRepository,
    private val utenteRepository: UtenteRepository
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
            if (medicinale.attivo) {
                medicinaleRepository.disattivaMedicinale(medicinale.id)
            } else {
                medicinaleRepository.attivaMedicinale(medicinale.id)
            }
        }
    }

    fun onEliminaClick(medicinale: Medicinale) {
        viewModelScope.launch(Dispatchers.IO) {
            medicinaleRepository.eliminaMedicinale(medicinale.id)
        }
    }
}
