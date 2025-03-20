package com.example.senefavores.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.ui.viewmodel.UserViewModel

@Composable
fun AccountScreen(navController: NavController, userViewModel: UserViewModel = hiltViewModel()) {
    val user by userViewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pantalla de Cuenta")

        Spacer(modifier = Modifier.height(16.dp))

        user?.let { user ->
            Text("ID: ${user.id}")
            Text("Email: ${user.email}")
        } ?: Text("Cargando información del usuario...")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Volver a Home")
        }
    }
}
