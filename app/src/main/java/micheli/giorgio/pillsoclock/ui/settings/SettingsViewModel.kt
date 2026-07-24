package micheli.giorgio.pillsoclock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.domain.repository.ImpostazioniRepository

data class SettingsUiState(
    val darkModeAbilitata: Boolean = false
)

class SettingsViewModel(
    private val impostazioniRepository: ImpostazioniRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        osservaImpostazioni()
    }

    private fun osservaImpostazioni() {
        viewModelScope.launch(Dispatchers.IO) {
            impostazioniRepository.getDarkModeAbilitata()
                .collect { abilitata ->
                    _uiState.update { it.copy(darkModeAbilitata = abilitata) }
                }
        }
    }

    fun onDarkModeToggle(abilitata: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            impostazioniRepository.impostaDarkMode(abilitata)
        }
    }
}
