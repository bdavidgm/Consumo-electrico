package com.bdavidgm.consumoelectrico.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

enum class ChartType {
    BARRAS,
    LINEAS
}

enum class PeriodType {
    SEMANAL,
    MENSUAL,
    ANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    consumoViewModel: ConsumoViewModel,
    onNavigateBack: () -> Unit
) {
    var chartType by remember { mutableStateOf(ChartType.BARRAS) }
    var periodType by remember { mutableStateOf(PeriodType.MENSUAL) }
    var chartData by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun cargarDatosGrafico() {
        coroutineScope.launch(Dispatchers.IO) {
            chartData = consumoViewModel.obtenerDatosParaGrafico(periodType.name)
        }
    }

    LaunchedEffect(periodType) {
        cargarDatosGrafico()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gráficos de Consumo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de tipo de gráfico
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tipo de Gráfico",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = chartType == ChartType.BARRAS,
                            onClick = { chartType = ChartType.BARRAS },
                            label = { Text("Barras") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = chartType == ChartType.LINEAS,
                            onClick = { chartType = ChartType.LINEAS },
                            label = { Text("Líneas") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Selector de período
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Período",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = periodType == PeriodType.SEMANAL,
                            onClick = { periodType = PeriodType.SEMANAL },
                            label = {
                                Text(
                                    "Semanal",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = periodType == PeriodType.MENSUAL,
                            onClick = { periodType = PeriodType.MENSUAL },
                            label = {
                                Text(
                                    "Mensual",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = periodType == PeriodType.ANUAL,
                            onClick = { periodType = PeriodType.ANUAL },
                            label = {
                                Text(
                                    "Anual",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Datos de prueba para gráficos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Datos de prueba",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Genera 3 años de lecturas diarias simuladas para probar los gráficos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val msg = consumoViewModel.generarDatosDePrueba3Anios()
                                snackbarHostState.showSnackbar(msg)
                                cargarDatosGrafico()
                            }
                        }
                    ) {
                        Text("Cargar datos de prueba (3 años)")
                    }
                }
            }

            // Gráfico
            if (chartData.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay datos para mostrar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    when (chartType) {
                        ChartType.BARRAS -> VicoBarChart(data = chartData)
                        ChartType.LINEAS -> VicoLineChart(data = chartData)
                    }
                }
            }
        }
    }
}

@Composable
fun VicoBarChart(data: List<Pair<String, Double>>) {
    if (data.isEmpty()) return

    val labels = data.map { it.first }
    val chartModel = remember(data) {
        val entries = data.mapIndexed { index, pair ->
            FloatEntry(x = index.toFloat(), y = pair.second.toFloat())
        }
        entryModelOf(entries)
    }

    Chart(
        chart = columnChart(),
        model = chartModel,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = AxisValueFormatter { value, _ ->
                labels.getOrNull(value.toInt().coerceIn(0, labels.size - 1)) ?: value.toString()
            }
        )
    )
}

@Composable
fun VicoLineChart(data: List<Pair<String, Double>>) {
    if (data.isEmpty()) return

    val labels = data.map { it.first }
    val chartModel = remember(data) {
        val entries = data.mapIndexed { index, pair ->
            FloatEntry(x = index.toFloat(), y = pair.second.toFloat())
        }
        entryModelOf(entries)
    }

    Chart(
        chart = lineChart(),
        model = chartModel,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = AxisValueFormatter { value, _ ->
                labels.getOrNull(value.toInt().coerceIn(0, labels.size - 1)) ?: value.toString()
            }
        )
    )
}
