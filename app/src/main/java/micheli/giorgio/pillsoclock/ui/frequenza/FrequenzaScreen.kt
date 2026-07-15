package micheli.giorgio.pillsoclock.ui.frequenza

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.ui.theme.AppTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FrequenzaScreen(
    modifier: Modifier = Modifier,
    onGiornoClick: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp

    val frequenzaViewModel: FrequenzaViewModel = viewModel(
        factory = FrequenzaViewModelFactory(
            assunzioneRepository = app.assunzioneRepository,
            utenteRepository = app.utenteRepository
        )
    )

    val uiState by frequenzaViewModel.uiState.collectAsStateWithLifecycle()

    Frequenza(
        modifier = modifier,
        uiState = uiState,
        onMesePrecedenteClick = frequenzaViewModel::onMesePrecedenteClick,
        onMeseSuccessivoClick = frequenzaViewModel::onMeseSuccessivoClick,
        onGiornoClick = onGiornoClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Frequenza(
    modifier: Modifier = Modifier,
    uiState: FrequenzaUiState,
    onMesePrecedenteClick: () -> Unit,
    onMeseSuccessivoClick: () -> Unit,
    onGiornoClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            SelettoreMese(
                mese = uiState.meseVisualizzato,
                onMesePrecedenteClick = onMesePrecedenteClick,
                onMeseSuccessivoClick = onMeseSuccessivoClick
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                CalendarioMese(
                    mese = uiState.meseVisualizzato,
                    giorniConAssunzioni = uiState.giorniConAssunzioni,
                    onGiornoClick = onGiornoClick
                )
                Legenda()
            }
        }
    }
}

@Composable
private fun SelettoreMese(
    mese: YearMonth,
    onMesePrecedenteClick: () -> Unit,
    onMeseSuccessivoClick: () -> Unit
) {
    val formatterMese = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALIAN) }
    val meseFormattato = remember(mese) {
        mese.format(formatterMese).replaceFirstChar { it.uppercase() }
    }
    val puoAndareAvanti = mese.isBefore(YearMonth.now())

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMesePrecedenteClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mese precedente")
        }
        Text(
            text = meseFormattato,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(
            onClick = onMeseSuccessivoClick,
            enabled = puoAndareAvanti
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Mese successivo",
                tint = if (puoAndareAvanti) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
            )
        }
    }
}

@Composable
private fun CalendarioMese(
    mese: YearMonth,
    giorniConAssunzioni: Set<LocalDate>,
    onGiornoClick: (LocalDate) -> Unit
) {
    val oggi = remember { LocalDate.now() }
    val letterGiorniSettimana = listOf("L", "M", "M", "G", "V", "S", "D")

    val celle = remember(mese) {
        val primoGiorno = mese.atDay(1)
        // dayOfWeek.value: 1=lunedì..7=domenica, la settimana qui parte da lunedì
        val offset = (primoGiorno.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        val giorni = (List(offset) { null } + (1..mese.lengthOfMonth()).map { mese.atDay(it) })
        val riempimentoFinale = (7 - giorni.size % 7) % 7
        giorni + List<LocalDate?>(riempimentoFinale) { null }
    }

    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            letterGiorniSettimana.forEach { lettera ->
                Text(
                    text = lettera,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        celle.chunked(7).forEach { settimana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                settimana.forEach { giorno ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (giorno != null) {
                            CellaGiorno(
                                giorno = giorno,
                                oggi = oggi,
                                haAssunzioni = giorniConAssunzioni.contains(giorno),
                                onClick = { onGiornoClick(giorno) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CellaGiorno(
    giorno: LocalDate,
    oggi: LocalDate,
    haAssunzioni: Boolean,
    onClick: () -> Unit
) {
    val nelFuturo = giorno.isAfter(oggi)
    val èOggi = giorno == oggi

    Box(
        modifier = Modifier
            .padding(3.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (haAssunzioni) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .then(
                if (èOggi) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
                } else {
                    Modifier
                }
            )
            .then(
                if (nelFuturo) Modifier else Modifier.clickable(onClick = onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${giorno.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (èOggi || haAssunzioni) FontWeight.Bold else FontWeight.Normal,
            color = when {
                nelFuturo -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                haAssunzioni -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun Legenda() {
    Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Text(
            text = "Giorni con assunzioni registrate",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FrequenzaScreenPreview() {
    val oggi = LocalDate.now()
    AppTheme {
        Scaffold() { innerPadding ->
            Frequenza(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                uiState = FrequenzaUiState(
                    meseVisualizzato = YearMonth.now(),
                    giorniConAssunzioni = setOf(
                        oggi,
                        oggi.minusDays(1),
                        oggi.minusDays(2),
                        oggi.minusDays(5)
                    ),
                    isLoading = false
                ),
                onMesePrecedenteClick = {},
                onMeseSuccessivoClick = {},
                onGiornoClick = {}
            )
        }
    }
}
