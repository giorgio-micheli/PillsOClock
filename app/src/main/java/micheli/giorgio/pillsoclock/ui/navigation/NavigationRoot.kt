package micheli.giorgio.pillsoclock.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.bottomsheet.BottomSheetScene
import com.example.nav3recipes.bottomsheet.BottomSheetSceneStrategy
import kotlinx.serialization.Serializable
import micheli.giorgio.pillsoclock.ui.addMedicine.AddMedicinale
import micheli.giorgio.pillsoclock.ui.frequenza.FrequenzaGiornoScreen
import micheli.giorgio.pillsoclock.ui.frequenza.FrequenzaScreen
import micheli.giorgio.pillsoclock.ui.home.HomeScreen
import micheli.giorgio.pillsoclock.ui.medicinali.MedicinaliScreen
import micheli.giorgio.pillsoclock.ui.settings.SettingsScreen
import micheli.giorgio.pillsoclock.ui.theme.onBackgroundDark
import java.time.LocalDate
import kotlin.collections.listOf


/*
1) Le keys vengono aggiunte o rimosse dal backstack in risposta agli eventi utente.

2) Il NavigationRoot è un composable che renderizza un backstack e quindi lo osserva.
Di default mostra la top entry presente nel backstack in modalità "single-pane".
Quando la key in cima al backstack cambia, il NavDisplay la prende e la usa per richiedere
all'entry provider il relativo Composable da mostrare.

3) L'entry provider è una funzione che a partire da una key data in input, ritorna un NavEntry.
Un NavEntry è un oggetto che contiene sia la key, sia il Composable da mostrare.
4) Il NavDisplay riceve il NavEntry e mostra il Composable a schermo.
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    // Backstack di Navigation3 gestito completamente dal developer
    // Viene implementato come una lista osservabile da Compose che contiene delle keys.
    val backStack = remember { mutableStateListOf<Any>(Routes.Home) }
    // Necessario per avere il bottom sheet dialog come destinazione di Navigation3
    val sceneStrategies = remember { listOf(BottomSheetSceneStrategy<Any>()) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeFromBackstack() },
        sceneStrategies = sceneStrategies,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        popTransitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        predictivePopTransitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        entryProvider = { key ->
            when (key) {
                is Routes.Home -> NavEntry(key) {
                    HomeScreen(
                        onAddMedicineButtonClick = {
                            backStack.addToBackstack(Routes.AddMedicine())
                        },
                        onUserSettingsButtonClick = {
                            backStack.addToBackstack(Routes.Settings)
                        },
                        onFrequenzaButtonClick = {
                            backStack.addToBackstack(Routes.Frequenza)
                        },
                        onMedicinaliButtonClick = {
                            backStack.addToBackstack(Routes.Medicinali)
                        }
                    )
                }
                is Routes.AddMedicine -> NavEntry(
                    key,
                    metadata = BottomSheetSceneStrategy.bottomSheet()
                ) {
                    AddMedicinale(idMedicinale = key.idMedicinale)
                }
                is Routes.Medicinali -> NavEntry(key) {
                    MedicinaliScreen(
                        onBack = { backStack.removeFromBackstack() },
                        onModificaMedicinaleClick = { idMedicinale ->
                            backStack.addToBackstack(Routes.AddMedicine(idMedicinale))
                        },
                        onAggiungiMedicinaleClick = {
                            backStack.addToBackstack(Routes.AddMedicine())
                        }
                    )
                }
                is Routes.Settings -> NavEntry(key) {
                    SettingsScreen(
                        onBack = { backStack.removeFromBackstack() }
                    )
                }
                is Routes.Frequenza -> NavEntry(key) {
                    FrequenzaScreen(
                        onBack = { backStack.removeFromBackstack() },
                        onGiornoClick = { giorno ->
                            backStack.addToBackstack(Routes.FrequenzaGiorno(giorno.toEpochDay()))
                        }
                    )
                }
                is Routes.FrequenzaGiorno -> NavEntry(key) {
                    FrequenzaGiornoScreen(
                        data = LocalDate.ofEpochDay(key.epochDay),
                        onBack = { backStack.removeFromBackstack() }
                    )
                }
                else -> NavEntry(Unit) {

                }
            }
        }
    )
}

/**
 * Extension function che esegue un controllo di non vuotezza
 * prima di rimuovere l'ultimo elemento in coda nella lista
 */
fun SnapshotStateList<Any>.removeFromBackstack(): Unit {
    if (size > 1) removeAt(size - 1)
}

/**
 * Extension function che aggiunge un elemento in coda alla lista solamente se è diverso
 * dall'ultimo elemento attualmente presente in essa
 */
fun SnapshotStateList<Any>.addToBackstack(key: NavKey): Unit {
    if (this[size - 1] != key) this.add(key)
}