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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate
import java.time.YearMonth

data class FrequenzaUiState(
    val meseVisualizzato: YearMonth = YearMonth.now(),
    val giorniConAssunzioni: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class FrequenzaViewModel(
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository
) : ViewModel() {

    private val meseVisualizzato = MutableStateFlow(YearMonth.now())

    private val _uiState = MutableStateFlow(FrequenzaUiState())
    val uiState: StateFlow<FrequenzaUiState> = _uiState.asStateFlow()

    init {
        osservaGiorniConAssunzioni()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun osservaGiorniConAssunzioni() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(utenteRepository.getUtente(), meseVisualizzato) { utente, mese -> utente to mese }
                .flatMapLatest { (utente, mese) ->
                    if (utente == null) {
                        Log.d("FrequenzaViewModel", "Utente non trovato")
                        flowOf(FrequenzaUiState(meseVisualizzato = mese, isLoading = true))
                    } else {
                        assunzioneRepository.getGiorniConAssunzioni(
                            utente.id,
                            mese.atDay(1),
                            mese.atEndOfMonth()
                        ).map { giorni ->
                            FrequenzaUiState(
                                meseVisualizzato = mese,
                                giorniConAssunzioni = giorni.toSet(),
                                isLoading = false
                            )
                        }
                    }
                }
                .catch { e ->
                    Log.d("FrequenzaViewModel", e.message ?: "Eccezione")
                    emit(FrequenzaUiState(isLoading = false, errorMessage = e.message))
                }
                .collect { nuovoStato ->
                    _uiState.value = nuovoStato
                }
        }
    }

    fun onMesePrecedenteClick() {
        meseVisualizzato.value = meseVisualizzato.value.minusMonths(1)
    }

    fun onMeseSuccessivoClick() {
        meseVisualizzato.value = meseVisualizzato.value.plusMonths(1)
    }
}
