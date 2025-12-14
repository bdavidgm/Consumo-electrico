package com.bdavidgm.consumoelectrico.room

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import com.bdavidgm.consumoelectrico.model.Consumo
import javax.inject.Inject

class ConsumoRepository @Inject constructor(private val consumoDao: ConsumoDao) {
    suspend fun insertConsumo(consmo: Consumo) = consumoDao.insert(consmo)
    suspend fun updateConsumo(consmo: Consumo) = consumoDao.update(consmo)
    suspend fun deleteConsumo(consmo: Consumo) = consumoDao.delete(consmo)
    suspend fun deleteAll() = consumoDao.deleteAll()

    fun getAll(): Flow<List<Consumo>> = consumoDao.getAll().flowOn(Dispatchers.IO).conflate()
    fun getConsumoByDate(dd: Long, mm: Long,yyyy: Long): Flow<List<Consumo>> = consumoDao.getConsumoByDate(dd,mm,yyyy).flowOn(Dispatchers.IO).conflate()
    fun getConsumoByDate(mm: Long,yyyy: Long): Flow<List<Consumo>> = consumoDao.getConsumoByDate(mm,yyyy).flowOn(Dispatchers.IO).conflate()
    fun getConsumoByDate(yyyy: Long): Flow<List<Consumo>> = consumoDao.getConsumoByDate(yyyy).flowOn(Dispatchers.IO).conflate()
}