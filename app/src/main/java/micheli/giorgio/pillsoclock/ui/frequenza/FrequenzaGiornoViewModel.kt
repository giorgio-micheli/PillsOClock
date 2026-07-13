package micheli.giorgio.pillsoclock.ui.frequenza

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
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

data class FrequenzaGiornoUiState(
    val data: LocalDate,
    val assunzioni: List<AssunzioneGiornaliera> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class FrequenzaGiornoViewModel(
    private val data: LocalDate,
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FrequenzaGiornoUiState(data = data))
    val uiState: StateFlow<FrequenzaGiornoUiState> = _uiState.asStateFlow()

    init {
        osservaAssunzioniDelGiorno()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun osservaAssunzioniDelGiorno() {
        viewModelScope.launch(Dispatchers.IO) {
            utenteRepository.getUtente()
                .flatMapLatest { utente ->
                    if (utente == null) {
                        Log.d("FrequenzaGiornoViewModel", "Utente non trovato")
                        flowOf(FrequenzaGiornoUiState(data = data, isLoading = true))
                    } else {
                        assunzioneRepository.getAssunzioniGiornaliere(utente.id, data)
                            .map { assunzioni ->
                                FrequenzaGiornoUiState(
                                    data = data,
                                    assunzioni = assunzioni,
                                    isLoading = false
                                )
                            }
                    }
                }
                .catch { e ->
                    Log.d("FrequenzaGiornoViewModel", e.message ?: "Eccezione")
                    emit(FrequenzaGiornoUiState(data = data, isLoading = false, errorMessage = e.message))
                }
                .collect { nuovoStato ->
                    _uiState.value = nuovoStato
                }
        }
    }
}
