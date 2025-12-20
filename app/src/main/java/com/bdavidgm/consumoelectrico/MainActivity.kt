package com.bdavidgm.consumoelectrico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bdavidgm.consumoelectrico.ui.theme.ConsumoelectricoTheme
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import com.bdavidgm.consumoelectrico.viewmodels.SettingsViewModel
import com.bdavidgm.consumoelectrico.views.ConsumoView
import com.bdavidgm.consumoelectrico.views.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConsumoelectricoTheme {
               val st : SettingsViewModel by viewModels()
               val cvm : ConsumoViewModel by viewModels()
              // SettingsScreen(st)
                ConsumoView(cvm)

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConsumoelectricoTheme {
        Greeting("Android")
    }
}