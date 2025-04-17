package com.example.senefavores.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.senefavores.R
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.viewmodel.UserViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.platform.LocalContext
import kotlin.math.floor

@Composable
fun AccountScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsState()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf(3) } // Account is index 3

    LaunchedEffect(Unit) {
        userViewModel.loadUserClientInfo()
    }

    Scaffold(
        topBar = {
            SenefavoresHeader(
                title = "SeneFavores",
                onAccountClick = { navController.navigate("account") { launchSingleTop = true } }
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
                        3 -> navController.navigate("account") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Picture or Icon
            if (user?.profilePic?.isNotEmpty() == true) {
                Image(
                    painter = rememberAsyncImagePainter(model = user!!.profilePic),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(top = 16.dp, bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                IconButton(
                    onClick = { navController.navigate("account") { launchSingleTop = true } },
                    modifier = Modifier
                        .size(120.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_account),
                        contentDescription = "Profile Icon",
                        modifier = Modifier.size(45.dp)
                    )
                }
            }

            // User Info
            user?.let { user ->
                // Name
                Text(
                    text = user.name?.takeIf { it.isNotEmpty() } ?: "Sin nombre",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Stars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val fullStars = floor(user.stars).toInt()
                    val emptyStars = 5 - fullStars

                    repeat(fullStars) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Filled Star",
                            tint = Color(0xFFFFD700), // Gold
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    repeat(emptyStars) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Empty Star",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%.1f", user.stars),
                        fontSize = 16.sp
                    )
                }

                // Email
                Text(
                    text = user.email,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Phone
                Text(
                    text = user.phone?.takeIf { it.isNotEmpty() } ?: "Sin teléfono",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } ?: Text(
                text = "Cargando información del usuario...",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            // Scrollable List Box
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Lista vacía",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Edit and Logout Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {}, // Edit button, no action yet
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        userViewModel.logout(context)
                        navController.navigate("signIn") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}