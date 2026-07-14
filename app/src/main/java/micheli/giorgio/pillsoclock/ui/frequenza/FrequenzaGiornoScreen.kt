package micheli.giorgio.pillsoclock.ui.frequenza

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.domain.model.AssunzioneEffettuata
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FrequenzaGiornoScreen(data: LocalDate) {
    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp

    val frequenzaGiornoViewModel: FrequenzaGiornoViewModel = viewModel(
        factory = FrequenzaGiornoViewModelFactory(
            data = data,
            assunzioneRepository = app.assunzioneRepository,
            utenteRepository = app.utenteRepository
        )
    )

    val uiState by frequenzaGiornoViewModel.uiState.collectAsStateWithLifecycle()

    FrequenzaGiorno(uiState = uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequenzaGiorno(
    uiState: FrequenzaGiornoUiState
) {
    val formatterData = remember { DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN) }
    val dataFormattata = remember(uiState.data) {
        uiState.data.format(formatterData).replaceFirstChar { it.uppercase() }
    }

    val assunte = uiState.assunzioni.count { it.assunzionePrevista.stato == StatoAssunzione.ASSUNTA }
    val totali = uiState.assunzioni.size

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        uiState.assunzioni.isEmpty() -> {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NessunaAssunzioneRegistrata()
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RiepilogoGiorno(assunte = assunte, totali = totali)

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        uiState.assunzioni.sortedBy { it.assunzionePrevista.orarioPrevisto },
                        key = { it.assunzionePrevista.id }
                    ) { assunzione ->
                        AssunzioneGiornoCard(assunzione)
                    }
                }
            }
        }
    }
}

@Composable
private fun RiepilogoGiorno(assunte: Int, totali: Int) {
    val progresso = (assunte.toFloat() / totali.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Assunzioni del giorno",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$assunte/$totali",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progresso)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun NessunaAssunzioneRegistrata() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Nessuna assunzione registrata",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Non risultano medicine previste per questo giorno",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AssunzioneGiornoCard(assunzione: AssunzioneGiornaliera) {
    val stato = assunzione.assunzionePrevista.stato
    val containerColor: Color
    val contentColor: Color
    when (stato) {
        StatoAssunzione.ASSUNTA -> {
            containerColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        }
        StatoAssunzione.SALTATA -> {
            containerColor = MaterialTheme.colorScheme.errorContainer
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        }
        StatoAssunzione.IN_ATTESA -> {
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (stato == StatoAssunzione.ASSUNTA || stato == StatoAssunzione.SALTATA) {
                    Icon(
                        imageVector = if (stato == StatoAssunzione.ASSUNTA) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = assunzione.nomeMedicinale,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (!assunzione.dosaggio.isNullOrBlank()) {
                        Text(
                            text = assunzione.dosaggio,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = "Prevista per le ${
                            assunzione.assunzionePrevista.orarioPrevisto.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            )
                        }",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Text(
                text = when (stato) {
                    StatoAssunzione.ASSUNTA -> {
                        val orarioEffettivo = assunzione.assunzioneEffettuata
                            ?.timestampAssunzione
                            ?.format(DateTimeFormatter.ofPattern("HH:mm"))
                        if (orarioEffettivo != null) "Assunta alle $orarioEffettivo" else "Assunta"
                    }
                    StatoAssunzione.SALTATA -> "Saltata"
                    StatoAssunzione.IN_ATTESA -> "In attesa"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            )
        }
    }
}

// --- Preview: dati di prova ---

private fun assunzioneDiProva(
    id: Int,
    nome: String,
    stato: StatoAssunzione,
    orario: LocalTime,
    dosaggio: String? = "1 compressa"
) = AssunzioneGiornaliera(
    assunzionePrevista = AssunzionePrevista(
        id = id,
        idOrarioAssunzione = id,
        data = LocalDate.now(),
        orarioPrevisto = orario,
        stato = stato
    ),
    assunzioneEffettuata = if (stato == StatoAssunzione.ASSUNTA) {
        AssunzioneEffettuata(
            id = id,
            idAssunzionePrevista = id,
            idUtente = 1,
            timestampAssunzione = LocalDateTime.now(),
            note = null
        )
    } else null,
    nomeMedicinale = nome,
    dosaggio = dosaggio
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FrequenzaGiornoScreenPreview() {
    AppTheme {
        FrequenzaGiorno(
            uiState = FrequenzaGiornoUiState(
                data = LocalDate.now(),
                assunzioni = listOf(
                    assunzioneDiProva(1, "Omeprazolo", StatoAssunzione.ASSUNTA, LocalTime.of(8, 0)),
                    assunzioneDiProva(2, "Cardioaspirina", StatoAssunzione.SALTATA, LocalTime.of(13, 0), "100mg"),
                    assunzioneDiProva(3, "Vitamina D", StatoAssunzione.IN_ATTESA, LocalTime.of(20, 0), "1 goccia")
                ),
                isLoading = false
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FrequenzaGiornoScreenVuotaPreview() {
    AppTheme {
        FrequenzaGiorno(
            uiState = FrequenzaGiornoUiState(
                data = LocalDate.now(),
                assunzioni = emptyList(),
                isLoading = false
            )
        )
    }
}
