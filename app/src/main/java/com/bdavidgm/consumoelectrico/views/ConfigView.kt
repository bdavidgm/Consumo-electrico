package com.bdavidgm.consumoelectrico.views

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bdavidgm.consumoelectrico.datastore.SettingsUiState
import com.bdavidgm.consumoelectrico.datastore.ViewMode
import com.bdavidgm.consumoelectrico.viewmodels.SettingsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
)
 {
    val uiState by viewModel.uiState.collectAsState()
    val newEmailInput by viewModel.newEmailInput.collectAsState()
    val senderEmailInput by viewModel.senderEmailInput.collectAsState()
    val senderPasswordInput by viewModel.senderPasswordInput.collectAsState()

    val snackbarHostState = remember{ SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Manejar Snackbars
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Reportes") }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SettingsContent(
                uiState = uiState,
                newEmailInput = newEmailInput,
                senderEmailInput = senderEmailInput,
                senderPasswordInput = senderPasswordInput,
                onNewEmailInputChanged = viewModel::onNewEmailInputChanged,
                onSenderEmailInputChanged = viewModel::onSenderEmailInputChanged,
                onSenderPasswordInputChanged = viewModel::onSenderPasswordInputChanged,
                onAddReportEmail = viewModel::addReportEmail,
                onRemoveReportEmail = viewModel::removeReportEmail,
                onSaveSenderCredentials = viewModel::saveSenderCredentials,
                onViewModeSelected = viewModel::setViewMode,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    newEmailInput: String,
    senderEmailInput: String,
    senderPasswordInput: String,
    onNewEmailInputChanged: (String) -> Unit,
    onSenderEmailInputChanged: (String) -> Unit,
    onSenderPasswordInputChanged: (String) -> Unit,
    onAddReportEmail: () -> Unit,
    onRemoveReportEmail: (String) -> Unit,
    onSaveSenderCredentials: () -> Unit,
    onViewModeSelected: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Sección: Emails para Reportes
        Text(
            text = "Emails para Reportes",
            style = MaterialTheme.typography.headlineSmall
        )

        // Input para nuevo email
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newEmailInput,
                onValueChange = onNewEmailInputChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nuevo email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onAddReportEmail() }
                ),
                singleLine = true
            )

            Button(
                onClick = onAddReportEmail,
                enabled = newEmailInput.isNotEmpty()
            ) {
                Text("Añadir")
            }
        }

        // Contador de emails
        Text(
            text = "Emails registrados: ${uiState.reportEmails.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Lista de emails
        if (uiState.reportEmails.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.reportEmails.toList()) { email ->
                    EmailItem(
                        email = email,
                        onRemove = { onRemoveReportEmail(email) }
                    )
                }
            }
        } else {
            Text(
                text = "No hay emails agregados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Divider()

        // Sección: Credenciales del Remitente
        Text(
            text = "Credenciales del Remitente",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = senderEmailInput,
            onValueChange = onSenderEmailInputChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Email del remitente") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = senderPasswordInput,
            onValueChange = onSenderPasswordInputChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Contraseña (se guardará encriptada)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Button(
            onClick = onSaveSenderCredentials,
            modifier = Modifier.align(Alignment.End),
            enabled = senderEmailInput.isNotEmpty() && senderPasswordInput.isNotEmpty()
        ) {
            Text("Guardar Credenciales")
        }

        Divider()

        // Sección: Modo de Visualización
        Text(
            text = "Modo de Visualización",
            style = MaterialTheme.typography.headlineSmall
        )

        ViewModeSelector(
            selectedMode = uiState.viewMode,
            onModeSelected = onViewModeSelected
        )

        // Descripción del modo seleccionado
        Text(
            text = when (uiState.viewMode) {
                ViewMode.MONTHLY -> "Mostrando datos del mes actual"
                ViewMode.YEARLY -> "Mostrando datos del año actual"
                ViewMode.ALL_TIME -> "Mostrando todos los datos históricos"
                ViewMode.WEEKLY -> TODO()
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ViewModeSelector(
    selectedMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ViewModeOption(
            mode = ViewMode.MONTHLY,
            title = "Vista Mensual",
            description = "Datos del mes actual",
            isSelected = selectedMode == ViewMode.MONTHLY,
            onSelected = onModeSelected
        )

        ViewModeOption(
            mode = ViewMode.YEARLY,
            title = "Vista Anual",
            description = "Datos del año actual",
            isSelected = selectedMode == ViewMode.YEARLY,
            onSelected = onModeSelected
        )

        ViewModeOption(
            mode = ViewMode.ALL_TIME,
            title = "Vista Completa",
            description = "Todos los datos históricos",
            isSelected = selectedMode == ViewMode.ALL_TIME,
            onSelected = onModeSelected
        )
    }
}

@Composable
private fun ViewModeOption(
    mode: ViewMode,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelected: (ViewMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(mode) },
        elevation = if (isSelected) CardDefaults.cardElevation(defaultElevation = 4.dp)
        else CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onSelected(mode) }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmailItem(
    email: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar email",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
