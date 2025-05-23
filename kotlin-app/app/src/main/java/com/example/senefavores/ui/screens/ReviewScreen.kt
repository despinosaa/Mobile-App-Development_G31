package com.example.senefavores.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.Review
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import com.example.senefavores.util.formatTime2
import com.example.senefavores.util.truncateText
import kotlinx.coroutines.launch
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewScreen(
    navController: NavController,
    favorId: String,
    requestUserId: String,
    acceptUserId: String,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    telemetryLogger: TelemetryLogger,
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() }

    // Notify parent of current screen
    LaunchedEffect(Unit) {
        onScreenChange("ReviewScreen")
    }

    // Log response time
    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("ReviewScreen", responseTime)
        }
    }

    val userInfo by userViewModel.user.collectAsState()
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var stars by remember { mutableStateOf(0) }
    var favor by remember { mutableStateOf<Favor?>(null) }
    var acceptUserName by remember { mutableStateOf("Cargando...") }
    var selectedItem by remember { mutableStateOf(2) } // History selected
    val snackbarHostState = remember { SnackbarHostState() }
    val starsOptions = listOf(1, 2, 3, 4, 5)
    var expanded by remember { mutableStateOf(false) }

    // Load user if not loaded
    LaunchedEffect(userInfo) {
        if (userInfo == null) {
            Log.d("ReviewScreen", "UserInfo is null, attempting to load user")
            userViewModel.loadUserClientInfo()
        }
    }

    // Fetch favor and accept user's name
    LaunchedEffect(favorId, acceptUserId, isOnline) {
        if (isOnline) {
            favor = favorViewModel.getFavorById(favorId)
            acceptUserName = if (acceptUserId.isNotEmpty()) {
                userViewModel.getClientById(acceptUserId)?.name ?: "Usuario desconocido"
            } else {
                "Ninguno"
            }
            Log.d("ReviewScreen", "Favor: $favor, AcceptUserName: $acceptUserName")
        }
    }

    // Log userInfo, favorId, requestUserId, acceptUserId
    LaunchedEffect(userInfo, favorId, requestUserId, acceptUserId) {
        Log.d("ReviewScreen", "UserInfo: id=${userInfo?.id}, name=${userInfo?.name}, favorId=$favorId, requestUserId=$requestUserId, acceptUserId=$acceptUserId")
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SenefavoresHeader(
                    title = "Hacer Reseña",
                    onAccountClick = { navController.navigate("account") }
                )
            }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Favor and User Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Favor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (favor != null) {
                    Text(
                        text = AnnotatedString.Builder().apply {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Título: ")
                            }
                            append(truncateText(favor!!.title))
                        }.toAnnotatedString(),
                        fontSize = 14.sp
                    )
                    Text(
                        text = AnnotatedString.Builder().apply {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Descripción: ")
                            }
                            append(truncateText(favor!!.description))
                        }.toAnnotatedString(),
                        fontSize = 14.sp
                    )
                    Text(
                        text = AnnotatedString.Builder().apply {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Hora: ")
                            }
                            append(favor!!.favor_time?.let { formatTime2(it) } ?: "N/A")
                        }.toAnnotatedString(),
                        fontSize = 14.sp
                    )
                    Text(
                        text = AnnotatedString.Builder().apply {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Estado: ")
                            }
                            append(
                                when (favor!!.status) {
                                    "pending" -> "Pendiente"
                                    "accepted" -> "Aceptado"
                                    "done" -> "Completado"
                                    else -> "Desconocido"
                                }
                            )
                        }.toAnnotatedString(),
                        fontSize = 14.sp
                    )
                    Text(
                        text = AnnotatedString.Builder().apply {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Senetendero: ")
                            }
                            append(truncateText(acceptUserName))
                        }.toAnnotatedString(),
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = if (isOnline) "Cargando información del favor..." else "Sin conexión, información no disponible",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Review Form
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedTextField(
                    value = if (stars == 0) "" else stars.toString(),
                    onValueChange = { /* Read-only */ },
                    label = { Text("Estrellas") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    starsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.toString()) },
                            onClick = {
                                stars = option
                                expanded = false
                            }
                        )
                    }
                }
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent
                    )
                ) {
                    // Invisible button to trigger dropdown
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (userInfo?.id == null) {
                            Log.d("ReviewScreen", "Submit failed: User not authenticated")
                            scope.launch {
                                snackbarHostState.showSnackbar("Usuario no autenticado")
                            }
                            return@Button
                        }
                        if (title.isBlank() || description.isBlank() || stars == 0) {
                            Log.d("ReviewScreen", "Submit failed: Empty fields (title=$title, description=$description, stars=$stars)")
                            scope.launch {
                                snackbarHostState.showSnackbar("Por favor, completa todos los campos")
                            }
                            return@Button
                        }
                        if (acceptUserId.isEmpty()) {
                            Log.d("ReviewScreen", "Submit failed: Invalid acceptUserId")
                            scope.launch {
                                snackbarHostState.showSnackbar("ID de usuario aceptado inválido")
                            }
                            return@Button
                        }
                        if (favorId.isEmpty()) {
                            Log.d("ReviewScreen", "Submit failed: Invalid favorId")
                            scope.launch {
                                snackbarHostState.showSnackbar("ID de favor inválido")
                            }
                            return@Button
                        }
                        if (!isOnline) {
                            Log.d("ReviewScreen", "Submit failed: No internet connection")
                            scope.launch {
                                snackbarHostState.showSnackbar("No hay conexión a internet")
                            }
                            return@Button
                        }
                        scope.launch {
                            try {
                                val reviewId = UUID.randomUUID().toString() // Generate unique ID for the review
                                Log.d("ReviewScreen", "Creating review: id=$reviewId, favor_id=$favorId, reviewer_id=${userInfo!!.id}, reviewed_id=$acceptUserId")
                                val review = Review(
                                    id = reviewId, // Unique ID for the review
                                    favor_id = favorId, // Link to the favor
                                    title = title,
                                    description = description,
                                    stars = stars,
                                    reviewer_id = userInfo!!.id,
                                    reviewed_id = acceptUserId
                                )
                                Log.d("ReviewScreen", "Submitting review: $review")
                                favorViewModel.addReview(review)
                                Log.d("ReviewScreen", "Review submitted successfully")
                                navController.navigate("history") { launchSingleTop = true }
                            } catch (e: Exception) {
                                Log.e("ReviewScreen", "Error submitting review: ${e.localizedMessage}, cause: ${e.cause}", e)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al enviar reseña: ${e.localizedMessage}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MikadoYellow,
                        contentColor = BlackTextColor
                    ),
                    modifier = Modifier.height(50.dp),
                    enabled = isOnline
                ) {
                    Text(text = "Enviar Reseña", fontSize = 16.sp)
                }
            }

            // No Connectivity Message
            if (!isOnline) {
                Text(
                    text = "No hay conexión a internet",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        navController.navigate("history") { launchSingleTop = true }
                    },
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "Volver a Historial",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}