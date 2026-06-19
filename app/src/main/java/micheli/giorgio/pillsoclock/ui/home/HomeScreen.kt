package micheli.giorgio.pillsoclock.ui.home

import android.graphics.Paint
import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.R
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.domain.model.AssunzioneGiornaliera
import micheli.giorgio.pillsoclock.domain.model.AssunzionePrevista
import micheli.giorgio.pillsoclock.ui.addMedicine.AddMedicinaleBottomSheet
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
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
        uiState.assunzioni
    )
}

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    errorMessage: String
) {
    Surface(
        modifier = modifier,
        color = Color.White
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    assunzioniGiornaliere: List<AssunzioneGiornaliera>
) {

    var showAddDialog by rememberSaveable { mutableStateOf(false)}

    if (showAddDialog) {
        AddMedicinaleBottomSheet(
            onDismissRequest = {
                showAddDialog = false
            },
            onSaveAndExit = {}
        )
    }

    Column(
        modifier = modifier.padding(horizontal = 10.dp).padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                val today = LocalDate.now()

                Text(
                    text = "${today.dayOfWeek.name} ${today.dayOfMonth} ${today.month.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                    .padding(8.dp),
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip(
                icon = Icons.Default.DateRange,
                text = "Frequenza",
                onClick = {}
            )
            Chip(
                icon = Icons.Default.Send,
                text = "Storico",
                onClick = {}
            )
            Chip(
                icon = Icons.Default.Add,
                text = "Aggiungi",
                onClick = {
                    showAddDialog = true
                }
            )
        }

        Spacer(
            Modifier.height(20.dp)
        )
        Image(
            modifier = Modifier.width(150.dp).height(150.dp),
            alignment = Alignment.Center,
            painter = painterResource(R.drawable.pill),
            contentDescription = "Pill's image",
            contentScale = ContentScale.Crop
        )
        Spacer(
            Modifier.height(20.dp)
        )

        // Pulsante conferma
        Button(
            onClick = {}
        ) {
            Text(
                modifier = Modifier.padding(
                    vertical = 4.dp,
                    horizontal = 32.dp
                ),
                text = "Conferma assunzione",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            Modifier.height(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "OGGI - 0/${assunzioniGiornaliere.size} ASSUNTE",
                fontWeight = FontWeight.Bold
            )
            // "OGGI - 0/3 assunte
        }

        if (assunzioniGiornaliere.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nessuna assunzione in programma"
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(assunzioniGiornaliere) { assunzioneGiornaliera ->
                    // Lista medicine
                    MedicineCard(
                        modifier = Modifier.fillMaxWidth(),
                        assunzioneGiornaliera
                    )
                }
            }
        }
    }
}

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    text: String = "Undefined",
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = icon,
                contentDescription = ""
            )
            Text(
                style = MaterialTheme.typography.labelMedium,
                text = text
            )
        }
    }
}

@Composable
fun MedicineCard(
    modifier: Modifier = Modifier,
    assunzioneGiornaliera: AssunzioneGiornaliera
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                vertical = 16.dp,
                horizontal = 8.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp),
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "clock"
                )
                Column() {
                    Text(
                        text = assunzioneGiornaliera.nomeMedicinale,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${assunzioneGiornaliera.assunzionePrevista.orarioPrevisto.format(
                            DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 6.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Prendi",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Preview
@Composable
fun MedicineCardPreview() {
    MedicineCard(
        assunzioneGiornaliera = AssunzioneGiornaliera(
            AssunzionePrevista(
                1,
                2,
                LocalDate.now(),
                LocalTime.now(),
                StatoAssunzione.IN_ATTESA
            ),
            null,
            "Omeprazolo",
            "1 compressa"
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Scaffold { innerPadding ->
        Home(
            modifier = Modifier.padding(innerPadding),
            emptyList()
        )
    }
}

@Preview
@Composable
fun ChipPreview() {
    Chip(
        icon = Icons.Default.Info,
        text = "Aggiungi",
        onClick = {}
    )
}