package com.bdavidgm.consumoelectrico.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import com.bdavidgm.consumoelectrico.viewmodels.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumoScreen(
    consumoViewModel: ConsumoViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Estados locales
    var lecturaInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showMonth by remember { mutableStateOf(true) }

    // Obtener datos del ViewModel
    val columnas by consumoViewModel.columns.collectAsState()

    // Efecto para cargar datos iniciales
    LaunchedEffect(key1 = Unit) {
        consumoViewModel.GetThisMonth()
    }

    // Obtener fecha actual para mostrar
    val fechaActual = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaFormateada = fechaActual.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Registro de Consumo Eléctrico",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Sección de entrada de datos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Información de fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fecha: $fechaFormateada",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Selector de vista (Mes/Año)
                    Row {
                        FilterChip(
                            selected = showMonth,
                            onClick = {
                                showMonth = true
                                consumoViewModel.GetThisMonth()
                            },
                            label = { Text("Este Mes") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !showMonth,
                            onClick = {
                                showMonth = false
                                consumoViewModel.GetThisYear()
                            },
                            label = { Text("Este Año") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para ingresar lectura
                OutlinedTextField(
                    value = lecturaInput,
                    onValueChange = {
                        // Solo permitir números y punto decimal
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            lecturaInput = it
                            errorMessage = ""
                        }
                    },
                    label = { Text("Lectura del contador (kWh)") },
                    placeholder = { Text("Ej: 12345.67") },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    supportingText = {
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Ingrese la lectura actual del contador")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            // Validar entrada
                            if (lecturaInput.isBlank()) {
                                errorMessage = "Por favor ingrese una lectura"
                                return@Button
                            }

                            try {
                                val lectura = lecturaInput.toDouble()
                                if (lectura < 0) {
                                    errorMessage = "La lectura no puede ser negativa"
                                    return@Button
                                }

                                // Aquí se llamaría a la función para guardar la lectura
                                // consumoViewModel.guardarLectura(lectura)

                                // Limpiar campo
                                lecturaInput = ""
                                errorMessage = ""

                                // Mostrar mensaje de éxito
                                // (En una implementación real, mostrarías un Snackbar)

                            } catch (e: NumberFormatException) {
                                errorMessage = "Por favor ingrese un número válido"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Lectura")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón para enviar reporte (se conectará después)
                    Button(
                        onClick = {
                            // Aquí se llamaría a la función para enviar reporte
                            // settingsViewModel.enviarReporte(columnas)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar Reporte")
                    }
                }
            }
        }

        // Tabla de consumo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Encabezado de la tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    // Fecha
                    TableCell(
                        text = "Fecha",
                        weight = 0.35f,
                        isHeader = true
                    )

                    // Consumo Diario
                    TableCell(
                        text = "Consumo Diario (kWh)",
                        weight = 0.35f,
                        isHeader = true
                    )

                    // Acumulado Mensual
                    TableCell(
                        text = if (showMonth) "Acumulado Mensual" else "Acumulado Anual",
                        weight = 0.3f,
                        isHeader = true
                    )
                }

                // Lista de registros
                if (columnas.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No hay registros",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Registra tu primera lectura arriba",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(columnas) { fila ->
                            TableRow(
                                fecha = fila.getOrNull(0) ?: "",
                                consumoDiario = fila.getOrNull(1) ?: "0.00",
                                acumulado = fila.getOrNull(2) ?: "0.00",
                                isEven = columnas.indexOf(fila) % 2 == 0
                            )
                        }
                    }
                }

                // Total acumulado
                val totalAcumulado = columnas.lastOrNull()?.getOrNull(2) ?: "0.00"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TOTAL:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = "$totalAcumulado kWh",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Información adicional
        Text(
            text = "Los valores se calculan automáticamente",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Componente para celdas de la tabla
@Composable
fun TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier= Modifier
            .padding(8.dp),
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        fontSize = if (isHeader) 14.sp else 13.sp,
        textAlign = TextAlign.Center,
        color = if (isHeader) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface
    )
}

// Componente para filas de la tabla
@Composable
fun TableRow(
    fecha: String,
    consumoDiario: String,
    acumulado: String,
    isEven: Boolean
) {
    val backgroundColor = if (isEven) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        // Fecha
        TableCell(
            text = fecha,
            weight = 0.35f
        )

        // Consumo Diario
        TableCell(
            text = consumoDiario,
            weight = 0.35f
        )

        // Acumulado
        TableCell(
            text = acumulado,
            weight = 0.3f
        )
    }
}
/*
// Preview de la pantalla
@Preview(showBackground = true)
@Composable
fun ConsumoScreenPreview() {
    MaterialTheme {
        ConsumoScreen()
    }
}*/