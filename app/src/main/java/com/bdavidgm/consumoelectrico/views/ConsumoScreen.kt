package com.bdavidgm.consumoelectrico.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings // <-- NUEVO ICONO
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import com.bdavidgm.consumoelectrico.viewmodels.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConsumoScreen(
    consumoViewModel: ConsumoViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit // <-- NUEVO PARÁMETRO
) {
    var lecturaInput by remember { mutableStateOf("") }
    var showMonth by remember { mutableStateOf(true) }
    var showHelp by remember { mutableStateOf(false) }

    val columnas by consumoViewModel.columns.collectAsState()
    val errorMessage by consumoViewModel.errorMessage.collectAsState()
    val successMessage by consumoViewModel.successMessage.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Obtener fecha actual
    val fechaActual = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaFormateada = fechaActual.format(formatter)

    // Mostrar snackbars para mensajes
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            // Podrías usar un Snackbar aquí
        }
    }

    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            // Podrías usar un Snackbar aquí
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 4.dp,top = 42.dp,bottom=32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header compacto CON BOTÓN DE CONFIGURACIÓN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Título a la izquierda
            Text(
                text = "📊 Consumo Eléctrico",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Botones a la derecha: Configuración y Ayuda
            Row {
                // Botón de configuración (NUEVO)
                IconButton(
                    onClick = onNavigateToSettings, // <-- Navega a Settings
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Configuración",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Botón de ayuda (existente)
                IconButton(
                    onClick = { showHelp = !showHelp },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ayuda",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title = { Text("Cómo funciona") },
                text = {
                    Column {
                        Text("1. Ingresa la lectura ACTUAL del contador")
                        Text("El sistema calcula:")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Consumo = Lectura actual - Lectura anterior")
                        Text("• Acumulado = Suma de consumos del mes")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelp = false }) {
                        Text("Entendido")
                    }
                }
            )
        }

        // Tarjeta de entrada
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Fila de fecha y selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fecha: $fechaFormateada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row {
                        FilterChip(
                            onClick = {
                                showMonth = true
                                consumoViewModel.cargarConsumosDelMes()
                            },
                            label = { Text("Mes", fontSize = 12.sp) },
                            selected = showMonth
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            onClick = {
                                showMonth = false
                                consumoViewModel.cargarConsumosDelAnio()
                            },
                            label = { Text("Año", fontSize = 12.sp) },
                            selected = !showMonth
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de entrada
                OutlinedTextField(
                    value = lecturaInput,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            lecturaInput = it
                        }
                    },
                    label = { Text("Lectura (kWh)", fontSize = 12.sp) },
                    placeholder = { Text("12345.67", fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage ?: "", fontSize = 10.sp)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (lecturaInput.isBlank()) {
                                consumoViewModel.limpiarMensajes()
                                // Mostrar error
                                return@Button
                            }

                            coroutineScope.launch {
                                try {
                                    val lectura = lecturaInput.toDouble()
                                    if (lectura < 0) {
                                        // Mostrar error
                                        return@launch
                                    }

                                    val exito = consumoViewModel.registrarLectura(lectura)
                                    if (exito) {
                                        lecturaInput = ""
                                    }
                                } catch (e: NumberFormatException) {
                                    // Mostrar error
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            consumoViewModel.enviarReportePorCorreo(context)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reporte", fontSize = 12.sp)
                    }
                }
            }
        }

        // Tabla con 4 columnas optimizada
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Encabezado de 4 columnas optimizado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    // Fecha (25%)
                    Text(
                        text = "Fecha",
                        modifier = Modifier.weight(0.25f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )

                    // Lectura (25%)
                    Text(
                        text = "Lectura",
                        modifier = Modifier.weight(0.25f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )

                    // Consumo (25%)
                    Text(
                        text = "Consumo",
                        modifier = Modifier.weight(0.25f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )

                    // Acumulado (25%)
                    Text(
                        text = if (showMonth) "Acumulado" else "Acum.Anual",
                        modifier = Modifier.weight(0.25f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }

                // Contenido de la tabla
                if (columnas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📈",
                                fontSize = 32.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "No hay registros",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Registra tu primera lectura",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(columnas) { fila ->
                            val isEven = columnas.indexOf(fila) % 2 == 0
                            val backgroundColor = if (isEven) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(vertical = 6.dp)
                            ) {
                                // Fecha (alineada izquierda)
                                Text(
                                    text = fila.getOrNull(0) ?: "--",
                                    modifier = Modifier.weight(0.25f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )

                                // Lectura (alineada derecha)
                                Text(
                                    text = fila.getOrNull(1) ?: "0.00",
                                    modifier = Modifier.weight(0.25f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )

                                // Consumo (alineada derecha)
                                Text(
                                    text = fila.getOrNull(2) ?: "0.00",
                                    modifier = Modifier.weight(0.25f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )

                                // Acumulado (alineada derecha con peso)
                                Text(
                                    text = fila.getOrNull(3) ?: "0.00",
                                    modifier = Modifier.weight(0.25f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Total acumulado
                val totalAcumulado = columnas.lastOrNull()?.getOrNull(3) ?: "0.00"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TOTAL:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$totalAcumulado kWh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Indicador
        Text(
            text = "↓ Desliza para ver más registros",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}