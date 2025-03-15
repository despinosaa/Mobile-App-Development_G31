package com.example.senefavores.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor

@Composable
fun CreateFavorScreen(
    navController: NavController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean
) {
    var selectedItem by remember { mutableStateOf(1) }

    // El check de la locacion (me copie de dani)
    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        println("¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}")
        result
    } else {
        false
    }

    // La vista per se jeje
    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "SeneFavores",
                onAccountClick = { navController.navigate("account") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedItem,
                onItemClick = { index ->
                    selectedItem = index
                    when (index) {
                        0 -> navController.navigate("home") { launchSingleTop = true }
                        1 -> navController.navigate("createFavor") { launchSingleTop = true }
                        2 -> navController.navigate("history") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        // Variables del favor
        var selectedCategory by remember { mutableStateOf("Favor") }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var recompensa by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Titulo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Título:")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Descripcion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Descripción:")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp),
                maxLines = 6,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recompensa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recompensa:")
                OutlinedTextField(
                    value = recompensa,
                    onValueChange = { recompensa = it },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categoria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Categoría:")
                Row {
                    val categories = listOf("Favor", "Compra", "Tutoria")
                    for (category in categories) {
                        val categoryColor = when (category) {
                            "Favor" -> FavorCategoryColor
                            "Compra" -> CompraCategoryColor
                            "Tutoria" -> TutoriaCategoryColor
                            else -> Color.Black
                        }

                        val isSelected = (selectedCategory == category)
                        val containerColor = if (isSelected) categoryColor else Color.White
                        val contentColor = if (isSelected) Color.White else Color.Black

                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { selectedCategory = category },
                            border = BorderStroke(1.dp, Color.Black),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = containerColor,
                                contentColor = contentColor,
                                disabledContainerColor = Color.LightGray,
                                disabledContentColor = Color.DarkGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(category)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Publicar
            OutlinedButton(
                onClick = {
                    // detecta si esta en el campus, esto es un place holder
                    // #TODO: cambiar cuando este el back conecatado
                    val recompensaInt = recompensa.toIntOrNull() ?: 0
                    val categoriaIndex = listOf("Favor", "Compra", "Tutoria").indexOf(selectedCategory)
                    println(
                        "Publicado: { " +
                                "titulo: \"$title\", " +
                                "descripcion: \"$description\", " +
                                "recompensa: $recompensaInt, " +
                                "categoria: $categoriaIndex " +
                                "}"
                    )
                },
                enabled = isInsideCampus,
                border = BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Publicar")
            }

            // Si no esta dentro del campus ewe
            if (!isInsideCampus) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No estás dentro del campus",
                    color = Color.DarkGray
                )
            }
        }
    }
}
