package com.example.senefavores.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.senefavores.ui.components.ReviewCard
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.ui.components.RatingStars
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel()
) {
    val user by userViewModel.user.collectAsState()
    val error by userViewModel.error.collectAsState()
    val reviews by favorViewModel.userReviews.collectAsState()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf(3) } // Account is index 3
    val coroutineScope = rememberCoroutineScope()

    // Map of reviewer_id to name
    val reviewerNames = remember { mutableStateMapOf<String, String?>() }

    // Load user and reviews
    LaunchedEffect(Unit) {
        userViewModel.loadUserClientInfo()
    }
    LaunchedEffect(user) {
        user?.let { favorViewModel.fetchUserReviews(it.id) }
    }

    // Fetch reviewer names for each review
    LaunchedEffect(reviews) {
        reviews.forEach { review ->
            if (!reviewerNames.containsKey(review.reviewer_id)) {
                coroutineScope.launch {
                    val client = userViewModel.getClientById(review.reviewer_id)
                    reviewerNames[review.reviewer_id] = client?.name
                }
            }
        }
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
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
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
            when {
                error != null -> {
                    Text(
                        text = error ?: "Error desconocido",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                }
                user != null -> {
                    Text(
                        text = user!!.name?.takeIf { it.isNotEmpty() } ?: "Sin nombre",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    RatingStars(rating = user!!.stars)
                    Text(
                        text = user!!.email,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = user!!.phone?.takeIf { it.isNotEmpty() } ?: "Sin teléfono",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                else -> {
                    Text(
                        text = "Cargando información del usuario...",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                }
            }

            // Scrollable List of Reviews
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    error != null -> {
                        item {
                            Text(
                                text = "Error al cargar reseñas",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    user == null -> {
                        item {
                            Text(
                                text = "Cargando reseñas...",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    reviews.isEmpty() -> {
                        item {
                            Text(
                                text = "No hay reseñas",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {
                        items(reviews) { review ->
                            ReviewCard(
                                review = review,
                                reviewerName = reviewerNames[review.reviewer_id]
                            )
                        }
                    }
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
                    onClick = {
                        navController.navigate("resetPassword")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cambiar contraseña")
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