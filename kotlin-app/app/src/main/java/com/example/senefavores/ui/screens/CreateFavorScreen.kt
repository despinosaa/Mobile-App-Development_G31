package com.example.senefavores.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateFavorScreen(
    navController: NavController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean,
    telemetryLogger: TelemetryLogger,
    favorRepository: FavorRepository,
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() }

    // Conexion
    var isOnline by remember { mutableStateOf(true) }
    var showNoConnectionDialog by remember { mutableStateOf(false) }
    var publicarClick by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()


    LaunchedEffect(Unit) {
        onScreenChange("CreateFavorScreen")
    }

    // Carga inicial de datos
    val favorViewModel: FavorViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        userViewModel.loadUserClientInfo()
        favorViewModel.fetchAllFavors()
    }
    val currentUser by userViewModel.user.collectAsState(initial = null)
    val allFavors by favorViewModel.allFavors.collectAsState(initial = emptyList())

    // Fetch location when permissions are granted
    LaunchedEffect(hasLocationPermission, currentUser) {
        if (hasLocationPermission) {
            locationHelper.getLastLocation(currentUser?.id)
        }
    }

    // Telemetría de tiempo de respuesta
    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("CreateFavorScreen", responseTime)
        }
    }

    // Verificar estado de red al entrar
    LaunchedEffect(Unit) {
        isOnline = networkChecker.isOnline()
        if (!isOnline) {
            Log.d("CreateFavorScreen", "No internet connection detected")
            showNoConnectionDialog = true
        } else {
            favorRepository.processQueuedFavors()
        }
    }

    // Ubicación dentro del campus
    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        Log.i("Locacion", "¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}")
        result
    } else {
        false
    }

    // Estados de formulario
    var selectedCategory by remember { mutableStateOf("Favor") }
    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var recompensa by remember { mutableStateOf("") }
    var rewardError by remember { mutableStateOf<String?>(null) }

    // Validación del formulario
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
                        1 -> {} // aquí
                        2 -> navController.navigate("history") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
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
                                text.filter { it.isLetter() }.length < 5 -> "El título debe tener al menos 5 letras"
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
                            text.filter { it.isLetter() }.length < 5 -> "La descripción debe tener al menos 5 letras"
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
                                text.toInt() > 10_000_000 -> "La recompensa no puede ser mayor a 10 000 000"
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
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Categoría:")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            border = BorderStroke(1.dp, Color.Black),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) catColor else Color.White,
                                contentColor = if (selected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(cat)
                        }
                        if (cat != categories.last()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))


                // TIPS
                var isTipsExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { isTipsExpanded = !isTipsExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tips")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTipsExpanded) "▲" else "▼")
                }
                if (isTipsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Recompensa promedio de $selectedCategory aceptados: $${calculateAverageReward(allFavors, selectedCategory)}")
                        Text("Hora promedio de aceptación: ${calculateAverageAcceptanceHour(allFavors, selectedCategory)}")
                        Text("Tiempo promedio de aceptación: ${calculateAverageAcceptanceTime(allFavors, selectedCategory)} minutos")
                        Text("Usuarios sin respuesta en las ultimas 24h: ${getUsersWithNoResponsesInLast24Hours(allFavors, selectedCategory)}")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // BOTÓN PUBLICAR
                val currentUserId = currentUser?.id ?: ""
                val currentLocation = locationHelper.currentLocation.value
                val latitud = currentLocation?.latitude ?: 0.0
                val longitud = currentLocation?.longitude ?: 0.0
                val canPublishByLocation = (selectedCategory == "Tutoría") || isInsideCampus

                OutlinedButton(
                    onClick = {
                        isOnline = networkChecker.isOnline()
                        if (hasLocationPermission) {
                            locationHelper.getLastLocation(currentUser?.id)
                        }
                        val updatedLocation = locationHelper.currentLocation.value
                        val updatedLatitud = updatedLocation?.latitude ?: 0.0
                        val updatedLongitud = updatedLocation?.longitude ?: 0.0
                        val currentTime = Clock.System.now().toString()
                        if (isOnline) {
                            scope.launch {
                                val newFavor = Favor(
                                    title = title,
                                    description = description,
                                    category = selectedCategory,
                                    reward = recompensa.toInt(),
                                    created_at = currentTime,
                                    request_user_id = currentUserId,
                                    latitude = updatedLatitud,
                                    longitude = updatedLongitud,
                                    status = "pending"
                                )
                                favorViewModel.addFavor(newFavor)
                                navController.navigate("home") { launchSingleTop = true }
                            }
                        } else {
                            scope.launch {
                                val newFavor = Favor(
                                    title = title,
                                    description = description,
                                    category = selectedCategory,
                                    reward = recompensa.toInt(),
                                    created_at = currentTime,
                                    request_user_id = currentUserId,
                                    latitude = updatedLatitud,
                                    longitude = updatedLongitud,
                                    status = "pending"
                                )
                                Log.e("Favor","OfflineQueue")
                                favorRepository.enqueueFavor(newFavor)
                                showNoConnectionDialog = true
                                publicarClick = true
                            }
                        }
                    },
                    enabled = canPublishByLocation && isFormValid,
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

            if (showNoConnectionDialog) {
                AlertDialog(
                    onDismissRequest = { /* opcional: showNoConnectionDialog = false */ },
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin conexión",
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (publicarClick) {
                                Text(
                                    text = "Estás sin conexión, tu favor se pondrá en una cola, si en 5 minutos no recuperas la conexión el favor no se publicará",
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                Text(
                                    text = "Estás sin conexión, si deseas publicar un favor este se pondrá en una cola, si en 5 minutos no recuperas la conexión el favor no se publicará",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = {
                                if (publicarClick) {
                                    navController.navigate("home")
                                }
                                showNoConnectionDialog = false
                                publicarClick = false
                            }) {
                                Text("Entendido")
                            }
                        }
                    }
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

fun calculateAverageReward(favors: List<Favor>, category: String): Int {
    val validFavors = favors.filter { favor ->
        favor.category == category && favor.accept_user_id != null && favor.accept_user_id.isNotEmpty()
    }
    if (validFavors.isEmpty()) return when (category) {
        "Favor" -> 0
        "Compra" -> 0
        "Tutoría" -> 0
        else -> 0
    }

    val totalReward = validFavors.sumOf { favor -> favor.reward.toLong() }
    return (totalReward / validFavors.size).toInt()
}

fun getUsersWithNoResponsesInLast24Hours(favors: List<Favor>, selectedCategory: String): Int {
    val currentTimeMillis = System.currentTimeMillis()
    val twentyFourHoursInMillis = 24 * 60 * 60 * 1000
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    val recentFavors = favors.filter { favor ->
        val favorCreatedAtMillis = favor.created_at?.let {
            val localDateTime = LocalDateTime.parse(it, formatter)
            localDateTime.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        favorCreatedAtMillis != null && favorCreatedAtMillis >= (currentTimeMillis - twentyFourHoursInMillis) &&
                favor.category == selectedCategory
    }

    val count = recentFavors.groupBy { it.request_user_id }
        .count { (_, userFavors) ->
            userFavors.all { favor -> favor.accept_user_id == null || favor.accepted_at == null }
        }

    return count
}

@SuppressLint("DefaultLocale")
fun calculateAverageAcceptanceHour(favors: List<Favor>, category: String): String {
    val validFavors = favors.filter { favor ->
        favor.category == category && favor.accept_user_id != null && favor.accept_user_id.isNotEmpty() && favor.accepted_at != null
    }
    Log.d("CreateFavorScreen", "Valid favors for $category: ${validFavors.size}, accepted_at: ${validFavors.map { it.accepted_at }}")
    if (validFavors.isEmpty()) return "00:00"

    val totalMinutes = validFavors.sumOf { favor ->
        try {
            val timePart = favor.accepted_at!!.split("T")[1] // e.g., "15:00:30.548106+00:00"
            val timeWithoutMicroseconds = timePart.split(".")[0] // e.g., "15:00:30"
            val timeComponents = timeWithoutMicroseconds.split(":") // e.g., ["15", "00", "30"]
            val hours = timeComponents[0].toDouble() // e.g., 15
            val minutes = timeComponents[1].toDouble() // e.g., 0
            val total = hours * 60 + minutes // e.g., 15 * 60 + 0 = 900
            Log.d("CreateFavorScreen", "Time for favor ${favor.id}: ${String.format("%02.0f:%02.0f", hours, minutes)}, total minutes=$total, accepted_at=${favor.accepted_at}")
            total
        } catch (e: Exception) {
            Log.e("CreateFavorScreen", "Error parsing accepted_at for favor ${favor.id}: accepted_at=${favor.accepted_at}", e)
            0.0
        }
    }
    val averageMinutes = totalMinutes / validFavors.size
    val hours = (averageMinutes / 60).toInt()
    val minutes = (averageMinutes % 60).toInt()
    Log.d("CreateFavorScreen", "Average time for $category: $averageMinutes minutes, formatted: ${String.format("%02d:%02d", hours, minutes)}")
    return String.format("%02d:%02d", hours, minutes)
}