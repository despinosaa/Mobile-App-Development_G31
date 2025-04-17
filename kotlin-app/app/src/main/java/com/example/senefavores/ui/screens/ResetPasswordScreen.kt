package com.example.senefavores.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.ui.components.CustomButton
import com.example.senefavores.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    token: String,
    type: String,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showEmailDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Log screen entry
    LaunchedEffect(Unit) {
        Log.d("ResetPasswordScreen", "Reached ResetPasswordScreen with token=$token, type=$type")
        if (type == "recovery" && token.isNotEmpty()) {
            showEmailDialog = true
        } else {
            Log.w("ResetPasswordScreen", "Invalid deep link: token=$token, type=$type")
            Toast.makeText(context, "Enlace inválido", Toast.LENGTH_LONG).show()
            navController.navigate("signIn") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    // Handle system back press
    BackHandler {
        Log.d("ResetPasswordScreen", "Back pressed, navigating to signIn")
        navController.navigate("signIn") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    // Email Input Dialog
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = {
                showEmailDialog = false
                Log.d("ResetPasswordScreen", "Email dialog dismissed, navigating to signIn")
                navController.navigate("signIn") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            title = { Text("Verificar Correo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    userViewModel.verifyRecoveryOtp(email, token)
                                    Log.d("ResetPasswordScreen", "OTP verified successfully for email: $email")
                                    showEmailDialog = false
                                } catch (e: Exception) {
                                    Log.e("ResetPasswordScreen", "Error verifying OTP: ${e.localizedMessage}")
                                    Toast.makeText(context, "Enlace inválido o expirado", Toast.LENGTH_LONG).show()
                                    navController.navigate("signIn") {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Por favor, ingrese su correo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    enabled = !isLoading
                ) {
                    Text("Verificar")
                }
            },
            dismissButton = {
                IconButton(onClick = {
                    showEmailDialog = false
                    Log.d("ResetPasswordScreen", "Email dialog cancelled, navigating to signIn")
                    navController.navigate("signIn") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }) {
                    Text("X", fontSize = 16.sp)
                }
            }
        )
    }

    // Main Password Reset UI
    if (!showEmailDialog) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Restablecer Contraseña", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(
                text = if (isLoading) "Actualizando..." else "Actualizar Contraseña",
                onClick = {
                    if (newPassword.isNotBlank() && newPassword == confirmPassword) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                userViewModel.updatePassword(newPassword)
                                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                Log.d("ResetPasswordScreen", "Password updated, navigating to signIn")
                                navController.navigate("signIn") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Log.e("ResetPasswordScreen", "Error updating password: ${e.localizedMessage}")
                                Toast.makeText(context, "Error al actualizar contraseña", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Las contraseñas no coinciden o están vacías", Toast.LENGTH_SHORT).show()
                    }
                },
                backgroundColor = Color(0xFF4CAF50), // Green for sign-in
                textColor = Color.White,
                hasBorder = false,
                enabled = !isLoading
            )
        }
    }
}