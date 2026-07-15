package micheli.giorgio.pillsoclock.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
TODO: aggiungere pulsate "ho assunto il medicinale all'orario corretto ma mi sono dimenticato
    di confermarlo sull'app" per i medicinali segnati come "in ritardo".
 */

//TODO: sistemare posizione snackbar che collide con il FAB nella homeScreen

/*
TODO: cambiare destinazione per la "modifica medicinale", far apparire la schermata in un
    single pane classico invece che nel bottom sheet dialog. Sistemare di conseguenza anche il fatto
    che spunta la topbar della homeScreen quando si cerca di modificare un medicinale. Creare una
    nuova route per la modifica del medicinale.
 */

/*
TODO: bug quando non ho nessun medicinale e ne aggiungo uno che però ha orari ormai già passati
 */

/*
TODO: Il FAB a volte smette di ricevere il click
 */

/*
TODO: Quando elimino definitivamente un medicinale, deve rimanere comunque lo storico nel calendario
    quindi mi sa che dobbiamo fare una eliminazione fake con un flag "eliminato" sul database.
 */

/*
TODO: se non ho nessuna assunzione in programma per oggi, devo cambiare il messaggio sulla homescreen,
    non deve dire di aggiungere un medicinale per iniziare, il medicinale magari c'è già, solamente
    oggi non è tra i giorni prefissati per assumerlo. Devo controllare se esiste già almeno un medicinale.
 */

/*
TODO: nella schermata "Medicinali" non si capisce che premendo una volta sola su un medicinale
  si apre la schermata per editarlo
 */

/*
TODO: implementare datastore
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
            assunzioneRepository = app.assunzioneRepository,
            utenteRepository = app.utenteRepository
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
    onAnnullaClick: (AssunzionePrevista) -> Unit,
    onAddMedicineButtonClick: () -> Unit,
    onUserSettingsButtonClick: () -> Unit,
    onFrequenzaButtonClick: () -> Unit,
    onMedicinaliButtonClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Punto unico di registrazione: registra l'assunzione e mostra lo
    // Snackbar con l'azione di annullamento. Tutte le card passano da qui.
    val confermaAssunzione: (AssunzioneGiornaliera) -> Unit = { assunzione ->
        // Questa funzione è fuori dalla coroutine, quindi viene eseguita sul main thread immediatamente
        onAssumiClick(assunzione.assunzionePrevista)
        scope.launch {
            // showSnackBar è una suspend function, quindi in certi punti sospenderà la coroutine
            // ma lascerà comunque il thread libero di fare altro, non sta di fatto bloccando il thread
            val risultato = snackbarHostState.showSnackbar(
                message = "${assunzione.nomeMedicinale} segnata come assunta",
                actionLabel = "Annulla",
                duration = SnackbarDuration.Short
            )
            if (risultato == SnackbarResult.ActionPerformed) {
                onAnnullaClick(assunzione.assunzionePrevista)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
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
                        onAggiungiClick = onAddMedicineButtonClick
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
                        AssunzioneRitardataCard(assunzione, confermaAssunzione)
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
}

/**
 * Intestazione della home: saluto contestuale in base all'ora del giorno,
 * data corrente in italiano, e accesso rapido alle impostazioni utente.
 */
