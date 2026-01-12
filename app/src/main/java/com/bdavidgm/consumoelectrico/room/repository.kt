package com.bdavidgm.consumoelectrico.room

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import com.bdavidgm.consumoelectrico.model.Consumo
import javax.inject.Inject

class ConsumoRepository @Inject constructor(private val consumoDao: ConsumoDao) {
    suspend fun insertConsumo(consumo: Consumo) = consumoDao.insert(consumo)
    suspend fun updateConsumo(consumo: Consumo) = consumoDao.update(consumo)
    suspend fun deleteConsumo(consumo: Consumo) = consumoDao.delete(consumo)
    suspend fun deleteAll() = consumoDao.deleteAll()

    fun getAll(): Flow<List<Consumo>> = consumoDao.getAll().flowOn(Dispatchers.IO).conflate()

    fun getConsumoByDate(dd: Long, mm: Long, yyyy: Long): Flow<List<Consumo>> =
        consumoDao.getConsumoByDate(dd, mm, yyyy).flowOn(Dispatchers.IO).conflate()

    fun getConsumoByDate(mm: Long, yyyy: Long): Flow<List<Consumo>> =
        consumoDao.getConsumoByDate(mm, yyyy).flowOn(Dispatchers.IO).conflate()

    fun getConsumoByDate(yyyy: Long): Flow<List<Consumo>> =
        consumoDao.getConsumoByDate(yyyy).flowOn(Dispatchers.IO).conflate()

    // Nueva función para obtener el último registro del mes
    suspend fun getUltimoRegistroDelMes(mes: Long, anio: Long): Consumo? {
        return consumoDao.getUltimoRegistroDelMes(mes, anio)
    }
}