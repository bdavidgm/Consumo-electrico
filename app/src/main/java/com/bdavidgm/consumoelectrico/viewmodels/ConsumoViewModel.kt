package com.bdavidgm.consumoelectrico.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdavidgm.consumoelectrico.model.Consumo
import com.bdavidgm.consumoelectrico.room.ConsumoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConsumoViewModel @Inject constructor(private val repository: ConsumoRepository) : ViewModel() {

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
}