@Composable
fun IntestazioneHome(
    onUserSettingsButtonClick: () -> Unit
) {
    val oggi = remember { LocalDate.now() }
    val ora = remember { LocalTime.now() }
    val formatterData = remember { DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN) }
    val dataFormattata = remember(oggi) {
        oggi.format(formatterData).replaceFirstChar { it.uppercase() }
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
                text = saluto(ora),
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
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.secondaryContainer),
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
                text = "Aderenza di oggi",
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
            text = "$testo ($numero)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
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
    onAggiungiClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var isConfirming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun eseguiConferma(assunzione: AssunzioneGiornaliera) {
        isConfirming = true
        scope.launch {
            delay(DURATA_CHECK_MS.milliseconds)
            onConferma(assunzione)
            delay(200.milliseconds)
            isConfirming = false
        }
    }

    val tuttoCompletato = prossima == null && totaliOggi > 0 && assunteOggi == totaliOggi
    val nessunaAssunzioneOggi = totaliOggi == 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                prossima != null -> MaterialTheme.colorScheme.primaryContainer
                tuttoCompletato -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                prossima != null -> MaterialTheme.colorScheme.onPrimaryContainer
                tuttoCompletato -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when {
                prossima != null -> {
                    BadgePillola(containerColor = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "PROSSIMA ASSUNZIONE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = prossima.nomeMedicinale,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (!prossima.dosaggio.isNullOrBlank()) {
                        Text(
                            text = prossima.dosaggio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = prossima.assunzionePrevista.orarioPrevisto.format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    AnimatedContent(targetState = isConfirming, label = "pulsante_assumi") { inCorso ->
                        if (inCorso) {
                            CheckmarkConfermato()
                        } else {
                            Button(
                                shape = RoundedCornerShape(50),
                                onClick = {
                                    if (puoAssumereOra) {
                                        eseguiConferma(prossima)
                                    } else {
                                        showDialog = true
                                    }
                                }
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 24.dp),
                                    text = if (puoAssumereOra) "Assumi" else "Assumi in anticipo",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                tuttoCompletato -> {
//                    Icon(
//                        imageVector = Icons.Default.CheckCircle,
//                        contentDescription = null,
//                        modifier = Modifier.size(40.dp)
//                    )
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
                        textAlign = TextAlign.Center
                    )
                }

                nessunaAssunzioneOggi -> {
                    Text(
                        text = "Nessuna assunzione in programma",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aggiungi un medicinale per iniziare a tenere traccia delle tue assunzioni",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
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

    if (showDialog && prossima != null) {
        ConfermaAnticipoDialog(
            nomeMedicinale = prossima.nomeMedicinale,
            onConferma = {
                showDialog = false
                eseguiConferma(prossima)
            },
            onAnnulla = { showDialog = false }
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

/**
 * Sequenza condivisa da "in ritardo" e "prossime assunzioni":
 * checkmark -> collasso della card -> solo allora `onConferma` reale.
 * La card resta composta (stessa `key` nella LazyColumn) finché l'uscita
 * non è finita, quindi la rimozione dalla lista vera non causa scatti.
 */
@Composable
private fun CardConAnimazioneAssunzione(
    onConferma: () -> Unit,
    content: @Composable (isConfirming: Boolean, avviaConferma: () -> Unit) -> Unit
) {
    var isConfirming by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = visible,
        exit = shrinkVertically(animationSpec = tween(DURATA_COLLASSO_MS)) +
                fadeOut(animationSpec = tween(DURATA_COLLASSO_MS / 2))
    ) {
        content(isConfirming) {
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
    onConferma: (AssunzioneGiornaliera) -> Unit
) {
    CardConAnimazioneAssunzione(onConferma = { onConferma(assunzione) }) { isConfirming, avviaConferma ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
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
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
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
                AnimatedContent(targetState = isConfirming, label = "assumi_in_ritardo") { inCorso ->
                    if (inCorso) {
                        CheckmarkConfermato()
                    } else {
                        // Niente dialog qui: la medicina è già saltata, la conferma
                        // dell'orario è già passata, non serve chiedere ulteriore conferma.
                        OutlinedButton(onClick = avviaConferma, shape = RoundedCornerShape(50)) {
                            Text("Assumi in ritardo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProssimaInCodaCard(
    assunzione: AssunzioneGiornaliera,
    onConferma: (AssunzioneGiornaliera) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    CardConAnimazioneAssunzione(onConferma = { onConferma(assunzione) }) { isConfirming, avviaConferma ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = assunzione.assunzionePrevista.orarioPrevisto.format(
                                DateTimeFormatter.ofPattern("HH:mm")
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
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
                    avviaConferma()
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
        Home(
            uiState = HomeUiState(
                prossimaAssunzione = assunzioneDiProva(1, "Omeprazolo", LocalTime.now().plusMinutes(2)),
                puoAssumereOra = true,
                inRitardo = listOf(
                    assunzioneDiProva(2, "Cardioaspirina", LocalTime.now().minusHours(2), dosaggio = "100mg")
                ),
                prossimeAssunzioni = listOf(
                    assunzioneDiProva(3, "Vitamina D", LocalTime.now().plusHours(3), dosaggio = "1 goccia"),
                    assunzioneDiProva(4, "Metformina", LocalTime.now().plusHours(6), dosaggio = "500mg")
                ),
                assunteOggi = 1,
                totaliOggi = 4,
                isLoading = false
            ),
            onAssumiClick = {},
            onAnnullaClick = {},
            onAddMedicineButtonClick = {},
            onUserSettingsButtonClick = {},
            onFrequenzaButtonClick = {},
            onMedicinaliButtonClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenVuotaPreview() {
    AppTheme {
        Home(
            uiState = HomeUiState(isLoading = false),
            onAssumiClick = {},
            onAnnullaClick = {},
            onAddMedicineButtonClick = {},
            onUserSettingsButtonClick = {},
            onFrequenzaButtonClick = {},
            onMedicinaliButtonClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenCompletataPreview() {
    AppTheme {
        Home(
            uiState = HomeUiState(
                assunteOggi = 3,
                totaliOggi = 3,
                isLoading = false
            ),
            onAssumiClick = {},
            onAnnullaClick = {},
            onAddMedicineButtonClick = {},
            onUserSettingsButtonClick = {},
            onFrequenzaButtonClick = {},
            onMedicinaliButtonClick = {}
        )
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