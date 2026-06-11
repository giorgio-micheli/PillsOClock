package micheli.giorgio.pillsoclock.ui.home

import android.graphics.Paint
import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import micheli.giorgio.pillsoclock.R

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    Home(
        modifier = modifier,
        "venerdi 24 aprile",
        "Omeprazolo"
    )
}

@Composable
fun Home(
    modifier: Modifier = Modifier,
    title: String,
    currentMedicine: String
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentMedicine,
                        style = MaterialTheme.typography.headlineMedium,
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
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Chip(
                    icon = Icons.Default.DateRange,
                    text = "Frequenza"
                )
                Chip(
                    icon = Icons.Default.Send,
                    text = "Storico"
                )
                Chip(
                    icon = Icons.Default.Add,
                    text = "Aggiungi"
                )
            }
        }

        item {
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
        }

        item {
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
        }

        item {
            Spacer(
                Modifier.height(10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "OGGI - 0/3 ASSUNTE",
                    fontWeight = FontWeight.Bold
                )
                // "OGGI - 0/3 assunte
            }
        }

        items(3) {
            MedicineCard(
                modifier = Modifier.fillMaxWidth()
            )
            // Lista medicine
        }

    }
}

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    text: String = "Undefined"
) {
    Card(
        modifier = modifier,
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
    modifier: Modifier = Modifier
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
                        text = "Omeprazolo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "08:00 - Mancata",
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
    MedicineCard()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    Scaffold { innerPadding ->
        Home(
            modifier = Modifier.padding(innerPadding),
            "venerdi 24 aprile",
            "Omeprazolo"
        )
    }
}

@Preview
@Composable
fun ChipPreview() {
    Chip()
}