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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.data.repository.UserRepository
import com.example.senefavores.ui.components.CustomButton
import com.example.senefavores.ui.theme.BlackButtons
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    telemetryLogger: TelemetryLogger,
    userRepository: UserRepository,
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() }

    // Notify parent of current screen
    LaunchedEffect(Unit) {
        onScreenChange("ResetPasswordScreen")
    }

    // Log response time
    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("ResetPasswordScreen", responseTime)
        }
    }

    val context = LocalContext.current
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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
            onValueChange = { if (it.length <= 22) newPassword = it },
            label = { Text("Nueva Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (isNewPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye_on
                        ),
                        contentDescription = if (isNewPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        // Confirm Password TextField
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { if (it.length <= 22) confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (isConfirmPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye_on
                        ),
                        contentDescription = if (isConfirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        // No Connectivity Message
        if (!isOnline) {
            Text(
                text = "No hay conexión a internet",
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Reset Password Button
        CustomButton(
            text = if (isLoading) "Restableciendo..." else "Restablecer",
            onClick = {
                if (!isOnline) {
                    Toast.makeText(context, "No hay conexión a internet", Toast.LENGTH_SHORT).show()
                    return@CustomButton
                }
                if (newPassword.isNotBlank() && confirmPassword.isNotBlank()) {
                    if (newPassword == confirmPassword) {
                        isLoading = true
                        userViewModel.resetPasswordFinal(newPassword) { success ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Contraseña restablecida", Toast.LENGTH_LONG).show()
                                navController.navigate("account") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                Toast.makeText(context, "Error al restablecer la contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            backgroundColor = MikadoYellow,
            textColor = Color.Black,
            hasBorder = false,
            enabled = !isLoading && isOnline
        )

        // Back to Account Button
        TextButton(
            onClick = {
                navController.navigate("account") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Volver a Cuenta", color = Color.Black)
        }
    }
}