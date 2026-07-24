package micheli.giorgio.pillsoclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import micheli.giorgio.pillsoclock.domain.repository.ImpostazioniRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class MainViewModelFactory(
    private val impostazioniRepository: ImpostazioniRepository,
    private val utenteRepository: UtenteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(impostazioniRepository, utenteRepository) as T
        }
        throw IllegalArgumentException("ViewModel class non riconosciuta: ${modelClass.name}")
    }
}
