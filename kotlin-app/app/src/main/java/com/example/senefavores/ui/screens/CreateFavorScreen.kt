package com.example.senefavores.ui.screens

import android.util.Log
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.viewmodel.UserViewModel
import kotlinx.datetime.Clock.System

@Composable
fun CreateFavorScreen(
    navController: NavController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean
) {
    var selectedItem by remember { mutableStateOf(1) } // Estado para el ítem seleccionado (1: Crear Favor)

    // Inyectamos los ViewModels necesarios mediante Hilt
    val favorViewModel: FavorViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    // Llamamos a loadUserInfo() para cargar la información del usuario
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }
    // Observamos el StateFlow del usuario
    val currentUser by userViewModel.user.collectAsState(initial = null)

    // Chequeo de ubicación: Si el usuario tiene permisos, se verifica si está dentro del campus
    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        Log.e("Locacion", "¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}")
        result
    } else {
        false
    }

    val scope = rememberCoroutineScope()

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
        // Variables de estado para la información del favor
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

            // Fila para "Título" y su TextField
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
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "Descripción" y su caja de texto más grande
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
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fila para "Recompensa" y su TextField
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
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fila para "Categoría" y botones de selección
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
                            "Favor" -> Color(0xFFFFC0CB)   // rosa
                            "Compra" -> Color(0xFF2196F3)  // azul
                            "Tutoria" -> Color(0xFFF44336) // rojo
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

            // Botón "Publicar" que envía el favor a la base de datos mediante el ViewModel.
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val currentTime = System.now().toString()
                        // Se obtiene el id del usuario actual del StateFlow
                        val currentUserId = currentUser?.id ?: ""
                        Log.e("CurrentUserId", "El id: $currentUserId")
                        // Se crea el objeto Favor con la información ingresada.
                        val newFavor = Favor(
                            title = title,
                            description = description,
                            category = selectedCategory,
                            reward = recompensa,
                            favor_time = currentTime,
                            created_at = currentTime,
                            request_user_id = currentUserId,
                            accept_user_id = ""
                        )
                        favorViewModel.addFavor(newFavor)
                        navController.navigate("home") { launchSingleTop = true }
                    }
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

            // Mensaje de advertencia si el usuario no está dentro del campus.
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
