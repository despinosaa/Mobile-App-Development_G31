package com.example.senefavores.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.data.model.Review
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ReviewScreen(
    navController: NavController,
    favorId: String,
    requestUserId: String,
    acceptUserId: String,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel()
) {
    val userInfo by userViewModel.user.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var stars: Int by remember { mutableStateOf<Int>(0) }
    val coroutineScope = rememberCoroutineScope()
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

    // Log userInfo, favorId, requestUserId, acceptUserId
    LaunchedEffect(userInfo, favorId, requestUserId, acceptUserId) {
        Log.d("ReviewScreen", "UserInfo: id=${userInfo?.id}, name=${userInfo?.name}, favorId=$favorId, requestUserId=$requestUserId, acceptUserId=$acceptUserId")
    }

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "Hacer Reseña",
                onAccountClick = { navController.navigate("account") }
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
                    value = stars?.toString() ?: "",
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

            Button(
                onClick = {
                    if (userInfo?.id == null) {
                        Log.d("ReviewScreen", "Submit failed: User not authenticated")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Usuario no autenticado")
                        }
                        return@Button
                    }
                    if (title.isBlank() || description.isBlank() || stars == null) {
                        Log.d("ReviewScreen", "Submit failed: Empty fields (title=$title, description=$description, stars=$stars)")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Por favor, completa todos los campos")
                        }
                        return@Button
                    }
                    if (acceptUserId.isEmpty()) {
                        Log.d("ReviewScreen", "Submit failed: Invalid acceptUserId")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("ID de usuario aceptado inválido")
                        }
                        return@Button
                    }
                    coroutineScope.launch {
                        try {
                            Log.d("ReviewScreen", "Creating review: id=$favorId, reviewer_id=${userInfo!!.id}, reviewed_id=$acceptUserId")
                            val review = Review(
                                id = favorId,
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
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error al enviar reseña: ${e.localizedMessage}")
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MikadoYellow,
                    contentColor = BlackTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Enviar Reseña", fontSize = 16.sp)
            }
        }
    }
}