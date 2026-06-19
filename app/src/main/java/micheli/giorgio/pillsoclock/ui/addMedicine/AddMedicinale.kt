package micheli.giorgio.pillsoclock.ui.addMedicine

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlin.collections.forEachIndexed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicinaleBottomSheet(
    onDismissRequest: () -> Unit,
    onSaveAndExit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        MedicinaleFields(
            onSaveAndExit = onSaveAndExit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinaleFields(
    onSaveAndExit: () -> Unit
) {

    var nome by remember { mutableStateOf("") }
    var dosaggio by remember { mutableStateOf("") }

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.now().hour,
        initialMinute = LocalTime.now().minute,
        is24Hour = true
    )

    val options = listOf(
        "Bassa",
        "Media",
        "Alta"
    )
    var selected by remember { mutableIntStateOf(1) }

    if (showTimePicker) {
        TimePickerDialog(
            timePickerState = timePickerState,
            onDismiss = {
                showTimePicker = false
            },
            onConfirm = {
                selectedTime = Pair(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp, horizontal = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aggiungi medicinale",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = nome,
            onValueChange = {
                nome = it
            },
            label = { Text("Nome") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = dosaggio,
            onValueChange = {
                dosaggio = it
            },
            label = { Text("Dosaggio") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedTime?.let { (h, m) -> "Ora selezionata: %02d:%02d".format(h,m) }
                    ?: "Nessuna ora selezionata",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        SegmentedSlider(
            options,
            selected,
            onOptionSelected = {
                selected = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            ) {
                Text(
                    text = "Salva"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicinaleFieldsPreview() {
    MedicinaleFields(
        onSaveAndExit = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleziona l'ora") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Orologio circolare
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            Row {
//                // Pulsante per alternare modalità Dial ↔ Input
//                TextButton(onClick = onSwitchMode) {
//                    Text(
//                        if (dialogType == DialogType.Dial)
//                            "Tastiera" else "Orologio"
//                    )
//                }
                TextButton(onClick = onDismiss) { Text("Annulla") }
            }
        }
    )
}

/**
 * Slider orizzontale segmentato con 3+ opzioni.
 *
 * @param options       Lista di opzioni da mostrare.
 * @param selectedIndex Indice dell'opzione attualmente selezionata.
 * @param onOptionSelected Callback chiamata quando l'utente cambia selezione.
 * @param modifier      Modifier opzionale per il contenitore esterno.
 */
@Composable
fun SegmentedSlider(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Larghezza totale del track in Dp (calcolata al layout)
    var trackWidthDp by remember { mutableStateOf(0.dp) }

    val trackPadding = 4.dp

    // Larghezza di ogni segmento
    val innerWidth: Dp = trackWidthDp - (trackPadding * 2)
    val segmentWidth: Dp = if (options.isNotEmpty()) innerWidth / options.size else 0.dp

    // Offset animato del thumb
    val thumbOffset by animateDpAsState(
        targetValue = trackPadding + (segmentWidth * selectedIndex),
        animationSpec = tween(durationMillis = 220),
        label = "thumbOffset"
    )

    val trackShape = RoundedCornerShape(12.dp)
    val thumbShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(trackShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onGloballyPositioned { coords ->
                trackWidthDp = with(density) { coords.size.width.toDp() }
            }
            .padding(vertical = trackPadding)
    ) {
        // ── Thumb animato ──────────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segmentWidth)
                .fillMaxHeight(),
            shape = thumbShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            tonalElevation = 0.dp
        ) {}

        // ── Label dei segmenti ─────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null          // nessun ripple: lo thumb fa da feedback
                        ) { onOptionSelected(index) }
                ) {
                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

