package com.example.senefavores.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.ui.components.CustomButton
import com.example.senefavores.ui.viewmodel.UserViewModel

@Composable
fun LogInScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAuthenticated by userViewModel.isAuthenticated.collectAsStateWithLifecycle()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center everything vertically
    ) {
        // Logo
        Image(
            painter = painterResource(R.drawable.ic_logo_cabra),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 80.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Azure Login Button
        CustomButton(
            text = if (isLoading) "Iniciando sesión..." else "Iniciar sesión con Microsoft",
            onClick = {
                isLoading = true
                //userViewModel.signInWithAzure(context)
                //userViewModel.extraFunkysignInWithAzure(context)
                //userViewModel.funkysignInWithAzure(context)
            },
            backgroundColor = Color(0xFF0078D4),
            textColor = Color.White,
            enabled = !isLoading
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(30.dp),
                color = Color(0xFF0078D4)
            )
        }


        // Reset loading state if auth fails
        LaunchedEffect(userViewModel.isAuthenticated) {
            if (!isAuthenticated) {
                isLoading = false

            }
        }
    }
}