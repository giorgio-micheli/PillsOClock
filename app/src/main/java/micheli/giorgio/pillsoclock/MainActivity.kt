package micheli.giorgio.pillsoclock

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = applicationContext as PillsOClockApp
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(app.impostazioniRepository)
            )
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            AppTheme(
                darkTheme = uiState.darkModeAbilitata
            ) {
                val snackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                        NavigationRoot()
                }
            }
        }
    }
}