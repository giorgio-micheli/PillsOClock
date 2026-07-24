package micheli.giorgio.pillsoclock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.domain.model.Utente
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

data class AccountUiState(
    val nome: String = "",
    val cognome: String = "",
    val email: String = "",
    val isLoading: Boolean = true
)

class AccountViewModel(
    private val utenteRepository: UtenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    // Caricato una sola volta all'apertura della schermata: un collect continuo
    // sovrascriverebbe quello che l'utente sta digitando se il flow riemettesse.
    private var utenteCorrente: Utente? = null

    init {
        caricaUtente()
    }

    private fun caricaUtente() {
        viewModelScope.launch(Dispatchers.IO) {
            val utente = utenteRepository.getUtente().firstOrNull()
            utenteCorrente = utente
            _uiState.update {
                it.copy(
                    nome = utente?.nome ?: "",
                    cognome = utente?.cognome ?: "",
                    email = utente?.email ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun onNomeChange(nome: String) {
        _uiState.update { it.copy(nome = nome) }
    }

    fun onCognomeChange(cognome: String) {
        _uiState.update { it.copy(cognome = cognome) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onSalvaClick() {
        val utente = utenteCorrente ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val aggiornato = utente.copy(
                nome = _uiState.value.nome.trim(),
                cognome = _uiState.value.cognome.trim(),
                email = _uiState.value.email.trim()
            )
            utenteRepository.aggiornaUtente(aggiornato)
            utenteCorrente = aggiornato
        }
    }
}
