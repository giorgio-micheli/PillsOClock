package micheli.giorgio.pillsoclock.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import micheli.giorgio.pillsoclock.BuildConfig
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.R
import micheli.giorgio.pillsoclock.ui.theme.AppTheme

/**
    Interfaccia sealed che dichiara tutte le classi che la implementano.
    Elenca tutti i vari settings item che possiamo trovare nella schermata delle impostazioni di quest'app
 */
sealed interface SettingsItem {
    data class Toggle(
        val title: String,
        val subtitle: String? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem

    data class Clickable(
        val title: String,
        val subtitle: String? = null,
        val onClick: () -> Unit
    ) : SettingsItem
}

/**
 * Classe che rappresenta lo stato di una sezione nella schermata delle impostazioni
 */
data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

@Composable
fun SettingsScreen(
    onNavigateToAccountScreen: () -> Unit
    // Se ci serviranno delle callback per navigare nelle altre sezioni andranno aggiunte qui
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(app.impostazioniRepository)
    )

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    Settings(
        uiState = uiState,
        onDarkModeToggle = settingsViewModel::onDarkModeToggle,
        onNavigateToAccountScreen = onNavigateToAccountScreen
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    uiState: SettingsUiState,
    onDarkModeToggle: (Boolean) -> Unit,
    onNavigateToAccountScreen: () -> Unit
) {
    // Dichiaro e definisco le varie sezioni che saranno presenti nella schermata
    val sections = listOf(
        SettingsSection(
            title = stringResource(R.string.settings_sezione_generali),
            items = listOf(
                SettingsItem.Clickable(
                    title = stringResource(R.string.settings_item_account_titolo),
                    stringResource(R.string.settings_item_account_sottotitolo),
                    onClick = onNavigateToAccountScreen
                ),
                SettingsItem.Toggle(
                    title = stringResource(R.string.settings_item_dark_mode_titolo),
                    checked = uiState.darkModeAbilitata,
                    onCheckedChange = onDarkModeToggle
                )
            )
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        /*
         Per ogni sezione creo a livello UI un Header e i relativi sotto-items, distinguendoli
         tra Toggle e Clickable. Quando aggiungeremo un'altra classe che implementa la sealed interface
         SettingsItem allora dovremo anche aggiungere un'altra riga a questa when expression.
         Imposto infine un divisore orizzontale per separare le varie sezioni.
         */
        sections.forEach { section ->
            item {
                SectionHeader(section.title)
            }
            items(section.items) { item ->
                when (item) {
                    is SettingsItem.Toggle -> SettingsToggleRow(item)
                    is SettingsItem.Clickable -> SettingsClickableRow(item)
                }
            }
            item {
                HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
                )
            }
        }

        item {
            val label = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · ${BuildConfig.BUILD_TYPE}"
            Text(
                text = label,
                color = Color.White,
                modifier = Modifier
                    .background(Color.Red)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    item: SettingsItem.Toggle
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge)
            item.subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = item.checked, onCheckedChange = item.onCheckedChange)
    }
}

@Composable
private fun SettingsClickableRow(item: SettingsItem.Clickable) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge)
            item.subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(Icons.Default.AccountCircle, contentDescription = null)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        Settings(
            uiState = SettingsUiState(),
            onDarkModeToggle = {},
            onNavigateToAccountScreen = {}
        )
    }
}