package micheli.giorgio.pillsoclock.ui.addMedicine

import androidx.lifecycle.ViewModel
import micheli.giorgio.pillsoclock.data.local.entity.Pill
import micheli.giorgio.pillsoclock.data.local.dao.PillDao

data class pillsUiState(
    val pills: List<Pill>
)

class addPillViewModel(
    private val dao: PillDao
) : ViewModel() {

}