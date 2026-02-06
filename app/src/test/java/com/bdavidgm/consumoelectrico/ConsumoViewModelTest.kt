package com.bdavidgm.consumoelectrico

import com.bdavidgm.consumoelectrico.datastore.SettingsRepository
import com.bdavidgm.consumoelectrico.model.Consumo
import com.bdavidgm.consumoelectrico.room.ConsumoRepository
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Run
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsumoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockRepository: ConsumoRepository = mockk(relaxed = true)
    private val mockSettingsRepository: SettingsRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Por defecto: flujos vacíos para que init no falle
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(emptyList())
        every { mockRepository.getConsumoByDate(any(), any(), any()) } returns flowOf(emptyList())
        every { mockRepository.getConsumoByDate(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun limpiarMensajes_borraErrorYExito() = runTest(testDispatcher) {
        // Primero provocamos un error (registro duplicado)
        every { mockRepository.getConsumoByDate(any(), any(), any()) } returns flowOf(
            listOf(Consumo(dia = 15, mes = 3, anio = 2024, lectura = 100.0))
        )
        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        viewModel.registrarLectura(150.0)
        advanceUntilIdle()
        assertEquals("Ya existe un registro para hoy", viewModel.errorMessage.value)

        viewModel.limpiarMensajes()
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun obtenerUltimaLectura_conRegistros_devuelveUltimaLectura() = runTest(testDispatcher) {
        val consumo = Consumo(
            dia = 1,
            mes = 3,
            anio = 2024,
            lectura = 50.5,
            diario = 10.0,
            mensual = 10.0,
            fechaCreacion = 1000L
        )
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(listOf(consumo))

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val resultado = viewModel.obtenerUltimaLectura()
        advanceUntilIdle()

        assertEquals(50.5, resultado!!, 0.001)
    }

    @Test
    fun obtenerUltimaLectura_sinRegistros_devuelveNull() = runTest(testDispatcher) {
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(emptyList())

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val resultado = viewModel.obtenerUltimaLectura()
        advanceUntilIdle()

        assertNull(resultado)
    }

    @Test
    fun obtenerDatosParaGrafico_MENSUAL_devuelveParesCorrectos() = runTest(testDispatcher) {
        val consumos = listOf(
            Consumo(dia = 1, mes = 3, anio = 2024, lectura = 100.0, diario = 0.0, mensual = 0.0, fechaCreacion = 1),
            Consumo(dia = 2, mes = 3, anio = 2024, lectura = 110.0, diario = 10.0, mensual = 10.0, fechaCreacion = 2)
        )
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(consumos)

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val resultado = viewModel.obtenerDatosParaGrafico("MENSUAL")
        advanceUntilIdle()

        assertEquals(2, resultado.size)
        assertEquals("1/3", resultado[0].first)
        assertEquals(0.0, resultado[0].second, 0.001)
        assertEquals("2/3", resultado[1].first)
        assertEquals(10.0, resultado[1].second, 0.001)
    }

    @Test
    fun registrarLectura_yaExisteRegistroHoy_retornaFalse() = runTest(testDispatcher) {
        every { mockRepository.getConsumoByDate(any(), any(), any()) } returns flowOf(
            listOf(Consumo(dia = 15, mes = 3, anio = 2024, lectura = 100.0))
        )
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(emptyList())

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val resultado = viewModel.registrarLectura(150.0)
        advanceUntilIdle()

        assertFalse(resultado)
        assertEquals("Ya existe un registro para hoy", viewModel.errorMessage.value)
    }

    @Test
    fun registrarLectura_lecturaMenorQueAnterior_retornaFalse() = runTest(testDispatcher) {
        every { mockRepository.getConsumoByDate(any(), any(), any()) } returns flowOf(emptyList())
        val registrosMes = listOf(
            Consumo(dia = 1, mes = 3, anio = 2024, lectura = 100.0, diario = 100.0, mensual = 100.0, fechaCreacion = 1)
        )
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(registrosMes)

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val resultado = viewModel.registrarLectura(50.0) // menor que 100
        advanceUntilIdle()

        assertFalse(resultado)
        assertTrue(viewModel.errorMessage.value!!.contains("menor"))
    }

    @Test
    fun registrarLectura_primeraLecturaDelMes_retornaTrue() = runTest(testDispatcher) {
        every { mockRepository.getConsumoByDate(any(), any(), any()) } returns flowOf(emptyList())
        every { mockRepository.getConsumoByDate(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.insertConsumo(any()) } just Run

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val resultado = viewModel.registrarLectura(100.0)
        advanceUntilIdle()

        assertTrue(resultado)
        assertEquals("Lectura registrada exitosamente", viewModel.successMessage.value)
    }

    @Test
    fun generarReporteHTML_conDatos_contieneContenidoEsperado() = runTest(testDispatcher) {
        val consumos = listOf(
            Consumo(dia = 1, mes = 3, anio = 2024, lectura = 100.0, diario = 100.0, mensual = 100.0, fechaCreacion = 1)
        )
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(consumos)

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val html = viewModel.generarReporteHTML()
        advanceUntilIdle()

        assertTrue(html.contains("REPORTE DE CONSUMO"))
        assertTrue(html.contains("01/03/2024") || html.contains("1/3/2024"))
        assertTrue(html.contains("100"))
        assertTrue(html.contains("</html>"))
    }

    @Test
    fun generarReporteTextoPlano_conDatos_contieneContenidoEsperado() = runTest(testDispatcher) {
        val consumos = listOf(
            Consumo(dia = 1, mes = 3, anio = 2024, lectura = 100.0, diario = 100.0, mensual = 100.0, fechaCreacion = 1)
        )
        every { mockRepository.getConsumoByDate(any(), any()) } returns flowOf(consumos)

        val viewModel = ConsumoViewModel(mockRepository, mockSettingsRepository)
        advanceUntilIdle()

        val texto = viewModel.generarReporteTextoPlano()
        advanceUntilIdle()

        assertTrue(texto.contains("REPORTE DE CONSUMO ELÉCTRICO"))
        assertTrue(texto.contains("100"))
        assertTrue(texto.contains("TOTAL ACUMULADO"))
    }
}
