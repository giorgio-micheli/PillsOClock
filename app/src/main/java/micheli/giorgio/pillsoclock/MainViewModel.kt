package micheli.giorgio.pillsoclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.domain.repository.ImpostazioniRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

// Stato generale dell'app
data class MainUiState(
    val darkModeAbilitata: Boolean = false,
    val nomeUtente: String? = null
)

/**
 * Viewmodel che resta in memoria per tutto il ciclo di vita dell'applicazione, indipendentemente
 * dalla schermata in cui si trova l'utente. É legata al composable root.
 */
class MainViewModel(
    private val impostazioniRepository: ImpostazioniRepository,
    private val utenteRepository: UtenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Colleziono il flow riguardante il valore della dark mode in datastore
        viewModelScope.launch(Dispatchers.IO) {
            impostazioniRepository.getDarkModeAbilitata()
                .collect { abilitata ->
                    _uiState.update { it.copy(darkModeAbilitata = abilitata) }
                }
        }
        // Colleziono il flow riguardante il nome utente salvato in Room
        viewModelScope.launch(Dispatchers.IO) {
            utenteRepository.getUtente()
                .collect { utente ->
                    _uiState.update { it.copy(nomeUtente = utente?.nome?.takeIf { nome -> nome.isNotBlank() }) }
                }
        }
    }
}
