package com.bdavidgm.consumoelectrico.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.bdavidgm.consumoelectrico.model.Consumo

@Dao // Data Access Observer
interface ConsumoDao {
    // Crud
    @Query("SELECT * FROM consumo")
    fun getAll(): Flow<List<Consumo>>

    @Query("SELECT * FROM consumo WHERE dia=:dd AND mes=:mm AND anio=:yyyy")
    fun getConsumoByDate(dd: Long,mm: Long,yyyy: Long): Flow<List<Consumo>>

    @Query("SELECT * FROM consumo WHERE mes=:mm AND anio=:yyyy")
    fun getConsumoByDate(mm: Long,yyyy: Long): Flow<List<Consumo>>

    @Query("SELECT * FROM consumo WHERE anio=:yyyy")
    fun getConsumoByDate(yyyy: Long): Flow<List<Consumo>>

    @Query("DELETE FROM consumo")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consumo: Consumo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(consumo: Consumo)

    @Delete
    suspend fun delete(consumo: Consumo)

}
