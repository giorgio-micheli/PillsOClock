package micheli.giorgio.pillsoclock.ui.navigation

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
import micheli.giorgio.pillsoclock.ui.home.HomeScreen
import micheli.giorgio.pillsoclock.ui.settings.SettingsScreen
import micheli.giorgio.pillsoclock.ui.theme.onBackgroundDark
import kotlin.collections.listOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<Any>(Routes.Home) }
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
        entryProvider = { key ->
            when (key) {
                is Routes.Home -> NavEntry(key) {
                    HomeScreen(
                        onAddMedicineButtonClick = {
                            backStack.addToBackstack(Routes.AddMedicine)
                        },
                        onUserSettingsButtonClick = {
                            backStack.addToBackstack(Routes.Settings)
                        }
                    )
                }
                is Routes.AddMedicine -> NavEntry(
                    key,
                    metadata = BottomSheetSceneStrategy.bottomSheet()
                ) {
                    AddMedicinale()
                }
                is Routes.Settings -> NavEntry(key) {
                    SettingsScreen(
                        onBack = { backStack.removeFromBackstack() }
                    )
                }
                else -> NavEntry(Unit) {

                }
            }
        }
    )
}

fun SnapshotStateList<Any>.removeFromBackstack(): Unit {
    if (size > 1) removeAt(size - 1)
}

fun SnapshotStateList<Any>.addToBackstack(key: NavKey): Unit {
    if (this[size - 1] != key) this.add(key)
}