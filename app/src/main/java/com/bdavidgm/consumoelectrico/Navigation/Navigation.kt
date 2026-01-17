package com.bdavidgm.consumoelectrico.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import com.bdavidgm.consumoelectrico.viewmodels.SettingsViewModel
import com.bdavidgm.consumoelectrico.views.ConsumoScreen
import com.bdavidgm.consumoelectrico.views.SettingsScreen

// Definición de las rutas de navegación
sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Settings : Screen("settings_screen")
}

@Composable
fun AppNavigation(consumoViewModel:ConsumoViewModel,settingsViewModel:SettingsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        // Pantalla principal (Consumo)
        composable(Screen.Main.route) {
            ConsumoScreen(
                consumoViewModel,
                settingsViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Pantalla de configuración
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}