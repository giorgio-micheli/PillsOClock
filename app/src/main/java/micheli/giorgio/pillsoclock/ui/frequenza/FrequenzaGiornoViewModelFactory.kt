package micheli.giorgio.pillsoclock.ui.frequenza

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

class FrequenzaGiornoViewModelFactory(
    private val data: LocalDate,
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FrequenzaGiornoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FrequenzaGiornoViewModel(data, assunzioneRepository, utenteRepository) as T
        }
        throw IllegalArgumentException("ViewModel class non riconosciuta: ${modelClass.name}")
    }
}
