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
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.util.TelemetryLogger
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun CreateFavorScreen(
    navController: NavController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean,
    telemetryLogger: TelemetryLogger,
    favorRepository: FavorRepository,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() } // Start time for response time measurement

    // Notify the parent of the current screen for crash reporting
    LaunchedEffect(Unit) {
        onScreenChange("CreateFavorScreen")
    }

    // Inyección de ViewModels con Hilt
    val favorViewModel: FavorViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    // Cargamos la información del usuario y los favores desde la base de datos
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
        favorViewModel.fetchFavors()
    }
    val currentUser by userViewModel.user.collectAsState(initial = null)
    val allFavors by favorViewModel.favors.collectAsState(initial = emptyList())

    // Chequeo de ubicación
    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        Log.i("Locacion", "¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}")
        result
    } else {
        false
    }

    // Log response time after the screen is composed
    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("CreateFavorScreen", responseTime)
        }
    }

    // Función auxiliar para convertir la fecha de tipo String a milisegundos desde la época
    fun convertDateToMillis(date: String): Long {
        // La fecha viene en formato "yyyy-MM-dd'T'HH:mm:ss" o "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        val dateParts = date.split("T")
        val dateComponents = dateParts[0].split("-")
        val timeComponents = dateParts[1].split(":")

        val year = dateComponents[0].toInt()
        val month = dateComponents[1].toInt()
        val day = dateComponents[2].toInt()

        val hour = timeComponents[0].toInt()
        val minute = timeComponents[1].toInt()
        val second = timeComponents[2].split(".")[0].toInt()

        // Aquí asumimos que las fechas son locales y estamos calculando un timestamp simple
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, second)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    fun calculateAverageAcceptanceTime(favors: List<Favor>, category: String): Long {
        // Filtramos los favores de la categoría que tengan favor_time no nulo
        val validFavors = favors.filter { it.category == category && it.favor_time != null }
        if (validFavors.isEmpty()) return 0

        val totalMinutes = validFavors.sumOf { favor ->
            try {
                // Convertimos las fechas 'created_at' y 'favor_time' a Longs representando los milisegundos desde la época (1970-01-01T00:00:00Z)
                val created = convertDateToMillis(favor.created_at)
                val accepted = convertDateToMillis(favor.favor_time!!)

                // Calculamos la diferencia en minutos
                (accepted - created) / 60000 // 60000 milisegundos en un minuto
            } catch (e: Exception) {
                // En caso de error, ignoramos este favor y seguimos con el siguiente
                0L
            }
        }

        // Si se omitieron todos los favores por error, devolvemos un valor por defecto según la categoría
        return if (totalMinutes > 0) {
            totalMinutes / validFavors.size
        } else {
            when (category) {
                "Favor" -> 5L
                "Compra" -> 10L
                "Tutoría" -> 15L
                else -> throw IllegalArgumentException("Categoría desconocida: $category")
            }
        }
    }

    var selectedItem by remember { mutableStateOf(1) } // Ítem seleccionado (1: Crear Favor)

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

            // "Descripción" y su caja de texto
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
                    val categories = listOf("Favor", "Compra", "Tutoría")
                    for (category in categories) {
                        val categoryColor = when (category) {
                            "Favor" -> FavorCategoryColor
                            "Compra" -> CompraCategoryColor
                            "Tutoría" -> TutoriaCategoryColor
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

            // Sección para mostrar el tiempo promedio de aceptación según la categoría seleccionada
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tiempo promedio de aceptación de un $selectedCategory: ${calculateAverageAcceptanceTime(allFavors, selectedCategory)} minutos",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Log.e("Locacion", locationHelper.currentLocation.toString())
            val currentLocation = locationHelper.currentLocation.value

            val latitud = currentLocation?.latitude ?: 0.0
            val longitud = currentLocation?.longitude ?: 0.0

            // Botón "Publicar" que envía el favor a la base de datos
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val currentTime = Clock.System.now().toString()
                        val currentUserId = currentUser?.id ?: ""
                        Log.e("CurrentUserId", "El id: $currentUserId")
                        val newFavor = Favor(
                            title = title,
                            description = description,
                            category = selectedCategory,
                            reward = recompensa.toInt(),
                            favor_time = null,
                            created_at = currentTime,
                            request_user_id = currentUserId,
                            accept_user_id = "",
                            latitude = latitud,
                            longitude = longitud
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

            // Advertencia si el usuario no está dentro del campus
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