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

data class MainUiState(
    val darkModeAbilitata: Boolean = false,
    val nomeUtente: String? = null
)

class MainViewModel(
    private val impostazioniRepository: ImpostazioniRepository,
    private val utenteRepository: UtenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            impostazioniRepository.getDarkModeAbilitata()
                .collect { abilitata ->
                    _uiState.update { it.copy(darkModeAbilitata = abilitata) }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            utenteRepository.getUtente()
                .collect { utente ->
                    _uiState.update { it.copy(nomeUtente = utente?.nome?.takeIf { nome -> nome.isNotBlank() }) }
                }
        }
    }
}
