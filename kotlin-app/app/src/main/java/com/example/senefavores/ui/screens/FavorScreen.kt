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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BlackButtons
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.theme.WhiteTextColor
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.LocationHelper

@Composable
fun FavorScreen(
    navController: NavController,
    favor: Favor,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var selectedItem by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var userStars by remember { mutableStateOf(0f) }
    var userName by remember { mutableStateOf("") }  // Store user's name

// Fetch user details from ViewModel
    LaunchedEffect(favor.request_user_id) {
        favor.request_user_id?.takeIf { it.isNotEmpty() }?.let { userId ->
            val user = userViewModel.getClientById(userId)
            user?.let {
                userStars = it.stars ?: 0f
                userName = it.name ?: "Unknown"  // Assuming `name` exists in the user object
            }
        }
    }


    val isInsideCampus = if (hasLocationPermission) {
        val result = locationHelper.isInsideCampus(locationHelper.currentLocation.value)
        println("¿Dentro del campus? $result, Ubicación: ${locationHelper.currentLocation.value}")
        result
    } else {
        false
    }

    val isAcceptEnabled = when (favor.category) {
        "Favor", "Compra" -> isInsideCampus
        "Tutoría" -> true
        else -> true
    }

    val categoryColor = when (favor.category) {
        "Favor" -> FavorCategoryColor
        "Compra" -> CompraCategoryColor
        "Tutoría" -> TutoriaCategoryColor
        else -> FavorCategoryColor
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
            Text(
                text = favor.title,
                fontSize = 30.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = favor.description,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recompensa: ${favor.reward}",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        text = userName,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingStars(rating = userStars)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Button(
                onClick = { if (isAcceptEnabled) showDialog = true },
                enabled = isAcceptEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MikadoYellow,
                    disabledContainerColor = MikadoYellow.copy(alpha = 0.5f)
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