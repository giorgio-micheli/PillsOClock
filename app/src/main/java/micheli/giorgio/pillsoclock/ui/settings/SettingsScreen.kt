package micheli.giorgio.pillsoclock.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAccountScreen: () -> Unit
    // Se ci serviranno delle callback per navigare nelle altre sezioni andranno aggiunte qui
) {

    var darkModeEnabled by remember { mutableStateOf(false) }

    // Dichiaro e definisco le varie sezioni che saranno presenti nella schermata
    val sections = listOf(
        SettingsSection(
            title = "Generali",
            items = listOf(
                SettingsItem.Clickable(
                    title = "Account",
                    "Informazioni sull'account",
                    onClick = onNavigateToAccountScreen
                ),
                SettingsItem.Toggle(
                    title = "Dark mode",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
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
        SettingsScreen(
            onNavigateToAccountScreen = {}
        )
    }
}