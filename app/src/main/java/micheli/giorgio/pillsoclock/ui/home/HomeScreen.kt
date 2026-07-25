package micheli.giorgio.pillsoclock.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.LocalSnackbarHostState
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.R
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

private const val DURATA_CHECK_MS = 600L
private const val DURATA_COLLASSO_MS = 300

/*
TODO: nella schermata "Medicinali" non si capisce che premendo una volta sola su un medicinale
  si apre la schermata per editarlo
 */

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAddMedicineButtonClick: () -> Unit,
    onUserSettingsButtonClick: () -> Unit,
    onFrequenzaButtonClick: () -> Unit,
    onMedicinaliButtonClick: () -> Unit
) {

    val context = LocalContext.current
    // applicationContext è una synthetic property generata dal compilatore, chiama il classico metodo getApplicationContext()
    val app = context.applicationContext as PillsOClockApp

    /*
    Non viene creato un viewModel ogni volta che scatta la recomposition.
    La funzione viewModel ritorna la stessa istanza se l'oggetto è già
    stato creato per questo scope.
    Quello che determina se il viewModel viene distrutto e ricreato non è
    la recomposition ma la distruzione dello scope a cui è legato.

    Se il viewModel viene creato all'interno di un Composable che rappresenta
    una destinazione di Navigation Compose, allora sopravvive alle ricomposizioni
    e vive finchè quella destinazione è presente nel back-stack. Viene distrutto
    quando l'utente naviga via da quella schermata.

    Se creassimo il viewModel nella mainActivity esso vivrebbe per tutta la durata
    dell'activity, quindi sopravviverebbe anche quando l'utente naviga su altre
    schermate. In alcuni casi può essere voluto questo comportamento, ma non nella
    maggior parte dei casi.
     */

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            medicinaleRepository = app.medicinaleRepository,
            assunzioneRepository = app.assunzioneRepository,
            utenteRepository = app.utenteRepository,
            promemoriaRepository = app.promemoriaRepository
        )
    )

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Log.d("HOME-SCREEN", "Loading: ${uiState.isLoading}")

    if (uiState.isLoading) {
        LoadingScreen(modifier)
        return
    }

    if (!uiState.errorMessage.isNullOrEmpty()) {
        ErrorScreen(
            modifier,
            uiState.errorMessage!!
        )
        return
    }

    Home(
        modifier = modifier,
        uiState = uiState,
        onAssumiClick = homeViewModel::onAssumiClick,
        onAssumiPuntualeClick = homeViewModel::onAssumiPuntualeClick,
        onAnnullaClick = homeViewModel::onAnnullaClick,
        onAddMedicineButtonClick = onAddMedicineButtonClick,
        onUserSettingsButtonClick = onUserSettingsButtonClick,
        onFrequenzaButtonClick = onFrequenzaButtonClick,
        onMedicinaliButtonClick = onMedicinaliButtonClick
    )
}

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    errorMessage: String
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(24.dp),
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onAssumiClick: (AssunzionePrevista) -> Unit,
    onAssumiPuntualeClick: (AssunzionePrevista) -> Unit,
    onAnnullaClick: (AssunzioneGiornaliera) -> Unit,
    onAddMedicineButtonClick: () -> Unit,
    onUserSettingsButtonClick: () -> Unit,
    onFrequenzaButtonClick: () -> Unit,
    onMedicinaliButtonClick: () -> Unit
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    // Punto unico di registrazione: registra l'assunzione e mostra lo
    // Snackbar con l'azione di annullamento. Tutte le card passano da qui.
    // Parametrizzato dalla funzione di registrazione così sia la conferma
    // "normale" (adesso) sia quella "puntuale" (orario previsto) condividono
    // la stessa logica di Snackbar/annullamento.
    fun confermaEMostraSnackbar(
        assunzione: AssunzioneGiornaliera,
        registra: (AssunzionePrevista) -> Unit
    ) {
        // Questa chiamata è fuori dalla coroutine, quindi viene eseguita sul main thread immediatamente
        registra(assunzione.assunzionePrevista)
        scope.launch {
            // showSnackBar è una suspend function, quindi in certi punti sospenderà la coroutine
            // ma lascerà comunque il thread libero di fare altro, non sta di fatto bloccando il thread
            val risultato = snackbarHostState.showSnackbar(
                message = "${assunzione.nomeMedicinale} segnata come assunta",
                actionLabel = "Annulla",
                duration = SnackbarDuration.Short
            )
            if (risultato == SnackbarResult.ActionPerformed) {
                onAnnullaClick(assunzione)
            }
        }
    }

    // remember: senza, queste lambda vengono ricreate a ogni ricomposizione di
    // Home e, passate a ogni item della LazyColumn, ne invaliderebbero la
    // skippability indipendentemente dal contenuto dei dati.
    val confermaAssunzione: (AssunzioneGiornaliera) -> Unit = remember(onAssumiClick, onAnnullaClick) {
        { assunzione -> confermaEMostraSnackbar(assunzione, onAssumiClick) }
    }
    val confermaAssunzionePuntuale: (AssunzioneGiornaliera) -> Unit = remember(onAssumiPuntualeClick, onAnnullaClick) {
        { assunzione -> confermaEMostraSnackbar(assunzione, onAssumiPuntualeClick) }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        //IntestazioneHome(onUserSettingsButtonClick)

        BarraAderenza(
            assunte = uiState.assunteOggi,
            totali = uiState.totaliOggi
        )

        AzioniRapide(
            onFrequenzaClick = onFrequenzaButtonClick,
            onMedicinaliClick = onMedicinaliButtonClick,
            onAggiungiClick = onAddMedicineButtonClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ProssimaAssunzioneCard(
                    prossima = uiState.prossimaAssunzione,
                    puoAssumereOra = uiState.puoAssumereOra,
                    assunteOggi = uiState.assunteOggi,
                    totaliOggi = uiState.totaliOggi,
                    onConferma = confermaAssunzione,
                    onConfermaPuntuale = confermaAssunzionePuntuale,
                    onAggiungiClick = onAddMedicineButtonClick,
                    medicinaleExist = uiState.esisteAlmenoUnMedicinale,
                    soloAssunzioniInRitardoRimaste = uiState.prossimaAssunzione == null && uiState.prossimaInRitardo != null,
                    prossimaInRitardo = uiState.prossimaInRitardo,
                    minutiAllaProssima = uiState.minutiAllaProssima,
                    minutiRitardo = uiState.minutiRitardo
                )
            }

            if (uiState.inRitardo.isNotEmpty()) {
                item {
                    SezioneTitolo(
                        testo = "IN RITARDO",
                        numero = uiState.inRitardo.size,
                        icon = Icons.Default.Warning,
                        accentColor = MaterialTheme.colorScheme.error
                    )
                }
                items(uiState.inRitardo, key = { it.assunzionePrevista.id }) { assunzione ->
                    AssunzioneRitardataCard(assunzione, confermaAssunzione, confermaAssunzionePuntuale)
                }
            }

            if (uiState.prossimeAssunzioni.isNotEmpty()) {
                item {
                    SezioneTitolo(
                        testo = "PROSSIME ASSUNZIONI",
                        numero = uiState.prossimeAssunzioni.size,
                        icon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                }
                items(uiState.prossimeAssunzioni, key = { it.assunzionePrevista.id }) { assunzione ->
                    ProssimaInCodaCard(assunzione, confermaAssunzione)
                }
            }
        }
    }
}

