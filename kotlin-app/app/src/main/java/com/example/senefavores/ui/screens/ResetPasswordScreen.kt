package com.example.senefavores.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.ui.components.CustomButton
import com.example.senefavores.ui.viewmodel.UserViewModel

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    token: String,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showChoiceDialog by remember { mutableStateOf(false) }

    // Check session on load
    LaunchedEffect(token) {
        if (token.isNotBlank()) {
            userViewModel.exchangeCodeForSession(token) { success ->
                isLoading = false
                if (success) {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                } else {
                    Toast.makeText(context, "Enlace inválido o expirado", Toast.LENGTH_LONG).show()
                    navController.navigate("signIn") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        } else {
            isLoading = false
            Toast.makeText(context, "Código de autenticación inválido", Toast.LENGTH_LONG).show()
            navController.navigate("signIn") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    // Choice Dialog (Reset Password or Log In)
    if (showChoiceDialog) {
        AlertDialog(
            onDismissRequest = { /* Non-dismissable */ },
            title = { Text("Verificación Exitosa") },
            text = { Text("¿Deseas restablecer tu contraseña o iniciar sesión directamente?") },
            confirmButton = {
                Button(
                    onClick = {
                        showChoiceDialog = false
                        // Stay on ResetPasswordScreen for password reset
                    }
                ) {
                    Text("Restablecer Contraseña")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showChoiceDialog = false
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Iniciar Sesión")
                }
            }
        )
    }

    // Main Password Reset UI
    if (!showChoiceDialog) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Restablecer Contraseña",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // New Password TextField
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva Contraseña") },
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
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Reset Password Button
            CustomButton(
                text = if (isLoading) "Restableciendo..." else "Restablecer",
                onClick = {
                    if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword == confirmPassword) {
                        isLoading = true
                        userViewModel.resetPasswordFinal(newPassword) { success ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Contraseña restablecida. Inicia sesión.", Toast.LENGTH_LONG).show()
                                navController.navigate("signIn") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                Toast.makeText(context, "Error al restablecer la contraseña.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else if (newPassword != confirmPassword) {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                backgroundColor = Color(0xFF4CAF50), // Green
                textColor = Color.White,
                hasBorder = false,
                enabled = !isLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            )

            // Back to SignIn Button
            TextButton(
                onClick = {
                    navController.navigate("signIn") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Volver al Inicio de Sesión", color = Color.Blue)
            }
        }
    }
}