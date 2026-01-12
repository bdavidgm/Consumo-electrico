package com.bdavidgm.consumoelectrico.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bdavidgm.consumoelectrico.model.Consumo

@Database(entities = [Consumo::class], version = 2, exportSchema = false)
abstract class ConsumoDataBase: RoomDatabase() {
    abstract fun consumoDao() : ConsumoDao
}