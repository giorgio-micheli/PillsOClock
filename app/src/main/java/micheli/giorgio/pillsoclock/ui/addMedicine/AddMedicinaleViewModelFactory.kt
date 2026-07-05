package micheli.giorgio.pillsoclock.ui.addMedicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class AddMedicinaleViewModelFactory(
    private val medicinaleRepository: MedicinaleRepository,
    private val utenteRepository: UtenteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddMedicinaleViewModel::class.java)) {
            return AddMedicinaleViewModel(medicinaleRepository, utenteRepository) as T
        }
        throw IllegalArgumentException("ViewModel class non riconosciuta: ${modelClass.name}")
    }
}