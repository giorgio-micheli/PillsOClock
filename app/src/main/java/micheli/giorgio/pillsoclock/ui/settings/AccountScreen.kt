package micheli.giorgio.pillsoclock.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.LocalSnackbarHostState
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.R
import micheli.giorgio.pillsoclock.ui.home.LoadingScreen
import micheli.giorgio.pillsoclock.ui.theme.AppTheme

@Composable
fun AccountScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as PillsOClockApp

    val accountViewModel: AccountViewModel = viewModel(
        factory = AccountViewModelFactory(app.utenteRepository)
    )

    val uiState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    // Risolto qui (contesto composable): onSalvaClick è una lambda () -> Unit
    // normale, eseguita al click e non durante la composizione, quindi non
    // potrebbe chiamare stringResource() al suo interno.
    val messaggioDatiSalvati = stringResource(R.string.account_snackbar_dati_salvati)

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    Account(
        uiState = uiState,
        onNomeChange = accountViewModel::onNomeChange,
        onCognomeChange = accountViewModel::onCognomeChange,
        onEmailChange = accountViewModel::onEmailChange,
        onSalvaClick = {
            accountViewModel.onSalvaClick()
            scope.launch {
                snackbarHostState.showSnackbar(messaggioDatiSalvati)
            }
        }
    )
}

@Composable
fun Account(
    uiState: AccountUiState,
    onNomeChange: (String) -> Unit,
    onCognomeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSalvaClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.nome,
            onValueChange = onNomeChange,
            label = { Text(stringResource(R.string.account_campo_nome)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.cognome,
            onValueChange = onCognomeChange,
            label = { Text(stringResource(R.string.account_campo_cognome)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.account_campo_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSalvaClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.account_bottone_salva))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AccountScreenPreview() {
    AppTheme {
        Account(
            uiState = AccountUiState(
                nome = "Giorgio",
                cognome = "Micheli",
                email = "giorgio@example.com",
                isLoading = false
            ),
            onNomeChange = {},
            onCognomeChange = {},
            onEmailChange = {},
            onSalvaClick = {}
        )
    }
}
