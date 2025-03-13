package com.example.senefavores.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BlackButtons
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.theme.WhiteTextColor
import com.example.senefavores.util.LocationHelper

@Composable
fun FavorScreen(
    navController: NavController,
    favor: Favor,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean
) {
    var selectedItem by remember { mutableStateOf(0) } // Estado para el ítem seleccionado (0: Home)
    var showDialog by remember { mutableStateOf(false) } // Estado para mostrar el popup

    // Verificar si el usuario está dentro del campus
    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        println("¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}") // Log para depuración
        result
    } else {
        false
    }

    // Habilitar el botón solo si está dentro del campus para "Favor" o "Compra", o si es "Tutoría"
    val isAcceptEnabled = when (favor.category) {
        "Favor", "Compra" -> isInsideCampus
        "Tutoría" -> true
        else -> true
    }

    // Definir el color de la categoría dinámicamente
    val categoryColor = when (favor.category) {
        "Favor" -> FavorCategoryColor
        "Compra" -> CompraCategoryColor
        "Tutoría" -> TutoriaCategoryColor
        else -> FavorCategoryColor // Por defecto
    }

    // Mostrar el popup si showDialog es true
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Cerrar el popup al hacer clic fuera
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showDialog = false }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_close), // Asegúrate de tener este ícono
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¡Favor aceptado!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlackTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Comunícate con ${favor.user} para más detalles.",
                        fontSize = 16.sp,
                        color = BlackTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_phone), // Asegúrate de tener este ícono
                            contentDescription = "Teléfono",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "Número de teléfono",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlackTextColor
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false // Cerrar el popup
                        navController.navigate("history") { launchSingleTop = true } // Navegar a HistoryScreen
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackButtons
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Ir a Mis Favores",
                        fontSize = 16.sp,
                        color = WhiteTextColor
                    )
                }
            },
            containerColor = MikadoYellow,
            modifier = Modifier.padding(16.dp)
        )
    }

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "SeneFavores",
                onAccountClick = { navController.navigate("account") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedItem,
                onItemClick = { index ->
                    selectedItem = index
                    when (index) {
                        0 -> navController.navigate("home") { launchSingleTop = true }
                        1 -> navController.navigate("createFavor") { launchSingleTop = true }
                        2 -> navController.navigate("history") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Título del favor
            Text(
                text = favor.name,
                fontSize = 30.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = favor.description,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recompensa
            Text(
                text = "Recompensa: ${favor.reward}",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categoría: ",
                    fontSize = 16.sp
                )
                Button(
                    onClick = { /* No action */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = categoryColor
                    ),
                    modifier = Modifier
                        .height(35.dp)
                        .widthIn(min = 80.dp, max = 100.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = favor.category,
                        fontSize = 14.sp,
                        color = BlackTextColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Solicitante y calificación
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Solicitado por:",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = "Usuario",
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = favor.user,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingStars(rating = favor.rating.toFloat())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje si no está dentro del campus (solo para "Favor" o "Compra")
            if (!isAcceptEnabled && (favor.category == "Favor" || favor.category == "Compra")) {
                Text(
                    text = if (!hasLocationPermission) {
                        "No puedes aceptar este favor porque no has concedido permisos de ubicación."
                    } else {
                        "No puedes aceptar este favor porque no estás dentro del campus."
                    },
                    fontSize = 14.sp,
                    color = BlackTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Aceptar
            Button(
                onClick = { if (isAcceptEnabled) showDialog = true }, // Mostrar el popup solo si está habilitado
                enabled = isAcceptEnabled, // Habilitar/deshabilitar según la ubicación
                colors = ButtonDefaults.buttonColors(
                    containerColor = MikadoYellow,
                    disabledContainerColor = MikadoYellow.copy(alpha = 0.5f) // Gris claro cuando está deshabilitado
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "ACEPTAR",
                    fontSize = 16.sp,
                    color = BlackTextColor
                )
            }
        }
    }
}

// Componente reutilizable para las estrellas
@Composable
fun RatingStars(rating: Float) {
    Row {
        for (i in 1..5) {
            Image(
                painter = painterResource(
                    if (i <= rating) R.drawable.ic_star_filled else R.drawable.ic_star_empty
                ),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}