/**
 * Intestazione della home: saluto contestuale in base all'ora del giorno,
 * data corrente in italiano, e accesso rapido alle impostazioni utente.
 */
@Composable
fun IntestazioneHome(
    onUserSettingsButtonClick: () -> Unit,
    nomeUtente: String? = null
) {
    val oggi = remember { LocalDate.now() }
    val ora = remember { LocalTime.now() }
    val formatterData = remember { DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN) }
    val dataFormattata = remember(oggi) {
        oggi.format(formatterData).replaceFirstChar { it.uppercase() }
    }
    val testoSaluto = remember(ora, nomeUtente) {
        if (!nomeUtente.isNullOrBlank()) "${saluto(ora)}, $nomeUtente" else saluto(ora)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = testoSaluto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = dataFormattata,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            modifier = Modifier
                .shadow(8.dp, CircleShape, false)

                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                ,
            onClick = dropUnlessResumed {
                onUserSettingsButtonClick()
            }
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account"
            )
        }
    }
}

private fun saluto(ora: LocalTime): String = when (ora.hour) {
    in 5..11 -> "Buongiorno"
    in 12..17 -> "Buon pomeriggio"
    else -> "Buonasera"
}

private fun formattaDurata(minuti: Long): String {
    val m = minuti.coerceAtLeast(0)
    val ore = m / 60
    val min = m % 60
    return when {
        ore == 0L -> "$min min"
        min == 0L -> "${ore}h"
        else -> "${ore}h ${min}min"
    }
}

