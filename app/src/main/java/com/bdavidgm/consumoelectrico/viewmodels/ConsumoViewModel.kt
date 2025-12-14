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

    fun GetThisMonth() {
        val fechaActual = LocalDate.now()
        val mes = fechaActual.monthValue.toLong()
        val anio = fechaActual.year.toLong()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getConsumoByDate(mes,anio ).collect{ item ->
                if (item.isNullOrEmpty()){
                    _listadoConsumo.value = Collections.emptyList()
                } else {
                    _listadoConsumo.value = item

                }
            }
        }
    }

    fun GetThisYear() {
        val fechaActual = LocalDate.now()
        val anio = fechaActual.year.toLong()
        viewModelScope.launch(Dispatchers.IO) {
            repository.getConsumoByDate(anio ).collect{ item ->
                if (item.isNullOrEmpty()){
                    _listadoConsumo.value = Collections.emptyList()
                } else {
                    _listadoConsumo.value = item
                }
            }
        }
    }
}


