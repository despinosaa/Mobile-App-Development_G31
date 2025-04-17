package com.example.senefavores.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.ui.components.CustomButton
import com.example.senefavores.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay

@Composable
fun SignInScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAuthenticated by userViewModel.isAuthenticated.collectAsStateWithLifecycle()
    var isLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authAttempt by remember { mutableStateOf(0) } // Track sign-in/sign-up attempts

    LaunchedEffect(authAttempt) {
        if (authAttempt > 0) {
            Log.d("SignInScreen", "Auth attempt started: $authAttempt")
            delay(2000) // Wait 2 seconds
            if (isAuthenticated) {
                Log.d("SignInScreen", "Auth successful, navigating to home")
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                Log.d("SignInScreen", "Auth failed, resetting loading")
                isLoading = false
                Toast.makeText(context, "Inicio de sesión fallido. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(R.drawable.ic_logo_cabra),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 48.dp),
            contentScale = ContentScale.Fit
        )

        // Email TextField
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        // Normal Sign-In Button
        CustomButton(
            text = if (isLoading) "Iniciando sesión..." else "Iniciar sesión",
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    userViewModel.signInWithEmail(email, password)
                    authAttempt++ // Trigger delayed check
                } else {
                    Toast.makeText(context, "Por favor, ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
                }
            },
            backgroundColor = Color(0xFF4CAF50), // Green for sign-in
            textColor = Color.White,
            hasBorder = false,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Normal Sign-Up Button
        CustomButton(
            text = if (isLoading) "Registrando..." else "Registrarse",
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    userViewModel.signUpWithEmail(email, password)
                    authAttempt++ // Trigger delayed check
                } else {
                    Toast.makeText(context, "Por favor, ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
                }
            },
            backgroundColor = Color(0xFF2196F3), // Blue for sign-up
            textColor = Color.White,
            hasBorder = false,
            enabled = !isLoading
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(30.dp),
                color = Color(0xFF4CAF50)
            )
        }
    }
}