package micheli.giorgio.pillsoclock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class AccountViewModelFactory(
    private val utenteRepository: UtenteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(utenteRepository) as T
        }
        throw IllegalArgumentException("ViewModel class non riconosciuta: ${modelClass.name}")
    }
}
