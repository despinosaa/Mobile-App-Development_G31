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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.HistoryFavorCard
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class Tab {
    SOLICITADOS,
    ACEPTADOS
}

@Composable
fun TabRow(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Solicitados" tab
        if (selectedTab == Tab.SOLICITADOS) {
            Button(
                onClick = { onTabSelected(Tab.SOLICITADOS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Solicitados")
            }
        } else {
            TextButton(onClick = { onTabSelected(Tab.SOLICITADOS) }) {
                Text(text = "Solicitados")
            }
        }
        // "Aceptados" tab
        if (selectedTab == Tab.ACEPTADOS) {
            Button(
                onClick = { onTabSelected(Tab.ACEPTADOS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Aceptados")
            }
        } else {
            TextButton(onClick = { onTabSelected(Tab.ACEPTADOS) }) {
                Text(text = "Aceptados")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseDateTime(dateTimeStr: String): LocalDateTime {
    val possibleFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    )
    for (formatter in possibleFormats) {
        try {
            return LocalDateTime.parse(dateTimeStr, formatter)
        } catch (e: DateTimeParseException) {
            // Try next format
        }
    }
    throw IllegalArgumentException("Invalid date format: $dateTimeStr")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(Tab.SOLICITADOS) }
    var selectedItem by remember { mutableStateOf(2) } // 2 corresponds to History
    val userInfo by userViewModel.user.collectAsState()
    val allFavors by favorViewModel.allFavors.collectAsState()
    val reviews by favorViewModel.reviews.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load user if not loaded
    LaunchedEffect(userInfo) {
        if (userInfo == null) {
            Log.d("HistoryScreen", "UserInfo is null, attempting to load user")
            userViewModel.loadUserClientInfo()
        }
    }

    // Fetch all favors and reviews
    LaunchedEffect(Unit) {
        Log.d("HistoryScreen", "Fetching all favors and reviews")
        favorViewModel.fetchAllFavors()
        favorViewModel.fetchReviews()
    }

    // Filter and sort favors
    val filteredFavors = allFavors.filter { favor ->
        when (selectedTab) {
            Tab.SOLICITADOS -> favor.request_user_id == userInfo?.id
            Tab.ACEPTADOS -> favor.accept_user_id == userInfo?.id
        }
    }.sortedByDescending { favor ->
        try {
            parseDateTime(favor.created_at)
        } catch (e: IllegalArgumentException) {
            Log.e("HistoryScreen", "Invalid date format for favor ${favor.id}: ${favor.created_at}")
            LocalDateTime.MIN // Fallback to oldest
        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tab row
            TabRow(
                selectedTab = selectedTab,
                onTabSelected = { tab -> selectedTab = tab }
            )

            // Scrollable list of favors
            if (userInfo?.id == null) {
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
                    items(filteredFavors) { favor ->
                        val hasReview = reviews.any { it.id == favor.id }
                        HistoryFavorCard(
                            favor = favor,
                            isSolicitados = selectedTab == Tab.SOLICITADOS,
                            hasReview = hasReview,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}