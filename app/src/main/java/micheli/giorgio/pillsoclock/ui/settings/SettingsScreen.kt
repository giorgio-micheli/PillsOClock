package micheli.giorgio.pillsoclock.ui.settings

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
import androidx.compose.ui.unit.dp

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

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
    // Se ci serviranno delle callback per navigare nelle altre sezioni andranno aggiunte qui
) {

    var darkModeEnabled by remember { mutableStateOf(false) }

    val sections = listOf(
        SettingsSection(
            title = "Generali",
            items = listOf(
                SettingsItem.Toggle(
                    title = "Dark mode",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            sections.forEach { section ->
                item {

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
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(item: SettingsItem.Toggle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
        Icon(Icons.Filled.Home, contentDescription = null)
    }
}