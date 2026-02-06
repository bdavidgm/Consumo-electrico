package com.bdavidgm.consumoelectrico

import com.bdavidgm.consumoelectrico.utils.FormatoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios para FormatoUtils.
 */
class FormatoUtilsTest {

    @Test
    fun formatearFecha_formatoCorrecto() {
        assertEquals("01/01/2024", FormatoUtils.formatearFecha(1, 1, 2024))
        assertEquals("15/03/2024", FormatoUtils.formatearFecha(15, 3, 2024))
        assertEquals("31/12/1999", FormatoUtils.formatearFecha(31, 12, 1999))
    }

    @Test
    fun formatearFecha_conCerosIzquierda() {
        assertEquals("05/09/2020", FormatoUtils.formatearFecha(5, 9, 2020))
    }

    @Test
    fun formatearFechaCorta_formatoCorrecto() {
        assertEquals("01/01", FormatoUtils.formatearFechaCorta(1, 1))
        assertEquals("15/03", FormatoUtils.formatearFechaCorta(15, 3))
        assertEquals("31/12", FormatoUtils.formatearFechaCorta(31, 12))
    }

    @Test
    fun formatearFechaCorta_conCerosIzquierda() {
        assertEquals("05/09", FormatoUtils.formatearFechaCorta(5, 9))
    }

    @Test
    fun formatearNumero_devuelveStringNoVacio() {
        val resultado = FormatoUtils.formatearNumero(1234.56)
        assertTrue(resultado.isNotEmpty())
    }

    @Test
    fun formatearNumero_contieneParteEnteraYDecimal() {
        val resultado = FormatoUtils.formatearNumero(100.5)
        assertTrue("Debe contener la parte entera 100", resultado.contains("100"))
        assertTrue("Debe contener la parte decimal (5 o 50)", resultado.contains("5"))
    }

    @Test
    fun formatearNumero_cero() {
        val resultado = FormatoUtils.formatearNumero(0.0)
        assertTrue("Debe contener 0", resultado.contains("0"))
    }
}
