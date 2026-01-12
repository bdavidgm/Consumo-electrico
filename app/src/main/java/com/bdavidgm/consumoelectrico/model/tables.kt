package com.bdavidgm.consumoelectrico.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "consumo")
data class Consumo(

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,

    @ColumnInfo(name = "diario")
    val diario: Double = 0.0,

    @ColumnInfo(name = "mensual")
     val mensual: Double = 0.0,

    @ColumnInfo(name = "lectura")
    val lectura: Double = 0.0,

    @ColumnInfo(name = "fechaCreacion")
    val fechaCreacion: Long = System.currentTimeMillis(),

@ColumnInfo(name = "dia")
val dia: Int = 0,

@ColumnInfo(name = "mes")
val mes: Int = 0,

@ColumnInfo(name = "anio")
val anio: Int = 0
)
