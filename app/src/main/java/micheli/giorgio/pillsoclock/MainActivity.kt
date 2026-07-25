package micheli.giorgio.pillsoclock

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import micheli.giorgio.pillsoclock.ui.home.HomeScreen
import micheli.giorgio.pillsoclock.ui.home.HomeViewModel
import micheli.giorgio.pillsoclock.ui.navigation.NavigationRoot
import micheli.giorgio.pillsoclock.ui.theme.AppTheme

/*
Viene usato staticCopositionLocalOf perchè l'istanza di SnackbarHostState che viene creata
non cambia mai nel corso della vita dell'applicazione. Il suo valore viene istanziato solamente
a livello root dell'app ma nei composable più annidati rimane sempre quello, non ne viene mai
fornita un'altra istanza. In questo modo usando staticCompositionLocalOf abbiamo dei guadagni
in termini di performance rispetto a compositionLocalOf.
 */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarHostState value not found")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen api, nessuna animazione custom
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            // Richiede all'utente il permesso per le notifiche
            NotificationPermissionHandler()
            // Guida l'utente ad attivare gli allarmi esatti, senza i quali i
            // promemoria potrebbero arrivare in ritardo o non arrivare affatto
            ExactAlarmPermissionHandler()

            val app = applicationContext as PillsOClockApp
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(app.impostazioniRepository, app.utenteRepository)
            )
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            AppTheme(
                darkTheme = uiState.darkModeAbilitata
            ) {
                val snackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                        NavigationRoot(nomeUtente = uiState.nomeUtente)
                }
            }
        }
    }
}

/**
 * Mostra il dialog di sistema per far accettare il permesso per le notifiche all'utente
 */
@Composable
fun NotificationPermissionHandler() {

    val context = LocalContext.current
    var hasNotificationPermission by remember {
        // Se il device è sotto l'api level 33 non abbiamo bisogno dell'autorizzazione dell'utente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else mutableStateOf(true)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * Mostra un dialog bloccante finché l'utente non concede il permesso per gli
 * allarmi esatti (Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM): senza,
 * i promemoria delle assunzioni possono arrivare in ritardo o non arrivare
 * affatto. Non esiste un ActivityResultContract per questo permesso (è un
 * redirect a Impostazioni, non un permesso runtime classico), quindi lo stato
 * viene ricontrollato a ogni ON_RESUME.
 */
@Composable
fun ExactAlarmPermissionHandler() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp
    val lifecycleOwner = LocalLifecycleOwner.current

    var puoPianificareAllarmiEsatti by remember {
        mutableStateOf(app.promemoriaRepository.puoPianificareAllarmiEsatti())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                puoPianificareAllarmiEsatti = app.promemoriaRepository.puoPianificareAllarmiEsatti()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!puoPianificareAllarmiEsatti) {
        AlertDialog(
            onDismissRequest = { /* bloccante: non può essere chiuso senza concedere il permesso */ },
            title = { Text("Permesso allarmi esatti necessario") },
            text = {
                Text(
                    "Per avvisarti puntualmente quando è ora di prendere una medicina, " +
                        "PillsOClock ha bisogno del permesso per pianificare allarmi esatti. " +
                        "Attivalo dalle impostazioni di sistema."
                )
            },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Apri impostazioni")
                }
            }
        )
    }
}