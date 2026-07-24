package micheli.giorgio.pillsoclock.ui.medicinali

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.R
import micheli.giorgio.pillsoclock.data.local.entity.TipoFrequenza
import micheli.giorgio.pillsoclock.domain.model.Medicinale
import micheli.giorgio.pillsoclock.domain.model.MedicinaleConPianoEOrari
import micheli.giorgio.pillsoclock.domain.model.OrarioAssunzione
import micheli.giorgio.pillsoclock.domain.model.PianoAssunzione
import micheli.giorgio.pillsoclock.domain.model.PianoConOrari
import micheli.giorgio.pillsoclock.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun MedicinaliScreen(
    modifier: Modifier = Modifier,
    onModificaMedicinaleClick: (Int) -> Unit,
    onNavigateToAddMedicinaleScreen: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp

    val medicinaliViewModel: MedicinaliViewModel = viewModel(
        factory = MedicinaliViewModelFactory(
            medicinaleRepository = app.medicinaleRepository,
            utenteRepository = app.utenteRepository,
            assunzioneRepository = app.assunzioneRepository,
            promemoriaRepository = app.promemoriaRepository
        )
    )

    val uiState by medicinaliViewModel.uiState.collectAsStateWithLifecycle()

    Medicinali(
        modifier = modifier,
        uiState = uiState,
        onNavigateToAddMedicinaleScreen = onNavigateToAddMedicinaleScreen,
        onModificaMedicinaleClick = onModificaMedicinaleClick,
        onToggleAttivoClick = medicinaliViewModel::onToggleClick,
        onEliminaClick = medicinaliViewModel::onEliminaClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Medicinali(
    modifier: Modifier = Modifier,
    uiState: MedicinaliUiState,
    onNavigateToAddMedicinaleScreen: () -> Unit,
    onModificaMedicinaleClick: (Int) -> Unit,
    onToggleAttivoClick: (Medicinale) -> Unit,
    onEliminaClick: (Medicinale) -> Unit
) {
    when {
        // Stato di caricamento
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        // Stato "nessun medicinale esistente"
        uiState.medicinali.isEmpty() -> {
            MedicinaliVuoto(
                modifier = Modifier
                    .fillMaxSize(),
                onAggiungiClick = onNavigateToAddMedicinaleScreen
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.medicinali, key = { it.medicinale.id }) { medicinaleConPiano ->
                    MedicinaleCard(
                        medicinaleConPiano = medicinaleConPiano,
                        onClick = { onModificaMedicinaleClick(medicinaleConPiano.medicinale.id) },
                        onToggleAttivoClick = { onToggleAttivoClick(medicinaleConPiano.medicinale) },
                        onEliminaClick = { onEliminaClick(medicinaleConPiano.medicinale) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicinaliVuoto(
    modifier: Modifier = Modifier,
    onAggiungiClick: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        val (image, content) = createRefs()

        Column(
            modifier = Modifier.constrainAs(content) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Nessun medicinale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Aggiungi un medicinale per iniziare a tenere traccia delle tue assunzioni",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onAggiungiClick, shape = RoundedCornerShape(50)) {
                Text("Aggiungi medicinale")
            }
        }

        Image(
            modifier = Modifier
                .size(64.dp)
                .constrainAs(image) {
                    bottom.linkTo(content.top, margin = 16.dp)
                    start.linkTo(content.start)
                    end.linkTo(content.end)
                },
            painter = painterResource(R.drawable.pill_box),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Badge circolare identico nella forma a quello della home, ma qui può
 * apparire in tonalità grigia per i medicinali disattivati o scaduti.
 */
@Composable
private fun BadgePillola(attivo: Boolean) {

    val color by animateColorAsState(
        targetValue = if (attivo) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(26.dp),
            painter = painterResource(R.drawable.pill),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Card di un medicinale nella lista "Medicinali". Tap per modificare, long
 * press per far comparire — nella stessa forma della card, senza AlertDialog
 * — la conferma di eliminazione definitiva.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MedicinaleCard(
    medicinaleConPiano: MedicinaleConPianoEOrari,
    onClick: () -> Unit,
    onToggleAttivoClick: () -> Unit,
    onEliminaClick: () -> Unit
) {
    var confermaEliminazioneVisibile by remember { mutableStateOf(false) }

    val medicinale = medicinaleConPiano.medicinale
    val scaduto = medicinale.dataFine?.isBefore(LocalDate.now()) == true
    val inGrigio = !medicinale.attivo || scaduto

    val color by animateColorAsState(
        targetValue = if (confermaEliminazioneVisibile) {
            MaterialTheme.colorScheme.errorContainer
        } else if (inGrigio) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = tween(300),
        label = "Container_color_animation"
    )

    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = if (confermaEliminazioneVisibile) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        //border = BorderStroke(2.dp, if (confermaEliminazioneVisibile) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .combinedClickable(
                    onClick = { if (!confermaEliminazioneVisibile) onClick() },
                    onLongClick = { confermaEliminazioneVisibile = true }
                )
        ) {
            AnimatedContent(
                targetState = confermaEliminazioneVisibile,
                transitionSpec = {
                    (fadeIn(tween(150)) + scaleIn(initialScale = 0.92f, animationSpec = tween(150))) togetherWith
                            (fadeOut(tween(120)) + scaleOut(targetScale = 0.92f, animationSpec = tween(120))) using
                            SizeTransform(clip = false)
                },
                label = "confermaEliminazione"
            ) { inConferma ->
                if (inConferma) {
                    RigaConfermaEliminazione(
                        nomeMedicinale = medicinale.nome,
                        onConferma = {
                            confermaEliminazioneVisibile = false
                            onEliminaClick()
                        },
                        onAnnulla = { confermaEliminazioneVisibile = false }
                    )
                } else {
                    RigaMedicinale(
                        medicinaleConPiano = medicinaleConPiano,
                        inGrigio = inGrigio,
                        scaduto = scaduto,
                        onToggleAttivoClick = onToggleAttivoClick
                    )
                }
            }
        }
    }
}

@Composable
private fun RigaMedicinale(
    medicinaleConPiano: MedicinaleConPianoEOrari,
    inGrigio: Boolean,
    scaduto: Boolean,
    onToggleAttivoClick: () -> Unit
) {
    val medicinale = medicinaleConPiano.medicinale
    val pianoConOrari = medicinaleConPiano.piani.firstOrNull()

    val contenutoColor = if (inGrigio) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgePillola(attivo = !inGrigio)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = medicinale.nome,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contenutoColor
                    )
                    if (scaduto) {
                        Text(
                            text = "· terminato",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (!medicinale.dosaggio.isNullOrBlank()) {
                    Text(
                        text = medicinale.dosaggio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (pianoConOrari != null) {
                    Text(
                        text = testoRiassuntoPiano(pianoConOrari),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Switch(
            checked = medicinale.attivo,
            onCheckedChange = { onToggleAttivoClick() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
private fun RigaConfermaEliminazione(
    nomeMedicinale: String,
    onConferma: () -> Unit,
    onAnnulla: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Eliminare \"$nomeMedicinale\" definitivamente?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onAnnulla,
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    Color.White,
                    Color.White
                )
            ) {
                Text("Annulla")
            }
            Button(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = onConferma
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Elimina")
            }
        }
    }
}

private fun testoRiassuntoPiano(pianoConOrari: PianoConOrari): String {
    val orari = pianoConOrari.orari
        .sortedBy { it.orario }
        .joinToString(", ") { it.orario.format(DateTimeFormatter.ofPattern("HH:mm")) }

    val frequenza = when (pianoConOrari.piano.tipoFrequenza) {
        TipoFrequenza.GIORNALIERA -> "ogni giorno"
        TipoFrequenza.OGNI_N_GIORNI -> "ogni ${pianoConOrari.piano.intervalloGiorni ?: 1} giorni"
        TipoFrequenza.GIORNI_SETTIMANA -> {
            val lettere = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "G", 5 to "V", 6 to "S", 7 to "D")
            val giorni = pianoConOrari.piano.giorniSettimana.orEmpty()
            lettere.filter { (numero, _) -> giorni.contains(numero) }.joinToString(" ") { it.second }
        }
    }

    return if (orari.isEmpty()) frequenza else "$orari · $frequenza"
}

// --- Preview: dati di prova ---

private fun medicinaleDiProva(
    id: Int,
    nome: String,
    dosaggio: String? = "1 compressa",
    attivo: Boolean = true,
    dataFine: LocalDate? = null,
    orari: List<LocalTime> = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
    tipoFrequenza: TipoFrequenza = TipoFrequenza.GIORNALIERA
) = MedicinaleConPianoEOrari(
    medicinale = Medicinale(
        id = id,
        idUtente = 1,
        nome = nome,
        dosaggio = dosaggio,
        note = null,
        attivo = attivo,
        dataInizio = LocalDate.now().minusMonths(1),
        dataFine = dataFine
    ),
    piani = listOf(
        PianoConOrari(
            piano = PianoAssunzione(
                id = id,
                idMedicinale = id,
                tipoFrequenza = tipoFrequenza,
                intervalloGiorni = if (tipoFrequenza == TipoFrequenza.OGNI_N_GIORNI) 3 else null,
                giorniSettimana = if (tipoFrequenza == TipoFrequenza.GIORNI_SETTIMANA) listOf(1, 3, 5) else null,
                dataInizio = LocalDate.now().minusMonths(1),
                dataFine = LocalDate.now().plusDays(2)
            ),
            orari = orari.mapIndexed { index, orario ->
                OrarioAssunzione(id = id * 10 + index, idPianoAssunzione = id, orario = orario)
            }
        )
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicinaliScreenPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Medicinali(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                uiState = MedicinaliUiState(
                    medicinali = listOf(
                        medicinaleDiProva(1, "Omeprazolo"),
                        medicinaleDiProva(2, "Cardioaspirina", dosaggio = "100mg", tipoFrequenza = TipoFrequenza.GIORNI_SETTIMANA),
                        medicinaleDiProva(3, "Vitamina D", attivo = false),
                        medicinaleDiProva(4, "Antibiotico", dataFine = LocalDate.now().minusDays(3))
                    ),
                    isLoading = false
                ),
                onNavigateToAddMedicinaleScreen = {},
                onModificaMedicinaleClick = {},
                onToggleAttivoClick = {},
                onEliminaClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MedicinaliScreenVuotaPreview() {
    AppTheme {
        Scaffold() { innerPadding ->
            Medicinali(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                uiState = MedicinaliUiState(isLoading = false),
                onNavigateToAddMedicinaleScreen = {},
                onModificaMedicinaleClick = {},
                onToggleAttivoClick = {},
                onEliminaClick = {}
            )
        }
    }
}
