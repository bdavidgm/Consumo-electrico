package com.bdavidgm.consumoelectrico.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdavidgm.consumoelectrico.datastore.SettingsRepository
import com.bdavidgm.consumoelectrico.model.Consumo
import com.bdavidgm.consumoelectrico.room.ConsumoRepository
import com.bdavidgm.consumoelectrico.utils.EmailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConsumoViewModel @Inject constructor(
    private val repository: ConsumoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _listadoConsumo = MutableStateFlow<List<Consumo>>(emptyList())
    val listadoConsumo = _listadoConsumo.asStateFlow()

    private var _columns = MutableStateFlow<List<List<String>>>(emptyList())
    val columns = _columns.asStateFlow()

    // Para manejar errores
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Para manejar éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    init {
        cargarConsumosDelMes()
    }

    fun cargarConsumosDelMes() {
        val fechaActual = LocalDate.now()
        val mes = fechaActual.monthValue.toLong()
        val anio = fechaActual.year.toLong()

        viewModelScope.launch(Dispatchers.IO) {
            repository.getConsumoByDate(mes, anio).collect { consumos ->
                _listadoConsumo.value = consumos
                actualizarColumnas(consumos)
            }
        }
    }

    fun cargarConsumosDelAnio() {
        val fechaActual = LocalDate.now()
        val anio = fechaActual.year.toLong()

        viewModelScope.launch(Dispatchers.IO) {
            repository.getConsumoByDate(anio).collect { consumos ->
                _listadoConsumo.value = consumos
                actualizarColumnas(consumos)
            }
        }
    }

    private fun actualizarColumnas(consumos: List<Consumo>) {
        val nuevasColumnas = consumos.sortedBy { it.fechaCreacion }.map { consumo ->
            listOf(
                "${consumo.dia}/${consumo.mes}/${consumo.anio}",
                String.format(Locale.getDefault(), "%.2f", consumo.lectura),
                String.format(Locale.getDefault(), "%.2f", consumo.diario),
                String.format(Locale.getDefault(), "%.2f", consumo.mensual)
            )
        }
        _columns.value = nuevasColumnas
    }

    suspend fun registrarLectura(lecturaActual: Double): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fechaActual = LocalDate.now()
                val dia = fechaActual.dayOfMonth.toLong()
                val mes = fechaActual.monthValue.toLong()
                val anio = fechaActual.year.toLong()

                // Verificar si ya existe registro para hoy
                val registrosHoy = repository.getConsumoByDate(dia, mes, anio).first()
                if (registrosHoy.isNotEmpty()) {
                    _errorMessage.value = "Ya existe un registro para hoy"
                    return@withContext false
                }

                // Obtener todos los registros del mes
                val registrosMes = repository.getConsumoByDate(mes, anio).first()

                // Calcular consumo diario
                val consumoDiario = if (registrosMes.isNotEmpty()) {
                    val ultimoRegistro = registrosMes.maxByOrNull { it.fechaCreacion } ?: registrosMes.last()
                    lecturaActual - ultimoRegistro.lectura
                } else {
                    // Primer registro del mes
                    lecturaActual
                }

                if (consumoDiario < 0) {
                    _errorMessage.value = "La lectura no puede ser menor a la anterior"
                    return@withContext false
                }

                // Calcular acumulado mensual
                val acumuladoMensual = registrosMes.sumOf { it.diario } + consumoDiario

                // Crear nuevo consumo
                val nuevoConsumo = Consumo(
                    dia = dia,
                    mes = mes,
                    anio = anio,
                    lectura = lecturaActual,
                    diario = consumoDiario,
                    mensual = acumuladoMensual
                )

                // Insertar en la base de datos
                repository.insertConsumo(nuevoConsumo)

                // Actualizar lista
                cargarConsumosDelMes()

                _successMessage.value = "Lectura registrada exitosamente"
                _errorMessage.value = null
                true
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar: ${e.message}"
                false
            }
        }
    }

    suspend fun obtenerUltimaLectura(): Double? {
        return withContext(Dispatchers.IO) {
            val fechaActual = LocalDate.now()
            val mes = fechaActual.monthValue.toLong()
            val anio = fechaActual.year.toLong()

            val registros = repository.getConsumoByDate(mes, anio).first()
            if (registros.isEmpty()) {
                null
            } else {
                registros.maxByOrNull { it.fechaCreacion }?.lectura
            }
        }
    }

    fun limpiarMensajes() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Genera un reporte en formato HTML con los datos de consumo del mes actual
     */
    suspend fun generarReporteHTML(): String {
        return withContext(Dispatchers.IO) {
            val fechaActual = LocalDate.now()
            val mes = fechaActual.monthValue.toLong()
            val anio = fechaActual.year.toLong()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val nombreMes = fechaActual.month.name.lowercase().replaceFirstChar { it.uppercase() }

            val registros = repository.getConsumoByDate(mes, anio).first()
            val consumosOrdenados = registros.sortedBy { it.fechaCreacion }

            val html = StringBuilder()
            html.appendLine("<!DOCTYPE html>")
            html.appendLine("<html>")
            html.appendLine("<head>")
            html.appendLine("<meta charset='UTF-8'>")
            html.appendLine("<style>")
            html.appendLine("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }")
            html.appendLine(".container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            html.appendLine("h1 { color: #1976d2; text-align: center; border-bottom: 3px solid #1976d2; padding-bottom: 10px; }")
            html.appendLine(".info { background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; }")
            html.appendLine(".info p { margin: 5px 0; }")
            html.appendLine("table { width: 100%; border-collapse: collapse; margin: 20px 0; }")
            html.appendLine("th { background-color: #1976d2; color: white; padding: 12px; text-align: left; font-weight: bold; }")
            html.appendLine("td { padding: 10px; border-bottom: 1px solid #ddd; }")
            html.appendLine("tr:nth-child(even) { background-color: #f9f9f9; }")
            html.appendLine("tr:hover { background-color: #f5f5f5; }")
            html.appendLine(".total { background-color: #4caf50; color: white; font-weight: bold; font-size: 18px; }")
            html.appendLine(".total td { padding: 15px; }")
            html.appendLine(".text-right { text-align: right; }")
            html.appendLine(".text-center { text-align: center; }")
            html.appendLine("</style>")
            html.appendLine("</head>")
            html.appendLine("<body>")
            html.appendLine("<div class='container'>")
            html.appendLine("<h1>📊 REPORTE DE CONSUMO ELÉCTRICO</h1>")
            html.appendLine("<div class='info'>")
            html.appendLine("<p><strong>Mes:</strong> $nombreMes $anio</p>")
            html.appendLine("<p><strong>Fecha de generación:</strong> ${fechaActual.format(formatter)}</p>")
            html.appendLine("</div>")

            if (consumosOrdenados.isEmpty()) {
                html.appendLine("<p style='text-align: center; color: #666; padding: 20px;'>No hay registros para este período.</p>")
            } else {
                html.appendLine("<table>")
                html.appendLine("<thead>")
                html.appendLine("<tr>")
                html.appendLine("<th>Fecha</th>")
                html.appendLine("<th class='text-right'>Lectura (kWh)</th>")
                html.appendLine("<th class='text-right'>Consumo (kWh)</th>")
                html.appendLine("<th class='text-right'>Acumulado (kWh)</th>")
                html.appendLine("</tr>")
                html.appendLine("</thead>")
                html.appendLine("<tbody>")

                consumosOrdenados.forEach { consumo ->
                    val fecha = String.format("%02d/%02d/%04d", consumo.dia, consumo.mes, consumo.anio)
                    val lectura = String.format(Locale.getDefault(), "%.2f", consumo.lectura)
                    val diario = String.format(Locale.getDefault(), "%.2f", consumo.diario)
                    val mensual = String.format(Locale.getDefault(), "%.2f", consumo.mensual)
                    
                    html.appendLine("<tr>")
                    html.appendLine("<td>$fecha</td>")
                    html.appendLine("<td class='text-right'>$lectura</td>")
                    html.appendLine("<td class='text-right'>$diario</td>")
                    html.appendLine("<td class='text-right'>$mensual</td>")
                    html.appendLine("</tr>")
                }

                html.appendLine("</tbody>")
                html.appendLine("</table>")
                
                // Total acumulado
                val totalAcumulado = consumosOrdenados.lastOrNull()?.mensual ?: 0.0
                html.appendLine("<table>")
                html.appendLine("<tr class='total'>")
                html.appendLine("<td colspan='3'><strong>TOTAL ACUMULADO</strong></td>")
                html.appendLine("<td class='text-right'><strong>${String.format(Locale.getDefault(), "%.2f", totalAcumulado)} kWh</strong></td>")
                html.appendLine("</tr>")
                html.appendLine("</table>")
            }

            html.appendLine("</div>")
            html.appendLine("</body>")
            html.appendLine("</html>")
            html.toString()
        }
    }

    /**
     * Genera un reporte en texto plano con los datos de consumo del mes actual
     */
    suspend fun generarReporteTextoPlano(): String {
        return withContext(Dispatchers.IO) {
            val fechaActual = LocalDate.now()
            val mes = fechaActual.monthValue.toLong()
            val anio = fechaActual.year.toLong()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val nombreMes = fechaActual.month.name.lowercase().replaceFirstChar { it.uppercase() }

            val registros = repository.getConsumoByDate(mes, anio).first()
            val consumosOrdenados = registros.sortedBy { it.fechaCreacion }

            val reporte = StringBuilder()
            reporte.appendLine("=".repeat(60))
            reporte.appendLine("REPORTE DE CONSUMO ELÉCTRICO")
            reporte.appendLine("Mes: $nombreMes $anio")
            reporte.appendLine("Fecha de generación: ${fechaActual.format(formatter)}")
            reporte.appendLine("=".repeat(60))
            reporte.appendLine()

            if (consumosOrdenados.isEmpty()) {
                reporte.appendLine("No hay registros para este período.")
            } else {
                // Encabezado de la tabla
                reporte.appendLine(String.format("%-12s | %12s | %12s | %12s", "Fecha", "Lectura", "Consumo", "Acumulado"))
                reporte.appendLine("-".repeat(60))

                // Filas de datos
                consumosOrdenados.forEach { consumo ->
                    val fecha = String.format("%02d/%02d/%04d", consumo.dia, consumo.mes, consumo.anio)
                    val lectura = String.format(Locale.getDefault(), "%.2f", consumo.lectura)
                    val diario = String.format(Locale.getDefault(), "%.2f", consumo.diario)
                    val mensual = String.format(Locale.getDefault(), "%.2f", consumo.mensual)
                    reporte.appendLine(String.format("%-12s | %12s | %12s | %12s", fecha, lectura, diario, mensual))
                }

                reporte.appendLine("-".repeat(60))
                
                // Total acumulado
                val totalAcumulado = consumosOrdenados.lastOrNull()?.mensual ?: 0.0
                reporte.appendLine()
                reporte.appendLine(String.format("TOTAL ACUMULADO: %.2f kWh", totalAcumulado))
            }

            reporte.appendLine()
            reporte.appendLine("=".repeat(60))
            reporte.toString()
        }
    }

    /**
     * Envía el reporte por correo electrónico a todos los destinatarios configurados
     */
    fun enviarReportePorCorreo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settings = settingsRepository.getCurrentSettings()
                
                // Log para depuración
                android.util.Log.d("ConsumoViewModel", "Email remitente: ${settings.senderEmail}")
                android.util.Log.d("ConsumoViewModel", "Contraseña obtenida: ${if (settings.senderPassword.isNotEmpty()) "***${settings.senderPassword.length} caracteres***" else "VACÍA"}")
                android.util.Log.d("ConsumoViewModel", "Destinatarios: ${settings.reportEmails.size}")
                
                // Verificar que haya credenciales del remitente
                if (settings.senderEmail.isEmpty() || settings.senderPassword.isEmpty()) {
                    android.util.Log.e("ConsumoViewModel", "Credenciales faltantes - Email: ${settings.senderEmail.isEmpty()}, Password: ${settings.senderPassword.isEmpty()}")
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            "Error: No hay credenciales del remitente configuradas",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Verificar que haya destinatarios
                if (settings.reportEmails.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            context,
                            "Error: No hay emails de destinatarios configurados",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                // Generar el reporte en HTML
                val reporteHTML = generarReporteHTML()
                val fechaActual = LocalDate.now()
                val nombreMes = fechaActual.month.name.lowercase().replaceFirstChar { it.uppercase() }
                val asunto = "Reporte de Consumo Eléctrico - $nombreMes ${fechaActual.year}"

                // Enviar a cada destinatario
                settings.reportEmails.forEach { destinatario ->
                    EmailService.enviarCorreoHTML(
                        usuario = settings.senderEmail,
                        password = settings.senderPassword,
                        destinatario = destinatario,
                        asunto = asunto,
                        cuerpoHTML = reporteHTML,
                        context = context,
                        scope = viewModelScope
                    )
                }

                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "Reporte enviado a ${settings.reportEmails.size} destinatario(s)",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        "Error al enviar reporte: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}