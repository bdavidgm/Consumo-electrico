package com.bdavidgm.consumoelectrico.room

import androidx.room.*
import com.bdavidgm.consumoelectrico.model.Consumo
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsumoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consumo: Consumo)

    @Update
    suspend fun update(consumo: Consumo)

    @Delete
    suspend fun delete(consumo: Consumo)

    @Query("DELETE FROM consumo_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM consumo_table ORDER BY fechaCreacion DESC")
    fun getAll(): Flow<List<Consumo>>

    @Query("SELECT * FROM consumo_table WHERE dia = :dd AND mes = :mm AND anio = :yyyy ORDER BY fechaCreacion DESC")
    fun getConsumoByDate(dd: Long, mm: Long, yyyy: Long): Flow<List<Consumo>>

    @Query("SELECT * FROM consumo_table WHERE mes = :mm AND anio = :yyyy ORDER BY fechaCreacion DESC")
    fun getConsumoByDate(mm: Long, yyyy: Long): Flow<List<Consumo>>

    @Query("SELECT * FROM consumo_table WHERE anio = :yyyy ORDER BY fechaCreacion DESC")
    fun getConsumoByDate(yyyy: Long): Flow<List<Consumo>>

    // Nueva consulta para obtener el último registro del mes
    @Query("SELECT * FROM consumo_table WHERE mes = :mes AND anio = :anio ORDER BY fechaCreacion DESC LIMIT 1")
    suspend fun getUltimoRegistroDelMes(mes: Long, anio: Long): Consumo?
}