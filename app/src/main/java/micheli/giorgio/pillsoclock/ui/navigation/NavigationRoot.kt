package micheli.giorgio.pillsoclock.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.bottomsheet.BottomSheetSceneStrategy
import micheli.giorgio.pillsoclock.LocalSnackbarHostState
import micheli.giorgio.pillsoclock.ui.addMedicine.AddMedicinale
import micheli.giorgio.pillsoclock.ui.frequenza.FrequenzaGiornoScreen
import micheli.giorgio.pillsoclock.ui.frequenza.FrequenzaScreen
import micheli.giorgio.pillsoclock.ui.home.HomeScreen
import micheli.giorgio.pillsoclock.ui.home.IntestazioneHome
import micheli.giorgio.pillsoclock.ui.medicinali.MedicinaliScreen
import micheli.giorgio.pillsoclock.ui.settings.SettingsScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
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
fun NavigationRoot() {
    // Backstack di Navigation3 gestito completamente dal developer
    // Viene implementato come una lista osservabile da Compose che contiene delle keys.
    val backStack = remember { mutableStateListOf<Any>(Routes.Home) }
    // Necessario per avere il bottom sheet dialog come destinazione di Navigation3
    val sceneStrategies = remember { listOf(BottomSheetSceneStrategy<Any>()) }

    //
    val fabVisible by remember {
        derivedStateOf { backStack.lastOrNull() is Routes.Home }
    }
    // Riferimento costante a State<Float> reso "float" dalla keyword "by"
    val scale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = if (fabVisible) {
            tween(durationMillis = 300)
        } else {
            tween(durationMillis = 200)
        },
        label = "fabScaleAlpha"
    )

    // Unica Scaffold principale dell'app
    Scaffold(
        topBar = {
            // Seleziona una topbar diversa in base alla route corrente
            SelectTopBar(
                route = backStack.lastOrNull(),
                onBack = { backStack.removeFromBackstack() },
                onNavigateToSettings = { backStack.addToBackstack(Routes.Settings) }
            )
        },
        floatingActionButton = {

//            AnimatedVisibility(
//                visible = backStack.lastOrNull() is Routes.Home,
//                enter = scaleIn(tween(300)) + fadeIn(tween(300)),
//                exit = scaleOut(tween(200)) + fadeOut(tween(200))
//            ) {
            FloatingActionButton(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = scale
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                },
                shape = CircleShape,
                onClick = dropUnlessResumed { backStack.addToBackstack(Routes.AddMedicine()) }
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "Aggiungi mediciale",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
           // }
        },
        snackbarHost = {
            SnackbarHost(hostState = LocalSnackbarHostState.current)
        }
    ) { innerPadding ->
        // Composable principale di Nav3
        NavDisplay(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeFromBackstack() },
//            sceneStrategies = sceneStrategies,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            // Transizioni custom globali, valide per tutte le schermate
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
                            modifier = Modifier.fillMaxSize(),
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
//                        metadata = BottomSheetSceneStrategy.bottomSheet()
                    ) {
                        AddMedicinale(
                            idMedicinale = key.idMedicinale,
                            onDismiss = { backStack.removeFromBackstack() }
                        )
                    }
                    is Routes.Medicinali -> NavEntry(key) {
                        MedicinaliScreen(
                            onNavigateToAddMedicinaleScreen = {
                                backStack.addToBackstack(Routes.AddMedicine())
                            },
                            onModificaMedicinaleClick = { idMedicinale ->
                                backStack.addToBackstack(Routes.AddMedicine(idMedicinale))
                            }
                        )
                    }
                    is Routes.Settings -> NavEntry(key) {
                        SettingsScreen(
                            onNavigateToAccountScreen = {
                                backStack.addToBackstack(Routes.SettingsRoutes.Account)
                            }
                        )
                    }
                    is Routes.SettingsRoutes.Account -> NavEntry(key) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Account screen")
                        }
                    }
                    is Routes.Frequenza -> NavEntry(key) {
                        FrequenzaScreen(
                            onGiornoClick = { giorno ->
                                backStack.addToBackstack(Routes.FrequenzaGiorno(giorno.toEpochDay()))
                            }
                        )
                    }
                    is Routes.FrequenzaGiorno -> NavEntry(key) {
                        FrequenzaGiornoScreen(LocalDate.ofEpochDay(key.epochDay))
                    }
                    else -> NavEntry(Unit) {

                    }
                }
            }
        )
    }
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


/**
 * Funzione che ritorna una topBar e quindi un Composable diverso in base alla schermata corrente
 * @route La destinazione corrente
 * @onBack La lambda che permette di togliere la route corrente dal backstack e tornare alla schermata precedente
 * @onNavigateToSettings Lambda utilizzata nella topBar della homeScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTopBar(
    route: Any?,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {

    AnimatedContent(
        targetState = route,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        label = "topBarTransition"
    ) { currentRoute ->

        when (currentRoute) {
            is Routes.Home -> IntestazioneHome(onNavigateToSettings)
            is Routes.AddMedicine, is Routes.Settings, is Routes.SettingsRoutes.Account, is Routes.Frequenza, is Routes.FrequenzaGiorno, is Routes.Medicinali -> {

                val title = when (currentRoute) {
                    is Routes.Settings -> "Impostazioni"
                    is Routes.SettingsRoutes.Account -> "Dettagli account"
                    is Routes.Frequenza -> "Frequenza"
                    is Routes.Medicinali -> "Medicinali"
                    is Routes.AddMedicine -> if (currentRoute.idMedicinale != null) "Modifica medicinale" else "Nuovo medicinale"
                    is Routes.FrequenzaGiorno -> {
                        val formatterData = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
                        LocalDate.ofEpochDay(currentRoute.epochDay).format(formatterData).replaceFirstChar { it.uppercase() }
                    }
                    else -> ""
                }

                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                        }
                    }
                )
            }
        }
    }
}