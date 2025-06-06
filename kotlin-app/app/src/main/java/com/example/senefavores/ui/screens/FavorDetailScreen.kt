package com.example.senefavores.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.User
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.RatingStars
import com.example.senefavores.ui.components.RewardText
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.formatTime2
import com.example.senefavores.util.truncateText
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DoneFavorDetailScreen(
    navController: NavController,
    favorJson: String,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(), // Add FavorViewModel to fetch reviews
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val favor = Json.decodeFromString<Favor>(favorJson)
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)
    val reviews by favorViewModel.reviews.collectAsState(initial = emptyList()) // Fetch reviews

    var userName by remember { mutableStateOf("Cargando...") }
    var userRating by remember { mutableStateOf(0.0f) }
    val currentUserId = userViewModel.getCurrentUserId()
    val isSolicitados = favor.request_user_id == currentUserId
    val isAccepter = favor.accept_user_id == currentUserId
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(2) } // "history" tab

    // Recalculate hasReview using favor_id
    val calculatedHasReview = reviews.any { it.favor_id == favor.id }

    // Debug logs for state tracking
    LaunchedEffect(Unit) {
        Log.d("DoneFavorDetailScreen", "Initial state - isSolicitados: $isSolicitados, isAccepter: $isAccepter, hasReview: $calculatedHasReview, currentUserId: $currentUserId, favor.request_user_id: ${favor.request_user_id}, favor.accept_user_id: ${favor.accept_user_id}")
    }

    // Retry loading user data when network status changes to online
    LaunchedEffect(isOnline) {
        if (isOnline && userViewModel.getCurrentUserId() == null) {
            coroutineScope.launch {
                userViewModel.loadUserClientInfo()
                Log.d("DoneFavorDetailScreen", "Retried loading user data due to network change")
            }
        }
    }

    // Force load user data if not loaded
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            if (currentUserId == null) {
                userViewModel.loadUserClientInfo()
                Log.d("DoneFavorDetailScreen", "Forced load of user data")
            }
        }
    }

    LaunchedEffect(favor.id) {
        coroutineScope.launch {
            if (isSolicitados) {
                userName = favor.accept_user_id?.let { acceptId ->
                    userViewModel.getClientById(acceptId)?.name ?: "Usuario desconocido"
                } ?: "Ninguno"
                userRating = favor.accept_user_id?.let { acceptId ->
                    userViewModel.getClientById(acceptId)?.stars ?: 0.0f
                } ?: 0.0f
            } else {
                userViewModel.getClientById(favor.request_user_id)?.let { user ->
                    userName = user.name ?: "Usuario desconocido"
                    userRating = user.stars ?: 0.0f
                }
            }
        }
    }

    // Re-evaluate network status changes
    LaunchedEffect(isOnline) {
        Log.d("DoneFavorDetailScreen", "Network status changed: isOnline=$isOnline")
    }

    LaunchedEffect(Unit) {
        onScreenChange("DoneFavorDetailScreen")
        Log.d("DoneFavorDetailScreen", "isSolicitados: $isSolicitados, isAccepter: $isAccepter, hasReview: $calculatedHasReview, accept_user_id: ${favor.accept_user_id}, currentUserId: $currentUserId")
    }

    val displayTime = if (favor.created_at != null) {
        try {
            formatTime2(favor.created_at)
        } catch (e: IllegalArgumentException) {
            "Invalid time"
        }
    } else {
        "Not specified"
    }

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "Detalles del Favor",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Offline warning
            if (!isOnline) {
                Text(
                    text = "Estás sin conexión",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // Favor details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fecha de Creación: $displayTime",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { /* No action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (favor.category) {
                                    "Favor" -> FavorCategoryColor
                                    "Compra" -> CompraCategoryColor
                                    "Tutoría" -> TutoriaCategoryColor
                                    else -> Color.Gray
                                }
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .width(72.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(
                                text = favor.category,
                                fontSize = 14.sp,
                                color = BlackTextColor,
                                maxLines = 1
                            )
                        }
                        RewardText(favor.reward)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = favor.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = favor.description,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = "Usuario",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSolicitados) "Senetendero: $userName" else "Solicitante: $userName",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingStars(rating = userRating)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estado: Finalizado",
                    fontSize = 14.sp,
                    color = Color.Green
                )
            }

            // Buttons at the bottom
            if (currentUserId != null && (isSolicitados || isAccepter) && !calculatedHasReview && favor.accept_user_id != null) {
                Button(
                    onClick = {
                        navController.navigate("review/${favor.id}/${favor.request_user_id}/${favor.accept_user_id}")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnline) MikadoYellow else Color.Gray,
                        contentColor = if (isOnline) BlackTextColor else Color.LightGray,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isOnline
                ) {
                    Text(text = "Hacer Reseña", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp)) // Add spacing before BottomNavigationBar
            } else {
                Log.d("DoneFavorDetailScreen", "Button not displayed - currentUserId: $currentUserId, isSolicitados: $isSolicitados, isAccepter: $isAccepter, hasReview: $calculatedHasReview, accept_user_id: ${favor.accept_user_id}")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PendingFavorDetailScreen(
    navController: NavController,
    favorJson: String,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val favor = Json.decodeFromString<Favor>(favorJson)
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)
    var localStatus by remember { mutableStateOf(favor.status) }

    var userName by remember { mutableStateOf("Cargando...") }
    var userRating by remember { mutableStateOf(0.0f) }
    val currentUserId = userViewModel.getCurrentUserId()
    val isSolicitados = favor.request_user_id == currentUserId
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf(2) } // "history" tab

    // Debug logs for state tracking
    LaunchedEffect(Unit) {
        Log.d("PendingFavorDetailScreen", "Initial state - isSolicitados: $isSolicitados, currentUserId: $currentUserId, favor.request_user_id: ${favor.request_user_id}")
    }

    // Retry loading user data when network status changes to online
    LaunchedEffect(isOnline) {
        if (isOnline && userViewModel.getCurrentUserId() == null) {
            coroutineScope.launch {
                userViewModel.loadUserClientInfo()
                Log.d("PendingFavorDetailScreen", "Retried loading user data due to network change")
            }
        }
    }

    // Force load user data if not loaded
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            if (currentUserId == null) {
                userViewModel.loadUserClientInfo()
                Log.d("PendingFavorDetailScreen", "Forced load of user data")
            }
        }
    }

    LaunchedEffect(favor.id) {
        coroutineScope.launch {
            if (isSolicitados) {
                userName = favor.accept_user_id?.let { acceptId ->
                    userViewModel.getClientById(acceptId)?.name ?: "Usuario desconocido"
                } ?: "Ninguno"
                userRating = favor.accept_user_id?.let { acceptId ->
                    userViewModel.getClientById(acceptId)?.stars ?: 0.0f
                } ?: 0.0f
            } else {
                userViewModel.getClientById(favor.request_user_id)?.let { user ->
                    userName = user.name ?: "Usuario desconocido"
                    userRating = user.stars ?: 0.0f
                }
            }
        }
    }

    // Re-evaluate network status changes
    LaunchedEffect(isOnline) {
        Log.d("PendingFavorDetailScreen", "Network status changed: isOnline=$isOnline")
    }

    LaunchedEffect(Unit) {
        onScreenChange("PendingFavorDetailScreen")
        Log.d("PendingFavorDetailScreen", "isSolicitados: $isSolicitados, currentUserId: $currentUserId")
    }

    val displayTime = if (favor.created_at != null) {
        try {
            formatTime2(favor.created_at)
        } catch (e: IllegalArgumentException) {
            "Invalid time"
        }
    } else {
        "Not specified"
    }

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "Detalles del Favor",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Offline warning
            if (!isOnline) {
                Text(
                    text = "Estás sin conexión",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // Favor details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fecha de Creación: $displayTime",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { /* No action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (favor.category) {
                                    "Favor" -> FavorCategoryColor
                                    "Compra" -> CompraCategoryColor
                                    "Tutoría" -> TutoriaCategoryColor
                                    else -> Color.Gray
                                }
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .width(72.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(
                                text = favor.category,
                                fontSize = 14.sp,
                                color = BlackTextColor,
                                maxLines = 1
                            )
                        }
                        RewardText(favor.reward)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = favor.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = favor.description,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = "Usuario",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSolicitados) "Senetendero: $userName" else "Solicitante: $userName",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingStars(rating = userRating)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estado: Pendiente",
                    fontSize = 14.sp,
                    color = Color(0xFFFFC107)
                )
            }

            // Buttons at the bottom
            if (currentUserId != null && isSolicitados) {
                Button(
                    onClick = {
                        localStatus = "cancelled"
                        favorViewModel.updateFavorStatus(favor.id.toString(), "cancelled")
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnline) Color.Red else Color.Gray,
                        contentColor = if (isOnline) Color.White else Color.LightGray,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isOnline
                ) {
                    Text(text = "Cancelar", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp)) // Add spacing before BottomNavigationBar
            } else {
                Log.d("PendingFavorDetailScreen", "Button not displayed - currentUserId: $currentUserId, isSolicitados: $isSolicitados")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AcceptedFavorDetailScreen(
    navController: NavController,
    favorJson: String,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val favor = Json.decodeFromString<Favor>(favorJson)
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)
    var localStatus by remember { mutableStateOf(favor.status) }

    var userName by remember { mutableStateOf("Cargando...") }
    var userRating by remember { mutableStateOf(0.0f) }
    val currentUserId = userViewModel.getCurrentUserId()
    val isSolicitados = favor.request_user_id == currentUserId
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var selectedItem by remember { mutableStateOf(2) } // "history" tab

    // Debug logs for state tracking
    LaunchedEffect(Unit) {
        Log.d("AcceptedFavorDetailScreen", "Initial state - isSolicitados: $isSolicitados, currentUserId: $currentUserId, favor.request_user_id: ${favor.request_user_id}, favor.accept_user_id: ${favor.accept_user_id}")
    }

    // Retry loading user data when network status changes to online
    LaunchedEffect(isOnline) {
        if (isOnline && userViewModel.getCurrentUserId() == null) {
            coroutineScope.launch {
                userViewModel.loadUserClientInfo()
                Log.d("AcceptedFavorDetailScreen", "Retried loading user data due to network change")
            }
        }
    }

    // Force load user data if not loaded
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            if (currentUserId == null) {
                userViewModel.loadUserClientInfo()
                Log.d("AcceptedFavorDetailScreen", "Forced load of user data")
            }
        }
    }

    LaunchedEffect(favor.id) {
        coroutineScope.launch {
            if (isSolicitados) {
                userName = favor.accept_user_id?.let { acceptId ->
                    userViewModel.getClientById(acceptId)?.name ?: "Usuario desconocido"
                } ?: "Ninguno"
                userRating = favor.accept_user_id?.let { acceptId ->
                    userViewModel.getClientById(acceptId)?.stars ?: 0.0f
                } ?: 0.0f
            } else {
                userViewModel.getClientById(favor.request_user_id)?.let { user ->
                    userName = user.name ?: "Usuario desconocido"
                    userRating = user.stars ?: 0.0f
                }
            }
        }
    }

    // Re-evaluate network status changes
    LaunchedEffect(isOnline) {
        Log.d("AcceptedFavorDetailScreen", "Network status changed: isOnline=$isOnline, isSolicitados=$isSolicitados")
    }

    LaunchedEffect(Unit) {
        onScreenChange("AcceptedFavorDetailScreen")
        Log.d("AcceptedFavorDetailScreen", "isSolicitados: $isSolicitados, accept_user_id: ${favor.accept_user_id}, currentUserId: $currentUserId")
    }

    val displayTime = if (favor.created_at != null) {
        try {
            formatTime2(favor.created_at)
        } catch (e: IllegalArgumentException) {
            "Invalid time"
        }
    } else {
        "Not specified"
    }

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "Detalles del Favor",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Offline warning
            if (!isOnline) {
                Text(
                    text = "Estás sin conexión",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // Favor details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fecha de Creación: $displayTime",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { /* No action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (favor.category) {
                                    "Favor" -> FavorCategoryColor
                                    "Compra" -> CompraCategoryColor
                                    "Tutoría" -> TutoriaCategoryColor
                                    else -> Color.Gray
                                }
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .width(72.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(
                                text = favor.category,
                                fontSize = 14.sp,
                                color = BlackTextColor,
                                maxLines = 1
                            )
                        }
                        RewardText(favor.reward)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = favor.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = favor.description,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = "Usuario",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSolicitados) "Senetendero: $userName" else "Solicitante: $userName",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingStars(rating = userRating)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estado: Aceptado",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            // Buttons at the bottom, stacked vertically
            if (currentUserId != null && isSolicitados) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Adds spacing between buttons
                ) {
                    Button(
                        onClick = {
                            localStatus = "cancelled"
                            favorViewModel.updateFavorStatus(favor.id.toString(), "cancelled")
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) Color.Red else Color.Gray,
                            contentColor = if (isOnline) Color.White else Color.LightGray,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = isOnline
                    ) {
                        Text(text = "Cancelar", fontSize = 14.sp)
                    }
                    Button(
                        onClick = {
                            localStatus = "done"
                            favorViewModel.updateFavorStatus(favor.id.toString(), "done")
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) MikadoYellow else Color.Gray,
                            contentColor = if (isOnline) BlackTextColor else Color.LightGray,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = isOnline
                    ) {
                        Text(text = "Finalizar", fontSize = 14.sp)
                    }
                    Button(
                        onClick = {
                            favor.accept_user_id?.let { acceptId ->
                                coroutineScope.launch {
                                    userViewModel.getClientById(acceptId)?.let { user ->
                                        selectedUser = user
                                        showDialog = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) Color.Black else Color.Gray,
                            contentColor = if (isOnline) Color.White else Color.LightGray,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = isOnline && favor.accept_user_id != null
                    ) {
                        Text(text = "Senetendero", fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // Add spacing before BottomNavigationBar
            } else {
                Log.d("AcceptedFavorDetailScreen", "Buttons not displayed - currentUserId: $currentUserId, isSolicitados: $isSolicitados")
            }
        }
    }

    // Dialog for Senetendero info
    if (showDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Información del Senetendero")
                    if (!selectedUser?.profilePic.isNullOrEmpty()) {
                        AsyncImage(
                            model = selectedUser?.profilePic,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_account),
                            placeholder = painterResource(R.drawable.ic_account)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_account),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                    Text("Nombre: ${truncateText(selectedUser?.name ?: "Desconocido")}")
                    Text("Correo: ${truncateText(selectedUser?.email ?: "N/A")}")
                    Text("Teléfono: ${truncateText(selectedUser?.phone ?: "N/A")}")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Calificación:")
                        RatingStars(rating = selectedUser?.stars ?: 0.0f)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MikadoYellow,
                            contentColor = BlackTextColor
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }
}