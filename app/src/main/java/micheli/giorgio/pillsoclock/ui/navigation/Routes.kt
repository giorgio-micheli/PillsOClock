package micheli.giorgio.pillsoclock.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes: NavKey {
    @Serializable
    data object Home : Routes
    // idMedicinale è null quando si sta aggiungendo un nuovo medicinale,
    // valorizzato quando si sta modificando un medicinale esistente.
    @Serializable
    data class AddMedicine(val idMedicinale: Int? = null) : Routes
    @Serializable
    data object Settings : Routes

    @Serializable
    data object Frequenza : Routes

    // La data viene passata come epochDay (Long) perché java.time.LocalDate
    // non è direttamente serializzabile con kotlinx.serialization.
    @Serializable
    data class FrequenzaGiorno(val epochDay: Long) : Routes

    @Serializable
    data object Medicinali : Routes

    sealed interface SettingsRoutes : NavKey {

    }







}