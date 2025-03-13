package com.example.senefavores.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
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
import java.io.IOException

@Composable
fun LogInScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo
        Image(
            painter = painterResource(R.drawable.ic_logo_cabra),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(300.dp)
                .padding(top = 80.dp, bottom = 46.dp),
            contentScale = ContentScale.Fit
        )

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "🙈" else "👁️",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(48.dp))
        // Log In Button (Black with White Text)
        CustomButton(
            text = "Iniciar sesión",
            onClick = { navController.navigate("home") },
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

fun loadBitmapFromAssets(context: Context, assetName: String): android.graphics.Bitmap? {
    return try {
        val inputStream = context.assets.open(assetName)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}