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
    val startTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        onScreenChange("CreateFavorScreen")
    }

    val favorViewModel: FavorViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        userViewModel.loadUserClientInfo()
        favorViewModel.fetchAllFavors()
    }
    val currentUser by userViewModel.user.collectAsState(initial = null)
    val allFavors by favorViewModel.allFavors.collectAsState(initial = emptyList())

    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        Log.i("Locacion", "¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}")
        result
    } else {
        false
    }

    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("CreateFavorScreen", responseTime)
        }
    }

    // Estados de campo + errores
    var selectedCategory by remember { mutableStateOf("Favor") }

    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }

    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    var recompensa by remember { mutableStateOf("") }
    var rewardError by remember { mutableStateOf<String?>(null) }

    // Validación final del formulario
    val isFormValid = titleError.isNullOrEmpty()
            && descriptionError.isNullOrEmpty()
            && rewardError.isNullOrEmpty()
            && title.isNotBlank()
            && description.isNotBlank()
            && recompensa.isNotBlank()

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "SeneFavores",
                onAccountClick = { navController.navigate("account") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 1,
                onItemClick = { idx ->
                    when (idx) {
                        0 -> navController.navigate("home") { launchSingleTop = true }
                        1 -> {} // estamos aquí
                        2 -> navController.navigate("history") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // TÍTULO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Título:")
                OutlinedTextField(
                    value = title,
                    onValueChange = { text ->
                        title = text
                        titleError = when {
                            text.isBlank() -> "Falta llenar el título"
                            text.length > 50 -> "El título no puede tener más de 50 caracteres"
                            else -> null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    isError = titleError != null,
                    singleLine = true
                )
            }
            titleError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DESCRIPCIÓN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Descripción:")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { text ->
                    description = text
                    descriptionError = when {
                        text.isBlank() -> "Falta llenar la descripción"
                        text.length > 500 -> "La descripción no puede tener más de 500 caracteres"
                        else -> null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp),
                maxLines = 6,
                isError = descriptionError != null
            )
            descriptionError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RECOMPENSA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recompensa:")
                OutlinedTextField(
                    value = recompensa,
                    onValueChange = { text ->
                        recompensa = text
                        rewardError = when {
                            text.isBlank() -> "Falta la recompensa"
                            text.toIntOrNull() == null -> "La recompensa debe ser un número"
                            text.toInt() > 10_000_000 -> "La recompensa no puede ser mayor a 10 000 000"
                            else -> null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    isError = rewardError != null,
                    singleLine = true
                )
            }
            rewardError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CATEGORÍA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
<<<<<<< Updated upstream
                Text(text = "Categoría:")
=======
>>>>>>> Stashed changes
                Spacer(modifier = Modifier.width(8.dp))
                val categories = listOf("Favor", "Compra", "Tutoría")
                for (cat in categories) {
                    val catColor = when (cat) {
                        "Favor" -> FavorCategoryColor
                        "Compra" -> CompraCategoryColor
                        "Tutoría" -> TutoriaCategoryColor
                        else -> Color.Black
                    }
                    val selected = (selectedCategory == cat)
                    OutlinedButton(
                        onClick = { selectedCategory = cat },
                        border = BorderStroke(1.dp, Color.Black),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) catColor else Color.White,
                            contentColor = if (selected) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(cat)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TIEMPO PROMEDIO
            Text(
                text = "Tiempo promedio de aceptación de un $selectedCategory: ${
                    calculateAverageAcceptanceTime(allFavors, selectedCategory)
                } minutos",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN PUBLICAR
            val currentUserId = currentUser?.id ?: ""
            val currentLocation = locationHelper.currentLocation.value
            val latitud = currentLocation?.latitude ?: 0.0
            val longitud = currentLocation?.longitude ?: 0.0

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val currentTime = Clock.System.now().toString()
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
                enabled = isInsideCampus && isFormValid,
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

// --- Funciones auxiliares (puedes moverlas a un archivo util si lo prefieres) ---
fun convertDateToMillis(date: String): Long {
    val dateParts = date.split("T")
    val dateComponents = dateParts[0].split("-")
    val timeComponents = dateParts[1].split(":")

    val year = dateComponents[0].toInt()
    val month = dateComponents[1].toInt()
    val day = dateComponents[2].toInt()

    val hour = timeComponents[0].toInt()
    val minute = timeComponents[1].toInt()
    val second = timeComponents[2].split(".")[0].toInt()

    val calendar = java.util.Calendar.getInstance()
    calendar.set(year, month - 1, day, hour, minute, second)
    calendar.set(java.util.Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}

fun calculateAverageAcceptanceTime(favors: List<Favor>, category: String): Long {
    // Filter favors by category and ensure both created_at and favor_time are non-null
    val validFavors = favors.filter { favor ->
        favor.category == category && favor.created_at != null && favor.favor_time != null
    }
    if (validFavors.isEmpty()) return when (category) {
        "Favor" -> 5L
        "Compra" -> 10L
        "Tutoría" -> 15L
        else -> 0L
    }

    val totalMinutes = validFavors.sumOf { favor ->
        try {
            val created = convertDateToMillis(favor.created_at!!)
            val accepted = convertDateToMillis(favor.favor_time!!)
            (accepted - created) / 60000
        } catch (e: Exception) {
            Log.e("CreateFavorScreen", "Error calculating time for favor ${favor.id}: created_at=${favor.created_at}, favor_time=${favor.favor_time}", e)
            0L
        }
    }
    return totalMinutes / validFavors.size
}