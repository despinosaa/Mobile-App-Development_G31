package com.example.senefavores.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.HistoryFavorCard
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import com.example.senefavores.util.parseDateTime
import java.time.LocalDateTime
import kotlinx.coroutines.launch

enum class Tab {
    SOLICITADOS,
    ACEPTADOS
}

@Composable
fun TabRow(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedTab == Tab.SOLICITADOS) {
            Button(
                onClick = { if (enabled) onTabSelected(Tab.SOLICITADOS) },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Solicitados")
            }
        } else {
            TextButton(
                onClick = { if (enabled) onTabSelected(Tab.SOLICITADOS) },
                enabled = enabled
            ) {
                Text(text = "Solicitados")
            }
        }
        if (selectedTab == Tab.ACEPTADOS) {
            Button(
                onClick = { if (enabled) onTabSelected(Tab.ACEPTADOS) },
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Aceptados")
            }
        } else {
            TextButton(
                onClick = { if (enabled) onTabSelected(Tab.ACEPTADOS) },
                enabled = enabled
            ) {
                Text(text = "Aceptados")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    telemetryLogger: TelemetryLogger,
    favorRepository: FavorRepository,
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val user by userViewModel.user.collectAsState()
    val startTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        onScreenChange("HistoryScreen")
    }

    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("HistoryScreen", responseTime)
        }
    }

    var selectedTab by remember { mutableStateOf(Tab.SOLICITADOS) }
    var selectedItem by remember { mutableStateOf(2) }
    val userInfo by userViewModel.user.collectAsState()
    val allFavorsOr by favorViewModel.allFavors.collectAsState()
    val allFavors = allFavorsOr
    val reviews by favorViewModel.reviews.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)

    var refreshKey by remember { mutableStateOf(0) }

    // Fetch user ID from SharedPreferences if userInfo is null and offline
    val effectiveUserId = userInfo?.id ?: if (!isOnline) userViewModel.getSavedUserId() else null

    LaunchedEffect(userInfo, refreshKey) {
        if (userInfo == null && isOnline) {
            Log.d("HistoryScreen", "UserInfo is null, attempting to load user")
            userViewModel.loadUserClientInfo()
        }
        Log.d("HistoryScreen", "Fetching all favors (refreshKey=$refreshKey)")
        favorViewModel.fetchAllFavors()
        if (isOnline) {
            favorViewModel.fetchReviews()
        }
    }

    val filteredFavors = if (effectiveUserId != null) {
        allFavors.filter { favor ->
            when (selectedTab) {
                Tab.SOLICITADOS -> favor.request_user_id == effectiveUserId
                Tab.ACEPTADOS -> favor.accept_user_id == effectiveUserId
            }
        }.filter { favor ->
            when (selectedTab) {
                Tab.SOLICITADOS -> favor.created_at != null
                Tab.ACEPTADOS -> favor.created_at != null || favor.accepted_at != null
            }
        }.sortedByDescending { favor ->
            try {
                when (selectedTab) {
                    Tab.SOLICITADOS -> parseDateTime(favor.created_at!!)
                    Tab.ACEPTADOS -> favor.accepted_at?.let { parseDateTime(it) } ?: parseDateTime(favor.created_at!!)
                }
            } catch (e: IllegalArgumentException) {
                Log.e("HistoryScreen", "Invalid date format for favor ${favor.id}: created_at=${favor.created_at}, accepted_at=${favor.accepted_at}")
                LocalDateTime.MIN
            }
        }
    } else {
        emptyList()
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTab = selectedTab,
                onTabSelected = { tab -> selectedTab = tab },
                enabled = effectiveUserId != null
            )

            if (!isOnline) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estás sin conexión",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (effectiveUserId == null) {
                        Text(
                            text = "No hay datos de usuario disponibles para mostrar el historial",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (filteredFavors.isEmpty()) {
                        Text(
                            text = if (selectedTab == Tab.SOLICITADOS) {
                                "No tienes favores solicitados"
                            } else {
                                "No tienes favores aceptados"
                            },
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredFavors, key = { it.id.toString() }) { favor ->
                                HistoryFavorCard(
                                    favor = favor,
                                    isSolicitados = selectedTab == Tab.SOLICITADOS,
                                    hasReview = reviews.any { it.id == favor.id },
                                    navController = navController,
                                    userViewModel = userViewModel,
                                    favorViewModel = favorViewModel,
                                    onStatusUpdate = { refreshKey++ },
                                    isOnline = isOnline // Pass isOnline to control buttons, remove enabled = false
                                )
                            }
                        }
                    }
                }
            } else if (userInfo?.id == null) {
                Text(
                    text = "Por favor, inicia sesión para ver tu historial",
                    modifier = Modifier.padding(16.dp)
                )
            } else if (filteredFavors.isEmpty()) {
                Text(
                    text = if (selectedTab == Tab.SOLICITADOS) {
                        "No tienes favores solicitados"
                    } else {
                        "No tienes favores aceptados"
                    },
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredFavors, key = { it.id.toString() }) { favor ->
                        val hasReview = reviews.any { it.id == favor.id }
                        HistoryFavorCard(
                            favor = favor,
                            isSolicitados = selectedTab == Tab.SOLICITADOS,
                            hasReview = hasReview,
                            navController = navController,
                            userViewModel = userViewModel,
                            favorViewModel = favorViewModel,
                            onStatusUpdate = { refreshKey++ },
                            isOnline = isOnline // Pass isOnline to control buttons
                        )
                    }
                }
            }
        }
    }
}