/**
 * Barra di aderenza giornaliera: quante assunzioni previste per oggi sono
 * già state completate. Non viene mostrata se non ci sono assunzioni oggi.
 */
@Composable
private fun BarraAderenza(
    assunte: Int,
    totali: Int,
    modifier: Modifier = Modifier
) {
    if (totali == 0) return

    val progresso = (assunte.toFloat() / totali.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Assunzioni di oggi",
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
private fun AzioniRapide(
    onFrequenzaClick: () -> Unit,
    onMedicinaliClick: () -> Unit,
    onAggiungiClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AzioneRapida(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
            text = "Frequenza",
            onClick = onFrequenzaClick
        )
        AzioneRapida(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.List,
            text = "Medicinali",
            onClick = onMedicinaliClick
        )
//        AzioneRapida(
//            modifier = Modifier.weight(1f),
//            icon = Icons.Default.Add,
//            text = "Aggiungi",
//            onClick = onAggiungiClick
//        )
    }
}

@Composable
private fun AzioneRapida(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = dropUnlessResumed {
            onClick()
        },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = icon,
                contentDescription = ""
            )
            Text(
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                text = text
            )
        }
    }
}

@Composable
private fun SezioneTitolo(
    testo: String,
    numero: Int,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = testo,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(accentColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$numero",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

/**
 * Badge circolare con l'icona della pillola. È l'elemento visivo ricorrente
 * di tutta la schermata: compare in primo piano nella card della prossima
 * assunzione e, più piccolo, in ogni riga delle altre sezioni.
 */
@Composable
private fun BadgePillola(
    modifier: Modifier = Modifier,
    dimensione: Dp = 44.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .size(dimensione)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(dimensione * 0.6f),
            painter = painterResource(R.drawable.pill),
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Card in primo piano. Sia "Assumi" (nella finestra) sia "Assumi in anticipo"
 * (dopo conferma nel dialog) passano da `eseguiConferma`, che mostra il
 * checkmark prima di notificare la conferma vera e propria.
 *
 * Gestisce anche gli stati particolari: nessuna assunzione prevista oggi, e
 * tutte le assunzioni di oggi già completate.
 */
@Composable
private fun ProssimaAssunzioneCard(
    prossima: AssunzioneGiornaliera?,
    puoAssumereOra: Boolean,
    assunteOggi: Int,
    totaliOggi: Int,
    onConferma: (AssunzioneGiornaliera) -> Unit,
    onConfermaPuntuale: (AssunzioneGiornaliera) -> Unit,
    onAggiungiClick: () -> Unit,
    medicinaleExist: Boolean,
    soloAssunzioniInRitardoRimaste: Boolean,
    prossimaInRitardo: AssunzioneGiornaliera?,
    minutiAllaProssima: Long?,
    minutiRitardo: Long?
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDialogPuntuale by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun eseguiConferma(assunzione: AssunzioneGiornaliera, registra: (AssunzioneGiornaliera) -> Unit) {
        isConfirming = true
        scope.launch {
            delay(DURATA_CHECK_MS.milliseconds)
            registra(assunzione)
            delay(200.milliseconds)
            isConfirming = false
        }
    }

    val tuttoCompletato = prossima == null && totaliOggi > 0 && assunteOggi == totaliOggi
    val nessunaAssunzioneOggi = totaliOggi == 0

    val colorePrimarioCard by animateColorAsState(
        targetValue = when {
            prossima != null -> MaterialTheme.colorScheme.primaryContainer
            soloAssunzioniInRitardoRimaste -> MaterialTheme.colorScheme.errorContainer
            tuttoCompletato -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "colore_primario_card"
    )
    val coloreAccentoCard by animateColorAsState(
        targetValue = when {
            prossima != null -> MaterialTheme.colorScheme.tertiaryContainer
            soloAssunzioniInRitardoRimaste -> MaterialTheme.colorScheme.error
            tuttoCompletato -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "colore_accento_card"
    )
    val fattoreMiscelaAccento = when {
        prossima != null -> 0.55f
        soloAssunzioniInRitardoRimaste -> 0.22f
        tuttoCompletato -> 0.22f
        else -> 0.55f
    }
    val contentColor = when {
        prossima != null -> MaterialTheme.colorScheme.onPrimaryContainer
        soloAssunzioniInRitardoRimaste -> MaterialTheme.colorScheme.onErrorContainer
        tuttoCompletato -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // remember: senza, questi Brush vengono riallocati a ogni ricomposizione
    // della card (es. quando cambia solo isConfirming), anche se i colori da
    // cui dipendono sono rimasti gli stessi.
    val brushSfondoCard = remember(colorePrimarioCard, coloreAccentoCard, fattoreMiscelaAccento) {
        Brush.linearGradient(
            listOf(colorePrimarioCard, lerp(colorePrimarioCard, coloreAccentoCard, fattoreMiscelaAccento))
        )
    }
    val brushBordoCard = remember(contentColor) {
        Brush.verticalGradient(listOf(contentColor.copy(alpha = 0.25f), Color.Transparent))
    }
    val brushBloomCard = remember(coloreAccentoCard) {
        Brush.radialGradient(listOf(coloreAccentoCard.copy(alpha = 0.15f), Color.Transparent))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (prossima != null || soloAssunzioniInRitardoRimaste) 6.dp else 0.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = colorePrimarioCard
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
      Box(
          modifier = Modifier
              .background(brushSfondoCard)
              .border(
                  width = 1.dp,
                  brush = brushBordoCard,
                  shape = RoundedCornerShape(28.dp)
              )
      ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .blur(radius = 40.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(
                    brushBloomCard,
                    shape = CircleShape
                )
        )

        AnimatedContent(
            targetState = when {
                prossima != null -> "prossima"
                soloAssunzioniInRitardoRimaste -> "ritardo"
                tuttoCompletato -> "completato"
                else -> "vuoto"
            },
            label = "stato_card",
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }) togetherWith
                        fadeOut(tween(150))
            }
        ) { stato ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (stato) {
                    "prossima" -> if (prossima != null) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconaPillolaCerchiata(
                                        background = MaterialTheme.colorScheme.primary,
                                        icona = Icons.Rounded.AccountCircle,
                                        dimensione = 40.dp
                                    )
                                    EtichettaStato(
                                        testo = "PROSSIMA ASSUNZIONE",
                                        color = contentColor.copy(alpha = 0.7f)
                                    )
                                }
                                ChipCountdown(
                                    testo = if (puoAssumereOra) "Ora" else "tra ${formattaDurata(minutiAllaProssima ?: 0)}",
                                    evidenziato = puoAssumereOra,
                                    contentColor = contentColor
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = prossima.nomeMedicinale,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (!prossima.dosaggio.isNullOrBlank()) {
                                    Text(
                                        text = prossima.dosaggio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = contentColor.copy(alpha = 0.75f)
                                    )
                                }
                                Text(
                                    text = prossima.assunzionePrevista.orarioPrevisto.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            AnimatedContent(targetState = isConfirming, label = "pulsante_assumi") { inCorso ->
                                if (inCorso) {
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CheckmarkConfermato()
                                    }
                                } else {
                                    Box(Modifier.fillMaxWidth()) {
                                        Button(
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp),
                                            colors = ButtonColors(
                                                containerColor = contentColor,
                                                contentColor = colorePrimarioCard,
                                                disabledContainerColor = contentColor,
                                                disabledContentColor = colorePrimarioCard
                                            ),
                                            onClick = {
                                                if (puoAssumereOra) {
                                                    eseguiConferma(prossima, onConferma)
                                                } else {
                                                    showDialog = true
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (puoAssumereOra) Icons.Rounded.Check else Icons.Rounded.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = if (puoAssumereOra) "Assumi" else "Assumi in anticipo",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        EffettoShimmerPulsante(colore = colorePrimarioCard)
                                    }
                                }
                            }
                        }
                    }

                    "ritardo" -> if (prossimaInRitardo != null) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconaPillolaCerchiata(
                                        background = MaterialTheme.colorScheme.error,
                                        icona = Icons.Rounded.Warning,
                                        dimensione = 32.dp
                                    )
                                    EtichettaStato(
                                        testo = "IN RITARDO",
                                        color = contentColor.copy(alpha = 0.85f)
                                    )
                                }
                                ChipCountdown(
                                    testo = "in ritardo da ${formattaDurata(minutiRitardo ?: 0)}",
                                    evidenziato = true,
                                    contentColor = contentColor
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            Text(
                                text = prossimaInRitardo.nomeMedicinale,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (!prossimaInRitardo.dosaggio.isNullOrBlank()) {
                                    Text(
                                        text = prossimaInRitardo.dosaggio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = contentColor.copy(alpha = 0.75f)
                                    )
                                }
                                Text(
                                    text = prossimaInRitardo.assunzionePrevista.orarioPrevisto.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            AnimatedContent(targetState = isConfirming, label = "pulsante_assumi") { inCorso ->
                                if (inCorso) {
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CheckmarkConfermato()
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showDialogPuntuale = true }) {
                                            Text("Presa puntualmente")
                                        }
                                        Box {
                                            Button(
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier.heightIn(min = 48.dp),
                                                onClick = { eseguiConferma(prossimaInRitardo, onConferma) },
                                                colors = ButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError,
                                                    disabledContainerColor = MaterialTheme.colorScheme.error,
                                                    disabledContentColor = MaterialTheme.colorScheme.onError
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = "Assumi in ritardo",
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            EffettoShimmerPulsante(colore = MaterialTheme.colorScheme.onError)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "completato" -> {
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(id = R.drawable.listcompleted),
                            contentDescription = "Lista medicine completate"
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tutto fatto per oggi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hai assunto $assunteOggi/$totaliOggi medicine previste",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }

                    else -> { // vuoto / nessuna assunzione oggi
                        Text(
                            text = "Nessuna assunzione in programma",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (medicinaleExist) "Nessuno tra i tuoi medicinali attivi è programmato per oggi"
                            else "Abilita o aggiungi un medicinale per iniziare a tenere traccia delle tue assunzioni",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Image(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(R.drawable.no_task),
                            contentDescription = null,
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
      }
    }

    if (showDialog && prossima != null) {
        ConfermaAnticipoDialog(
            nomeMedicinale = prossima.nomeMedicinale,
            onConferma = {
                showDialog = false
                eseguiConferma(prossima, onConferma)
            },
            onAnnulla = { showDialog = false }
        )
    }

    if (showDialogPuntuale && prossimaInRitardo != null) {
        ConfermaPresaPuntualeDialog(
            nomeMedicinale = prossimaInRitardo.nomeMedicinale,
            orarioPrevisto = prossimaInRitardo.assunzionePrevista.orarioPrevisto,
            onConferma = {
                showDialogPuntuale = false
                eseguiConferma(prossimaInRitardo, onConfermaPuntuale)
            },
            onAnnulla = { showDialogPuntuale = false }
        )
    }
}

@Composable
private fun IconaPillolaCerchiata(
    background: Color,
    icona: ImageVector,
    pulsante: Boolean = false,
    dimensione: Dp = 56.dp
) {
    val transizione = rememberInfiniteTransition(label = "badge_animato")

    val scalaRespiro by transizione.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scala_respiro"
    )

    val progressoAnello by transizione.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progresso_anello"
    )

    Box(contentAlignment = Alignment.Center) {
        if (pulsante) {
            Box(
                modifier = Modifier
                    .size(dimensione)
                    .scale(1f + progressoAnello * 0.5f)
                    .clip(CircleShape)
                    .background(background.copy(alpha = (1f - progressoAnello) * 0.5f))
            )
        }
        Box(
            modifier = Modifier
                .size(dimensione)
                .scale(scalaRespiro)
                .clip(CircleShape)
                .background(background.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.padding(5.dp).size(26.dp),
                painter = painterResource(R.drawable.pill),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Badge circolare statico (nessuna animazione), usato nelle card di lista
 * ("in ritardo"/"prossime assunzioni") per dare un accento colorato senza
 * competere con il badge animato della hero card.
 */
@Composable
private fun BadgeIconaLista(
    background: Color,
    dimensione: Dp = 40.dp,
    contenuto: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(dimensione)
            .clip(CircleShape)
            .background(background.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
        content = contenuto
    )
}

/**
 * Fascia di luce che attraversa in loop il pulsante sovrastante, per
 * segnalare che è pronto per essere premuto. Si clippa da sola alla stessa
 * forma a pillola del `Button`, indipendentemente dai suoi modifier interni.
 */
@Composable
private fun BoxScope.EffettoShimmerPulsante(colore: Color) {
    val transizione = rememberInfiniteTransition(label = "shimmer_pulsante")
    val avanzamento by transizione.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avanzamento_shimmer"
    )
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(50))
            .drawWithContent {
                val centro = avanzamento * size.width
                val larghezzaFascia = size.width * 0.3f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colore.copy(alpha = 0f),
                            colore.copy(alpha = 0.35f),
                            colore.copy(alpha = 0f)
                        ),
                        start = Offset(centro - larghezzaFascia, 0f),
                        end = Offset(centro + larghezzaFascia, 0f)
                    )
                )
            }
    )
}

@Composable
private fun EtichettaStato(testo: String, color: Color) {
    Text(
        text = testo,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = color
    )
}

/**
 * Chip del countdown nell'intestazione della hero card: pulsa leggermente
 * quando `evidenziato` è vero, cioè quando rappresenta un invito ad agire
 * ("Ora" per la prossima assunzione, sempre per quella in ritardo).
 */
@Composable
private fun ChipCountdown(testo: String, evidenziato: Boolean, contentColor: Color) {
    val transizione = rememberInfiniteTransition(label = "chip_countdown")
    val scala by transizione.animateFloat(
        initialValue = 1f,
        targetValue = if (evidenziato) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scala_chip"
    )
    Box(
        modifier = Modifier
            .scale(scala)
            .clip(RoundedCornerShape(50))
            .background(contentColor.copy(alpha = if (evidenziato) 0.22f else 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = testo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun CheckmarkConfermato() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Assunta",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun ConfermaAnticipoDialog(
    nomeMedicinale: String,
    onConferma: () -> Unit,
    onAnnulla: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text("Assumere in anticipo?") },
        text = {
            Text("Non è ancora l'orario previsto per $nomeMedicinale. Vuoi assumerla comunque adesso?")
        },
        confirmButton = {
            TextButton(onClick = onConferma) { Text("Assumi in anticipo") }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) { Text("Annulla") }
        }
    )
}

@Composable
private fun ConfermaPresaPuntualeDialog(
    nomeMedicinale: String,
    orarioPrevisto: LocalTime,
    onConferma: () -> Unit,
    onAnnulla: () -> Unit
) {
    val orarioFormattato = remember(orarioPrevisto) {
        orarioPrevisto.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text("Confermare l'assunzione puntuale?") },
        text = {
            Text("$nomeMedicinale verrà segnata come assunta alle $orarioFormattato, l'orario previsto, invece che ora.")
        },
        confirmButton = {
            TextButton(onClick = onConferma) { Text("Conferma") }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) { Text("Annulla") }
        }
    )
}

/**
 * Sequenza condivisa da "in ritardo" e "prossime assunzioni":
 * checkmark -> collasso della card -> solo allora l'`onConferma` reale.
 * La card resta composta (stessa `key` nella LazyColumn) finché l'uscita
 * non è finita, quindi la rimozione dalla lista vera non causa scatti.
 * `onConferma` è passato ad `avviaConferma` invece che fisso, così più
 * pulsanti della stessa card possono condividere l'animazione pur portando
 * ciascuno a un'azione finale diversa (es. "Assumi in ritardo" vs "Presa
 * puntualmente").
 */
@Composable
private fun CardConAnimazioneAssunzione(
    content: @Composable (isConfirming: Boolean, avviaConferma: (onConferma: () -> Unit) -> Unit) -> Unit
) {
    var isConfirming by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically(animationSpec = tween(DURATA_COLLASSO_MS)) +
                fadeOut(animationSpec = tween(DURATA_COLLASSO_MS / 2))
    ) {
        content(isConfirming) { onConferma ->
            // Protegge da doppio tap: se l'animazione è già partita, ignora
            // ulteriori richieste di conferma finché non si conclude.
            if (!isConfirming) {
                isConfirming = true
                scope.launch {
                    delay(DURATA_CHECK_MS.milliseconds)
                    visible = false
                    delay(DURATA_COLLASSO_MS.toLong().milliseconds)
                    onConferma()
                }
            }
        }
    }
}

@Composable
private fun AssunzioneRitardataCard(
    assunzione: AssunzioneGiornaliera,
    onConfermaRitardo: (AssunzioneGiornaliera) -> Unit,
    onConfermaPuntuale: (AssunzioneGiornaliera) -> Unit
) {
    var mostraDialogPuntuale by remember { mutableStateOf(false) }

    CardConAnimazioneAssunzione { isConfirming, avviaConferma ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = MaterialTheme.colorScheme.errorContainer
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeIconaLista(background = MaterialTheme.colorScheme.error) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = assunzione.nomeMedicinale,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedContent(targetState = isConfirming, label = "assumi_in_ritardo") { inCorso ->
                    if (inCorso) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            CheckmarkConfermato()
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            TextButton(onClick = { mostraDialogPuntuale = true }) {
                                Text("Presa puntualmente")
                            }
                            // Niente dialog qui: la medicina è già saltata, la conferma
                            // dell'orario è già passata, non serve chiedere ulteriore conferma.
                            OutlinedButton(
                                onClick = { avviaConferma { onConfermaRitardo(assunzione) } },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Assumi in ritardo")
                            }
                        }
                    }
                }
            }
        }

        if (mostraDialogPuntuale) {
            ConfermaPresaPuntualeDialog(
                nomeMedicinale = assunzione.nomeMedicinale,
                orarioPrevisto = assunzione.assunzionePrevista.orarioPrevisto,
                onConferma = {
                    mostraDialogPuntuale = false
                    avviaConferma { onConfermaPuntuale(assunzione) }
                },
                onAnnulla = { mostraDialogPuntuale = false }
            )
        }
    }
}

@Composable
private fun ProssimaInCodaCard(
    assunzione: AssunzioneGiornaliera,
    onConferma: (AssunzioneGiornaliera) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    CardConAnimazioneAssunzione { isConfirming, avviaConferma ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = MaterialTheme.colorScheme.secondaryContainer
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
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
                    BadgeIconaLista(background = MaterialTheme.colorScheme.primary) {
                        Image(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.pill),
                            contentDescription = null,
                            contentScale = ContentScale.Fit
                        )
                    }
                    Column {
                        Text(
                            text = assunzione.nomeMedicinale,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = buildString {
                                if (!assunzione.dosaggio.isNullOrBlank()) append("${assunzione.dosaggio} · ")
                                append(
                                    assunzione.assunzionePrevista.orarioPrevisto.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    )
                                )
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                AnimatedContent(targetState = isConfirming, label = "assumi_in_anticipo") { inCorso ->
                    if (inCorso) {
                        CheckmarkConfermato()
                    } else {
                        OutlinedButton(onClick = { showDialog = true }, shape = RoundedCornerShape(50)) {
                            Text("Assumi in anticipo")
                        }
                    }
                }
            }
        }

        if (showDialog) {
            ConfermaAnticipoDialog(
                nomeMedicinale = assunzione.nomeMedicinale,
                onConferma = {
                    showDialog = false
                    avviaConferma { onConferma(assunzione) }
                },
                onAnnulla = { showDialog = false }
            )
        }
    }
}

// --- Preview: dati di prova ---

private fun assunzioneDiProva(
    id: Int,
    nome: String,
    orario: LocalTime,
    dosaggio: String? = "1 compressa",
    stato: StatoAssunzione = StatoAssunzione.IN_ATTESA
) = AssunzioneGiornaliera(
    assunzionePrevista = AssunzionePrevista(
        id = id,
        idOrarioAssunzione = id,
        data = LocalDate.now(),
        orarioPrevisto = orario,
        stato = stato
    ),
    assunzioneEffettuata = null,
    nomeMedicinale = nome,
    dosaggio = dosaggio
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    AppTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides remember { SnackbarHostState() }) {
            Home(
                uiState = HomeUiState(
                    prossimaAssunzione = assunzioneDiProva(1, "Omeprazolo", LocalTime.now().plusMinutes(2)),
                    puoAssumereOra = true,
                    inRitardo = listOf(
                        assunzioneDiProva(2, "Aspirina", LocalTime.now().minusHours(2), dosaggio = "100mg")
                    ),
                    prossimeAssunzioni = listOf(
                        assunzioneDiProva(3, "Vitamina D", LocalTime.now().plusHours(3), dosaggio = "1 goccia"),
                        assunzioneDiProva(4, "FaringelPlus", LocalTime.now().plusHours(6), dosaggio = "500mg")
                    ),
                    assunteOggi = 1,
                    totaliOggi = 4,
                    isLoading = false
                ),
                onAssumiClick = {},
                onAssumiPuntualeClick = {},
                onAnnullaClick = {},
                onAddMedicineButtonClick = {},
                onUserSettingsButtonClick = {},
                onFrequenzaButtonClick = {},
                onMedicinaliButtonClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenInRitardoPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            CompositionLocalProvider(LocalSnackbarHostState provides remember { SnackbarHostState() }) {
                Home(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    uiState = HomeUiState(
                        prossimaAssunzione = null,
                        puoAssumereOra = true,
                        prossimaInRitardo = assunzioneDiProva(1, "Faringel", LocalTime.now().minusHours(5), "1 bustina"),
                        inRitardo = listOf(
                            assunzioneDiProva(2, "Aspirina", LocalTime.now().minusHours(2), dosaggio = "100mg"),
                            assunzioneDiProva(3, "Tachipirina", LocalTime.now().minusHours(2), dosaggio = "100mg"),
                            assunzioneDiProva(4, "Omeprazolo", LocalTime.now().minusHours(2), dosaggio = "100mg")
                        ),
                        prossimeAssunzioni = emptyList(),
                        assunteOggi = 1,
                        totaliOggi = 4,
                        isLoading = false
                    ),
                    onAssumiClick = {},
                onAssumiPuntualeClick = {},
                    onAnnullaClick = {},
                    onAddMedicineButtonClick = {},
                    onUserSettingsButtonClick = {},
                    onFrequenzaButtonClick = {},
                    onMedicinaliButtonClick = {}
                )
            }
        }
    }
}

