package micheli.giorgio.pillsoclock.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class HomeViewModelFactory(
    private val medicinaleRepository: MedicinaleRepository,
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository,
) : ViewModelProvider.Factory {

    /*
    L'interface Factory dichiara dei metodi ma hanno già una implementazione di default,
    quindi quando implemento questa interfaccia non sono obbligato a fornire io una implementazione
    per tutti i metodi che dichiara. Infatti qui stiamo facendo un override di un metodo che ha
    già la sua implementazione di default.
     */
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(medicinaleRepository, assunzioneRepository, utenteRepository) as T
        }
        throw IllegalArgumentException("ViewModel class non riconosciuta: ${modelClass.name}")
    }
}