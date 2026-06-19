package micheli.giorgio.pillsoclock.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

data class HomeUiState(
    val assunzioni: List<AssunzioneGiornaliera> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
class HomeViewModel(
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository
) : ViewModel() {

    // reference privata mutabile
    private val _uiState = MutableStateFlow(HomeUiState())
    // reference pubblica immutabile
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
                        /*
                        Nel caso in cui la coroutine responsabile della creazione dell'utente di default
                        non abbia ancora creato l'utente prima che questo flow venga collezionato, allora
                        ci mettiamo in stato di caricamento, dato che è semplicemente una questione di
                        tempo prima che l'utente venga creato.
                         */
                        flowOf(HomeUiState(isLoading = true))
                    } else {
                        Log.d("osservaAssunzioniDiOggi", "Utente trovato, recupero assunzioni")
                        assunzioneRepository.getAssunzioniGiornaliere(utente.id, LocalDate.now())
                            .map { assunzioni ->
                                HomeUiState(assunzioni = assunzioni, isLoading = false)
                            }
                    }
                }
                .catch { e ->
                    Log.d("osservaAssunzioniDiOggi", e.message ?: "Eccezione")
                    emit(HomeUiState(isLoading = false, errorMessage = e.message))
                }
                .collect { nuovoStato ->
                    Log.d("osservaAssunzioniDiOggi", "stato aggiornato correttamente")
                    _uiState.value = nuovoStato
                }
        }
    }

    fun onAssumiClick(assunzionePrevista: AssunzionePrevista) {
        viewModelScope.launch {
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