@Preview(name = "Home nessun medicinale esistente o attivo",showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenVuotaPreview() {
    AppTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides remember { SnackbarHostState() }) {
            Home(
                uiState = HomeUiState(isLoading = false),
                onAssumiClick = {},
                onAssumiPuntualeClick = {},
                onAnnullaClick = {},
                onAddMedicineButtonClick = {},
                onUserSettingsButtonClick = {},
                onFrequenzaButtonClick = {},
                onMedicinaliButtonClick = {}
            )
        }
    }
}

@Preview(name = "Home medicinale non attivo", showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenConMedicinaleNonAttivoPreview() {
    AppTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides remember { SnackbarHostState() }) {
            Home(
                uiState = HomeUiState(isLoading = false, esisteAlmenoUnMedicinale = true),
                onAssumiClick = {},
                onAssumiPuntualeClick = {},
                onAnnullaClick = {},
                onAddMedicineButtonClick = {},
                onUserSettingsButtonClick = {},
                onFrequenzaButtonClick = {},
                onMedicinaliButtonClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenCompletataPreview() {
    AppTheme {
        CompositionLocalProvider(LocalSnackbarHostState provides remember { SnackbarHostState() }) {
            Home(
                uiState = HomeUiState(
                    assunteOggi = 3,
                    totaliOggi = 3,
                    isLoading = false
                ),
                onAssumiClick = {},
                onAssumiPuntualeClick = {},
                onAnnullaClick = {},
                onAddMedicineButtonClick = {},
                onUserSettingsButtonClick = {},
                onFrequenzaButtonClick = {},
                onMedicinaliButtonClick = {}
            )
        }
    }
}

@Preview
@Composable
fun AzioneRapidaPreview() {
    AppTheme {
        AzioneRapida(
            icon = Icons.Default.Add,
            text = "Aggiungi",
            onClick = {}
        )
    }
}