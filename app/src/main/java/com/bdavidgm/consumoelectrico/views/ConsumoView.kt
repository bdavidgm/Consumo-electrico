package com.bdavidgm.consumoelectrico.views


import com.bdavidgm.consumoelectrico.R
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.bdavidgm.consumoelectrico.datastore.SettingsRepository
import com.bdavidgm.consumoelectrico.viewmodels.ConsumoViewModel
import com.bdavidgm.consumoelectrico.viewmodels.SettingsViewModel

@Composable
fun ConsumoView(consumoVM: ConsumoViewModel, settingsVM: SettingsViewModel) {
    // Opción 1: Pasar screen1 directamente
    scaffoldView(
        title = "Consumo Eléctrico",
        viewModel = consumoVM,
        view = { paddingValues, viewModel ->
            // Convertimos ViewModel a ConsumoViewModel (es seguro ya que sabemos el tipo)
            screen1(
                paddingValues = 15.dp,
                consumoVM = viewModel as ConsumoViewModel,
                settingsVM
            )
        }
    )
    //
}


@Composable
fun screen1(paddingValues: Dp, consumoVM: ConsumoViewModel, settingsVM: SettingsViewModel)
{
    val listadoConsumo by consumoVM.listadoConsumo.collectAsState()
    var lectura : String by rememberSaveable { mutableStateOf("") }
    //settingsVM.
    val headers: List<String> = emptyList()
    val rows: List<List<String>> = emptyList()


    Column( modifier = Modifier
        .fillMaxSize()
        .padding(top = 100.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    )
    {
        OutlinedTextField(
            value = lectura,
            onValueChange = {lectura = it; },
            label = { Text("Lectura del contador") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 16.dp),

            )


        Row() {
            Button(
                onClick = { },
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    colorResource(id = R.color.PrimaryColor)
                )
            ) {
                Text("Agregar y enviar reporte")
            }

            Button(
                onClick = { },
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    colorResource(id = R.color.PrimaryColor)
                )
            ) {
                Text("Solo agregar")
            }


        }
        BorderedTable(headers, rows)
    }
}


@Composable
fun BorderedTable(headers: List<String>, rows: List<List<String>>) {
    val heigth = 35.dp
    Column {
        // Encabezado
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp, bottom = 0.dp, top = 0.dp,)
                .height(heigth)
        ) {
            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.Gray)
                        .padding(0.dp)
                        .height(35.dp)
                        .align(Alignment.CenterVertically),
                    fontWeight = FontWeight.Bold,

                    )
            }
        }
        // Filas
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp, bottom = 0.dp, top = 0.dp,)
                    .height(heigth)
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.LightGray)
                            .padding(0.dp)
                            .height(35.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scaffoldView(title: String, viewModel: ViewModel,view: @Composable (padding: PaddingValues, VM: ViewModel) -> Unit )
{
    //var presses by remember{ mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryColor),//MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = colorResource(id = R.color.SecondaryColor)//MaterialTheme.colorScheme.primarycolorResource(id = R.color.Azul1)
                ),
                title = {
                    Text(
                        color = colorResource(id = R.color.black),
                        text = title,
                        fontWeight = FontWeight.Normal
                    )
                },
                actions = {
                    // Add icons and actions here

                    /*IconButton(onClick = { /* Handle settings action */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }*/
                },
                navigationIcon = {
                    // Solo muestra el icono si hay algo a lo que navegar hacia atrás


                }
            )
        },
        /*bottomBar = {
            BottomAppBar(
                containerColor = colorResource(id = R.color.white),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .height(90.dp)
                    .padding(bottom = 5.dp /*WindowInsets.navigationBars.getBottom(LocalDensity.current).dp*/)

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp), // Adjust horizontal padding as needed
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    // RoundAddButton(onClick = { println("Add button clicked!") },25,Icons.Filled.Add)
                    //OvalSaveButton("Agregar",{ showDialog = true })
                    Spacer(modifier = Modifier.width(40.dp))
                    //OvalSaveButton("Guardar",{ addToDataBase = true })
                    // RoundAddButton(onClick = { showDialog = true }, icon = Icons.Filled.Add)
                    // RoundAddButton(onClick = {addAccountVM.addAccount(itemList) },50, icon = Icons.Outlined.Check)
                }
            }
        },*/

        ) { innerPadding ->

        view(innerPadding,viewModel);


    }

}