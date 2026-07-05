package micheli.giorgio.pillsoclock.ui.addMedicine

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nav3recipes.bottomsheet.LocalBottomSheetDismiss
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.data.local.entity.TipoFrequenza
import micheli.giorgio.pillsoclock.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ---------------------------------------------------------------------------
// Costanti di design
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Entry point: BottomSheet
// ---------------------------------------------------------------------------

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AggiungiMedicinaleBottomSheet(
//    onDismiss: () -> Unit,
//    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//) {
//
//    val context = LocalContext.current
//    val app = context.applicationContext as PillsOClockApp
//    val dismiss = LocalBottomSheetDismiss.current
//
//    val addMedicinaleViewModel: AddMedicinaleViewModel = viewModel(
//        factory = AddMedicinaleViewModelFactory(
//            app.medicinaleRepository,
//            app.utenteRepository
//        )
//    )
//
//    val uiState by addMedicinaleViewModel.uiState.collectAsStateWithLifecycle()
//
//    addMedicinaleViewModel.reset()
//
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        sheetState = sheetState,
//        containerColor = MaterialTheme.colorScheme.background,
//        dragHandle = {
//            Box(
//                modifier = Modifier
//                    .padding(top = 12.dp, bottom = 4.dp)
//                    .size(width = 36.dp, height = 4.dp)
//                    .clip(CircleShape)
//                    .background(Color.White)
//            )
//        },
//        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
//    ) {
//        AggiungiMedicinaleContent(
//            uiState = uiState,
//            onNomeChange = addMedicinaleViewModel::onNomeChange,
//            onDosaggioChange = addMedicinaleViewModel::onDosaggioChange,
//            onNoteChange = addMedicinaleViewModel::onNoteChange,
//            onOrarioAggiunto = addMedicinaleViewModel::onOrarioAggiunto,
//            onOrarioRimosso = addMedicinaleViewModel::onOrarioRimosso,
//            onTipoFrequenzaChange = addMedicinaleViewModel::onTipoFrequenzaChange,
//            onIntervalloGiorniChange = addMedicinaleViewModel::onIntervalloGiorniChange,
//            onGiornoSettimanaToggle = addMedicinaleViewModel::onGiornoSettimanaToggle,
//            onDataInizioChange = addMedicinaleViewModel::onDataInizioChange,
//            onDataFineChange = addMedicinaleViewModel::onDataFineChange,
//            onSalvaClick = addMedicinaleViewModel::onSave,
//            onDismiss = dismiss
//        )
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicinale() {

    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp
    val dismiss = LocalBottomSheetDismiss.current

    val addMedicinaleViewModel: AddMedicinaleViewModel = viewModel(
        factory = AddMedicinaleViewModelFactory(
            app.medicinaleRepository,
            app.utenteRepository
        )
    )

    val uiState by addMedicinaleViewModel.uiState.collectAsStateWithLifecycle()

    AggiungiMedicinaleContent(
        uiState = uiState,
        onNomeChange = addMedicinaleViewModel::onNomeChange,
        onDosaggioChange = addMedicinaleViewModel::onDosaggioChange,
        onNoteChange = addMedicinaleViewModel::onNoteChange,
        onOrarioAggiunto = addMedicinaleViewModel::onOrarioAggiunto,
        onOrarioRimosso = addMedicinaleViewModel::onOrarioRimosso,
        onTipoFrequenzaChange = addMedicinaleViewModel::onTipoFrequenzaChange,
        onIntervalloGiorniChange = addMedicinaleViewModel::onIntervalloGiorniChange,
        onGiornoSettimanaToggle = addMedicinaleViewModel::onGiornoSettimanaToggle,
        onDataInizioChange = addMedicinaleViewModel::onDataInizioChange,
        onDataFineChange = addMedicinaleViewModel::onDataFineChange,
        onSalvaClick = addMedicinaleViewModel::onSave,
        onDismiss = dismiss
    )
}


// ---------------------------------------------------------------------------
// Contenuto principale (scrollabile)
// ---------------------------------------------------------------------------

@Composable
private fun AggiungiMedicinaleContent(
    uiState: AggiungiMedicinaleUiState,
    onNomeChange: (String) -> Unit,
    onDosaggioChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onOrarioAggiunto: (LocalTime) -> Unit,
    onOrarioRimosso: (LocalTime) -> Unit,
    onTipoFrequenzaChange: (TipoFrequenza) -> Unit,
    onIntervalloGiorniChange: (Int) -> Unit,
    onGiornoSettimanaToggle: (Int) -> Unit,
    onDataInizioChange: (LocalDate) -> Unit,
    onDataFineChange: (LocalDate?) -> Unit,
    onSalvaClick: () -> Boolean,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header fisso: fuori dal Column scrollabile, resta sempre visibile
        SheetHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            onDismiss = onDismiss
        )

        // Tutto il resto scrolla in questo Column interno
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sezione info medicinale
            SezioneCard(titolo = "Medicinale") {
                CampoTestoModerno(
                    valore = uiState.nome,
                    onValueChange = onNomeChange,
                    label = "Nome *",
                    placeholder = "es. Tachipirina",
                    errore = uiState.nomeError,
                    messaggioErrore = "Il nome è obbligatorio",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (uiState.nomeError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                CampoTestoModerno(
                    valore = uiState.dosaggio,
                    onValueChange = onDosaggioChange,
                    label = "Dosaggio",
                    placeholder = "es. 500mg"
                )
                CampoTestoModerno(
                    valore = uiState.note,
                    onValueChange = onNoteChange,
                    label = "Note",
                    placeholder = "es. Da prendere a stomaco pieno",
                    righeMax = 3
                )
            }

            // Sezione orari
            SezioneCard(titolo = "Orari di assunzione") {
                SezioneOrari(
                    orari = uiState.orari,
                    onOrarioAggiunto = onOrarioAggiunto,
                    onOrarioRimosso = onOrarioRimosso,
                    errore = uiState.orariError
                )
            }

            SezioneCard(titolo = "Frequenza") {
                SegmentedSelectorFrequenza(
                    selezionato = uiState.tipoFrequenza,
                    onSelezionato = onTipoFrequenzaChange
                )

                AnimatedContent(
                    targetState = uiState.tipoFrequenza,
                    transitionSpec = {
                        (fadeIn() + expandVertically()) togetherWith
                                (fadeOut() + shrinkVertically()) using
                                SizeTransform(clip = false)
                    },
                    label = "frequenzaContent"
                ) { tipo ->
                    when (tipo) {
                        TipoFrequenza.OGNI_N_GIORNI -> SezioneIntervalloGiorni(
                            intervallo = uiState.intervalloGiorni,
                            onIntervalloChange = onIntervalloGiorniChange
                        )
                        TipoFrequenza.GIORNI_SETTIMANA -> SezioneGiorniSettimana(
                            giorniSelezionati = uiState.giorniSettimana,
                            onGiornoToggle = onGiornoSettimanaToggle,
                            errore = uiState.giorniSettimanaError
                        )
                        else -> Unit // nessun contenuto extra per altri tipi, se esistono
                    }
                }
            }

            // Sezione date
            SezioneCard(titolo = "Durata terapia") {
                SezioneDatePicker(
                    dataInizio = uiState.dataInizio,
                    dataFine = uiState.dataFine,
                    onDataInizioChange = onDataInizioChange,
                    onDataFineChange = onDataFineChange
                )
            }

            // Pulsante salva
            Button(
                onClick = {
                    val saved = onSalvaClick()
                    if (saved) onDismiss()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Salva medicinale",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            // Messaggio di errore globale
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onError,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Intestazione sheet
// ---------------------------------------------------------------------------

@Composable
private fun SheetHeader(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                "Nuovo medicinale",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                "Compila i dettagli per aggiungere un promemoria",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(
            onClick = dropUnlessResumed {
                onDismiss()
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Chiudi",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Card sezione
// ---------------------------------------------------------------------------

@Composable
private fun SezioneCard(
    titolo: String,
    contenuto: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            titolo.uppercase(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = contenuto
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Campo testo moderno
// ---------------------------------------------------------------------------

@Composable
private fun CampoTestoModerno(
    valore: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    errore: Boolean = false,
    messaggioErrore: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    righeMax: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valore,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp) },
            leadingIcon = leadingIcon,
            isError = errore,
            maxLines = righeMax,
            minLines = if (righeMax > 1) 2 else 1,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
                errorLabelColor = MaterialTheme.colorScheme.onError,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        if (errore && messaggioErrore.isNotEmpty()) {
            Text(
                messaggioErrore,
                color = MaterialTheme.colorScheme.onError,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sezione orari
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SezioneOrari(
    orari: List<LocalTime>,
    onOrarioAggiunto: (LocalTime) -> Unit,
    onOrarioRimosso: (LocalTime) -> Unit,
    errore: Boolean
) {
    var mostraTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    if (mostraTimePicker) {
        AlertDialog(
            onDismissRequest = { mostraTimePicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Scegli orario", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surface,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.primary,
                        selectorColor = MaterialTheme.colorScheme.primary,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onOrarioAggiunto(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    mostraTimePicker = false
                }) {
                    Text("Aggiungi", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostraTimePicker = false }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (orari.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = if (errore) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (errore) "Aggiungi almeno un orario" else "Nessun orario aggiunto",
                    color = if (errore) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orari.sortedBy { it }) { orario ->
                    ChipOrario(
                        orario = orario,
                        onRimuovi = { onOrarioRimosso(orario) }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { mostraTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Aggiungi orario", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ChipOrario(orario: LocalTime, onRimuovi: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            orario.format(formatter),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            Icons.Default.Close,
            contentDescription = "Rimuovi",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .size(14.dp)
                .clickable { onRimuovi() }
        )
    }
}

// ---------------------------------------------------------------------------
// Segmented selector frequenza custom
// ---------------------------------------------------------------------------

@Composable
private fun SegmentedSelectorFrequenza(
    selezionato: TipoFrequenza,
    onSelezionato: (TipoFrequenza) -> Unit
) {
    val opzioni = listOf(
        TipoFrequenza.GIORNALIERA to "Ogni giorno",
        TipoFrequenza.OGNI_N_GIORNI to "Intervallo",
        TipoFrequenza.GIORNI_SETTIMANA to "Giorni"
    )

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val segmentWidthDp = with(density) {
        if (containerWidthPx > 0) (containerWidthPx / opzioni.size).toDp() else 0.dp
    }
    val selectedIndex = opzioni.indexOfFirst { it.first == selezionato }

    val indicatorOffset by animateDpAsState(
        targetValue = segmentWidthDp * selectedIndex,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "indicatorOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(4.dp)
            .onGloballyPositioned { containerWidthPx = it.size.width }
    ) {
        // Pillola animata di sfondo
        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            opzioni.forEach { (tipo, etichetta) ->
                val isSelected = selezionato == tipo
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    label = "segmentContent"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { onSelezionato(tipo) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        etichetta,
                        color = contentColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sezione intervallo giorni
// ---------------------------------------------------------------------------

@Composable
private fun SezioneIntervalloGiorni(
    intervallo: Int,
    onIntervalloChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ogni quanti giorni?",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { if (intervallo > 2) onIntervalloChange(intervallo - 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ) {
                    Text("−", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Light)
                }
                Text(
                    "$intervallo",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 28.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { if (intervallo < 30) onIntervalloChange(intervallo + 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ) {
                    Text("+", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Light)
                }
            }
        }
        Slider(
            value = intervallo.toFloat(),
            onValueChange = { onIntervalloChange(it.toInt()) },
            valueRange = 2f..30f,
            steps = 27,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("2 giorni", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
            Text("30 giorni", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Sezione giorni della settimana
// ---------------------------------------------------------------------------

@Composable
private fun SezioneGiorniSettimana(
    giorniSelezionati: List<Int>,
    onGiornoToggle: (Int) -> Unit,
    errore: Boolean
) {
    val giorni = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "G", 5 to "V", 6 to "S", 7 to "D")

    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Seleziona i giorni",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            giorni.forEach { (numero, etichetta) ->
                val isSelected = giorniSelezionati.contains(numero)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else if (errore) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                errore -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                        .clickable { onGiornoToggle(numero) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        etichetta,
                        color = if (isSelected) Color.White else if (errore) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        if (errore) {
            Text(
                "Seleziona almeno un giorno",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sezione date
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SezioneDatePicker(
    dataInizio: LocalDate,
    dataFine: LocalDate?,
    onDataInizioChange: (LocalDate) -> Unit,
    onDataFineChange: (LocalDate?) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    var mostraPickerInizio by remember { mutableStateOf(false) }
    var mostraPickerFine by remember { mutableStateOf(false) }
    var dataFineAbilitata by remember { mutableStateOf(dataFine != null) }

    // DatePicker dialogs
    if (mostraPickerInizio) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = dataInizio.toEpochDay() * 86400000L
        )
        DatePickerDialog(
            onDismissRequest = { mostraPickerInizio = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDataInizioChange(LocalDate.ofEpochDay(millis / 86400000L))
                    }
                    mostraPickerInizio = false
                }) { Text("Conferma", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { mostraPickerInizio = false }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.onSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(state = state)
        }
    }

    if (mostraPickerFine) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (dataFine ?: dataInizio.plusDays(30))
                .toEpochDay() * 86400000L
        )
        DatePickerDialog(
            onDismissRequest = { mostraPickerFine = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDataFineChange(LocalDate.ofEpochDay(millis / 86400000L))
                    }
                    mostraPickerFine = false
                }) { Text("Conferma", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { mostraPickerFine = false }) {
                    Text("Annulla", color = MaterialTheme.colorScheme.onSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(state = state)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Data inizio
        RowData(
            etichetta = "Inizio terapia",
            valore = dataInizio.format(formatter),
            onClick = { mostraPickerInizio = true }
        )

        // Toggle data fine
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fine terapia", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
            Switch(
                checked = dataFineAbilitata,
                onCheckedChange = {
                    dataFineAbilitata = it
                    if (!it) onDataFineChange(null)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        AnimatedVisibility(
            visible = dataFineAbilitata,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            RowData(
                etichetta = "Data fine",
                valore = dataFine?.format(formatter) ?: "Seleziona",
                onClick = { mostraPickerFine = true }
            )
        }
    }
}

@Composable
private fun RowData(etichetta: String, valore: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etichetta, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(valore, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Preview(
    name = "Light bottom sheet",
    showBackground = true
)
@Composable
private fun LightBottomSheetPreview() {
    AppTheme {
            AggiungiMedicinaleContent(
                uiState = AggiungiMedicinaleUiState(
                    tipoFrequenza = TipoFrequenza.OGNI_N_GIORNI
                ),
                onNomeChange = {},
                onDosaggioChange = {},
                onNoteChange = {},
                onOrarioAggiunto = {},
                onOrarioRimosso = {},
                onTipoFrequenzaChange = {},
                onIntervalloGiorniChange = {},
                onGiornoSettimanaToggle = {},
                onDataInizioChange = {},
                onDataFineChange = {},
                onSalvaClick = { true },
                onDismiss = {}
            )
    }
}

@Preview(
    name = "Dark bottom sheet",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun DarkBottomSheetPreview() {
    AppTheme {
        AggiungiMedicinaleContent(
            uiState = AggiungiMedicinaleUiState(),
            onNomeChange = {},
            onDosaggioChange = {},
            onNoteChange = {},
            onOrarioAggiunto = {},
            onOrarioRimosso = {},
            onTipoFrequenzaChange = {},
            onIntervalloGiorniChange = {},
            onGiornoSettimanaToggle = {},
            onDataInizioChange = {},
            onDataFineChange = {},
            onSalvaClick = { true },
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Con errori di validazione")
@Composable
private fun AggiungiMedicinaleBottomSheetErroriPreview() {
    AppTheme {
            AggiungiMedicinaleContent(
                uiState = AggiungiMedicinaleUiState(
                    nomeError = true,
                    orariError = true
                ),
                onNomeChange = {},
                onDosaggioChange = {},
                onNoteChange = {},
                onOrarioAggiunto = {},
                onOrarioRimosso = {},
                onTipoFrequenzaChange = {},
                onIntervalloGiorniChange = {},
                onGiornoSettimanaToggle = {},
                onDataInizioChange = {},
                onDataFineChange = {},
                onSalvaClick = { true },
                onDismiss = {}
            )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B2E, name = "Frequenza — Intervallo giorni")
@Composable
private fun AggiungiMedicinaleBottomSheetIntervalloPreview() {
    MaterialTheme {
        Surface(color = Color(0xFF1C1B2E)) {
            AggiungiMedicinaleContent(
                uiState = AggiungiMedicinaleUiState(
                    nome = "Aspirina",
                    dosaggio = "100mg",
                    orari = listOf(LocalTime.of(8, 0), LocalTime.of(21, 0)),
                    tipoFrequenza = TipoFrequenza.OGNI_N_GIORNI,
                    intervalloGiorni = 3
                ),
                onNomeChange = {},
                onDosaggioChange = {},
                onNoteChange = {},
                onOrarioAggiunto = {},
                onOrarioRimosso = {},
                onTipoFrequenzaChange = {},
                onIntervalloGiorniChange = {},
                onGiornoSettimanaToggle = {},
                onDataInizioChange = {},
                onDataFineChange = {},
                onSalvaClick = { true },
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B2E, name = "Frequenza — Giorni settimana")
@Composable
private fun AggiungiMedicinaleBottomSheetGiorniPreview() {
    MaterialTheme {
        Surface(color = Color(0xFF1C1B2E)) {
            AggiungiMedicinaleContent(
                uiState = AggiungiMedicinaleUiState(
                    nome = "Tachipirina",
                    dosaggio = "500mg",
                    note = "Da prendere a stomaco pieno",
                    orari = listOf(LocalTime.of(8, 0), LocalTime.of(14, 0), LocalTime.of(21, 0)),
                    tipoFrequenza = TipoFrequenza.GIORNI_SETTIMANA,
                    giorniSettimana = listOf(1, 3, 5),
                    dataFine = LocalDate.now().plusMonths(1)
                ),
                onNomeChange = {},
                onDosaggioChange = {},
                onNoteChange = {},
                onOrarioAggiunto = {},
                onOrarioRimosso = {},
                onTipoFrequenzaChange = {},
                onIntervalloGiorniChange = {},
                onGiornoSettimanaToggle = {},
                onDataInizioChange = {},
                onDataFineChange = {},
                onSalvaClick = { true },
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B2E, name = "Caricamento in corso")
@Composable
private fun AggiungiMedicinaleBottomSheetLoadingPreview() {
    MaterialTheme {
        Surface(color = Color(0xFF1C1B2E)) {
            AggiungiMedicinaleContent(
                uiState = AggiungiMedicinaleUiState(
                    nome = "Tachipirina",
                    dosaggio = "500mg",
                    orari = listOf(LocalTime.of(8, 0)),
                    isLoading = true,
                ),
                onNomeChange = {},
                onDosaggioChange = {},
                onNoteChange = {},
                onOrarioAggiunto = {},
                onOrarioRimosso = {},
                onTipoFrequenzaChange = {},
                onIntervalloGiorniChange = {},
                onGiornoSettimanaToggle = {},
                onDataInizioChange = {},
                onDataFineChange = {},
                onSalvaClick = { true },
                onDismiss = {}
            )
        }
    }
}