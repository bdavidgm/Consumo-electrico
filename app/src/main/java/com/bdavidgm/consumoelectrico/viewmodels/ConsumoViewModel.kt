package com.bdavidgm.consumoelectrico.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdavidgm.consumoelectrico.model.Consumo
import com.bdavidgm.consumoelectrico.room.ConsumoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@HiltViewModel
class ConsumoViewModel @Inject constructor(private val repository: ConsumoRepository): ViewModel() {
    private val _listadoConsumo = MutableStateFlow<List<Consumo>>(emptyList())
    val listadoConsumo = _listadoConsumo.asStateFlow()

    private var _columns = MutableStateFlow<List<List<String>>>(emptyList())
    val columns = _columns.asStateFlow()

    fun GetThisMonth() {
        val fechaActual = LocalDate.now()
        val mes = fechaActual.monthValue.toLong()
        val anio = fechaActual.year.toLong()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getConsumoByDate(mes, anio).collect{ item ->
                if (item.isNullOrEmpty()){
                    _listadoConsumo.value = Collections.emptyList()
                    _columns.value = emptyList() // Limpiar columnas si no hay datos
                } else {
                    _listadoConsumo.value = item

                    // Transformar cada Consumo en una lista de Strings y agregarla a _columns
                    val nuevasColumnas = item.map { consumo ->
                        // Crear lista con los valores formateados
                        listOf(
                            "${consumo.dia}/${consumo.mes}/${consumo.anio}", // Fecha formateada
                            String.format("%.2f", consumo.diario), // Diario con 2 decimales
                            String.format("%.2f", consumo.mensual) // Mensual con 2 decimales
                        )
                    }

                    // Asignar las nuevas columnas
                    _columns.value = nuevasColumnas
                }
            }
        }
    }

    fun GetThisYear() {
        val fechaActual = LocalDate.now()
        val anio = fechaActual.year.toLong()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getConsumoByDate(anio).collect{ item ->
                if (item.isNullOrEmpty()){
                    _listadoConsumo.value = Collections.emptyList()
                    _columns.value = emptyList()
                } else {
                    _listadoConsumo.value = item

                    // Transformar cada Consumo en una lista de Strings
                    val nuevasColumnas = item.map { consumo ->
                        listOf(
                            "${consumo.dia}/${consumo.mes}/${consumo.anio}",
                            String.format("%.2f", consumo.diario),
                            String.format("%.2f", consumo.mensual)
                        )
                    }

                    _columns.value = nuevasColumnas
                }
            }
        }
    }

    // Método adicional si necesitas agregar un solo consumo
    fun agregarConsumoAColumns(consumo: Consumo) {
        val nuevaFila = listOf(
            "${consumo.dia}/${consumo.mes}/${consumo.anio}",
            String.format("%.2f", consumo.diario),
            String.format("%.2f", consumo.mensual)
        )

        // Agregar la nueva fila a las columnas existentes
        val columnasActuales = _columns.value.toMutableList()
        columnasActuales.add(nuevaFila)
        _columns.value = columnasActuales
    }
}

