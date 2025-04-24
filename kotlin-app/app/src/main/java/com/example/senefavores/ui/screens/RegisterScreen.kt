package com.example.senefavores.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.ui.components.CustomButton
import com.example.senefavores.ui.viewmodel.UserViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAuthenticated by userViewModel.isAuthenticated.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Navigate to signIn on successful authentication
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Log.d("RegisterScreen", "Auth successful, navigating to signIn")
            navController.navigate("signIn") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
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
            onValueChange = { if (it.length <= 50) email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { if (it.length <= 21) password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        // Confirm Password TextField
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { if (it.length <= 21) confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        // Warning Text
        Text(
            text = "La contraseña debe tener entre 8 y 21 caracteres.",
            fontSize = 12.sp,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Register Button
        CustomButton(
            text = if (isLoading) "Registrando..." else "Registrarse",
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                    if (password.length < 8 || confirmPassword.length < 8) {
                        Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    } else if (!email.endsWith("@uniandes.edu.co")) {
                        Toast.makeText(context, "Correo debe terminar en @uniandes.edu.co", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        userViewModel.signUpWithEmail(email, password) { success, errorMessage ->
                            isLoading = false
                            if (success) {
                                Log.d("RegisterScreen", "Sign-up successful")
                                Toast.makeText(context, "Registro exitoso. Verifica tu correo.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, errorMessage ?: "Error al registrarse", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            backgroundColor = Color(0xFF4CAF50), // Green for sign-in
            textColor = Color.White,
            hasBorder = false,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Debes verificar tu cuenta con el enlace enviado a tu correo.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Sign-In Link
        Text(
            text = "¿Si tienes una cuenta? Ingresa",
            fontSize = 16.sp,
            color = Color.Blue,
            style = TextStyle(textDecoration = TextDecoration.Underline),
            modifier = Modifier
                .clickable { navController.navigate("signIn") }
                .padding(bottom = 8.dp)
        )
    }
}
