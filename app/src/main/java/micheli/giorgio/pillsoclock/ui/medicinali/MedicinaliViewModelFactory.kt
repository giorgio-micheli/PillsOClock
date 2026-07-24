package micheli.giorgio.pillsoclock.ui.medicinali

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class MedicinaliViewModelFactory(
    private val medicinaleRepository: MedicinaleRepository,
    private val utenteRepository: UtenteRepository,
    private val assunzioneRepository: AssunzioneRepository,
    private val promemoriaRepository: PromemoriaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicinaliViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicinaliViewModel(medicinaleRepository, utenteRepository, assunzioneRepository, promemoriaRepository) as T
        }
        throw IllegalArgumentException("ViewModel class non riconosciuta: ${modelClass.name}")
    }
}
