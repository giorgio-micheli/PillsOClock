package micheli.giorgio.pillsoclock.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes: NavKey {
    @Serializable
    data object Home : Routes
    @Serializable
    data object AddMedicine : Routes
    @Serializable
    data object Settings : Routes

    sealed interface SettingsRoutes : NavKey {

    }







}