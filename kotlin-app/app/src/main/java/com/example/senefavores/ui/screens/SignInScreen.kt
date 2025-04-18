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
import androidx.compose.ui.text.input.VisualTransformation
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
fun SignInScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isAuthenticated by userViewModel.isAuthenticated.collectAsStateWithLifecycle()
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Navigate to home on successful authentication
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Log.d("SignInScreen", "Auth successful, navigating to home")
            navController.navigate("home") {
                popUpTo("signIn") { inclusive = true }
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
            label = { Text("Correo Electrónico") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // Password TextField with Eye Icon
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (isPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye_on
                        ),
                        contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        // Login Button
        CustomButton(
            text = if (isLoading) "Iniciando..." else "Iniciar Sesión",
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    userViewModel.signInWithEmail(email, password) { success, errorMessage ->
                        isLoading = false
                        if (success) {
                            Log.d("SignInScreen", "Sign-in successful")
                        } else {
                            Toast.makeText(context, errorMessage ?: "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                        }
                    }
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

        // Register Link
        Text(
            text = "¿No tienes una cuenta? Regístrate",
            fontSize = 16.sp,
            color = Color.Blue,
            style = TextStyle(textDecoration = TextDecoration.Underline),
            modifier = Modifier
                .clickable {
                    Log.d("SignInScreen", "Register link clicked, navigating to register")
                    navController.navigate("register")
                }
                .padding(bottom = 8.dp)
        )

        // Password Reset Link
        Text(
            text = "¿Olvidaste tu contraseña?",
            fontSize = 16.sp,
            color = Color.Blue,
            style = TextStyle(textDecoration = TextDecoration.Underline),
            modifier = Modifier
                .clickable { showPasswordResetDialog = true }
                .padding(bottom = 8.dp)
        )

        // Password Reset Dialog
        if (showPasswordResetDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordResetDialog = false },
                title = { Text("Recuperar Contraseña") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
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
                            if (resetEmail.isNotBlank()) {
                                isLoading = true
                                userViewModel.sendPasswordResetEmail(resetEmail) { success ->
                                    isLoading = false
                                    if (success) {
                                        showPasswordResetDialog = false
                                        Toast.makeText(context, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Email no registrado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Por favor, ingresa un correo", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = !isLoading
                    ) {
                        Text(if (isLoading) "Enviando..." else "Enviar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordResetDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}