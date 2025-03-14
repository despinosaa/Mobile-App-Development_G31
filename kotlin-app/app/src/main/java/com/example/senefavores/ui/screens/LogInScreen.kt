package com.example.senefavores.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.ui.components.CustomButton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun LogInScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
<<<<<<< HEAD

        // Logo
        Image(
            painter = painterResource(R.drawable.ic_logo_cabra),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(top = 80.dp, bottom = 46.dp),
            contentScale = ContentScale.Fit
        )
=======
        // Logo from assets
        imageBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(300.dp)
                    .padding(top = 80.dp, bottom = 21.dp),
                contentScale = ContentScale.Fit
            )
        }
>>>>>>> Sinis

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Correo uniandes") },
            //modifier = Modifier .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            //modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "🙈" else "👁️",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            }
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        // Log In Button (Black with White Text)
        CustomButton(
            text = "Iniciar sesión",
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor, llena todos los campos."
                } else {
                    loginUser(username, password, context, navController) { success, message ->
                        if (!success) {
                            errorMessage = message
                        }
                    }
                }
            },
            backgroundColor = Color.Black,
            textColor = Color.White,
            hasBorder = false
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "¿Olvidaste tu contraseña?",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { navController.navigate("forgot") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomButton(
            text = "Crear cuenta",
            onClick = { navController.navigate("register") },
            backgroundColor = Color.White,
            textColor = Color.Black,
            hasBorder = true
        )
    }
}

fun loginUser(
    email: String,
    password: String,
    context: Context,
    navController: NavController,
    onResult: (Boolean, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        //val supabase = supabase // Get your Supabase instance
        /*
        val result = supabase.auth.signInWithPassword(email, password)



        withContext(Dispatchers.Main) {
            if (result.user != null) {
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                navController.navigate("home")
                onResult(true, null)
            } else {
                onResult(false, "Usuario o contraseña incorrectos")
            }
        }

         */
        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
        navController.navigate("home")
        onResult(true, null)
    }
}

fun loadBitmapFromAssets(context: Context, assetName: String): android.graphics.Bitmap? {
    return try {
        val inputStream = context.assets.open(assetName)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}