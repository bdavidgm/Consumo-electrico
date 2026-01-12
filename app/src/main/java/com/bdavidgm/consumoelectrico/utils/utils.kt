package com.bdavidgm.consumoelectrico.utils


import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object FormatoUtils {

    fun formatearNumero(numero: Double): String {
        val df = DecimalFormat("#,##0.00")
        df.decimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault())
        return df.format(numero)
    }

    fun formatearFecha(dia: Long, mes: Long, anio: Long): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes, anio)
    }

    fun formatearFechaCorta(dia: Long, mes: Long): String {
        return String.format(Locale.getDefault(), "%02d/%02d", dia, mes)
    }
}