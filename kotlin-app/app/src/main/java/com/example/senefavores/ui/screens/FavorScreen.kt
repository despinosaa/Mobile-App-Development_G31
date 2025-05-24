package com.example.senefavores.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Composable
fun RewardText(reward: Int) {
    val df = remember {
        DecimalFormat("#,###", DecimalFormatSymbols().apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        })
    }
    val formatted = df.format(reward)

    Text(
        text = "Recompensa: $ $formatted",
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

@Composable
fun FavorScreen(
    navController: NavController,
    favor: Favor,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    networkChecker: NetworkChecker,
    telemetryLogger: TelemetryLogger,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() }

    // Notify parent of current screen
    LaunchedEffect(Unit) {
        onScreenChange("FavorScreen")
    }

    // Log response time
    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("FavorScreen", responseTime)
        }
    }

    var selectedItem by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var userStars by remember { mutableStateOf(0f) }
    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val userInfo by userViewModel.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)
    var showNoConnectionDialog by remember { mutableStateOf(false) }

    // Fetch requester's details
    LaunchedEffect(favor.request_user_id) {
        favor.request_user_id.takeIf { it.isNotEmpty() }?.let { userId ->
            Log.d("FavorScreen", "Fetching requester: userId=$userId")
            val user = userViewModel.getClientById(userId)
            user?.let {
                userStars = it.stars ?: 0f
                userName = it.name ?: "Unknown"
                userPhone = it.phone ?: "No phone provided"
                Log.d("FavorScreen", "Requester loaded: name=$userName, phone=$userPhone, stars=$userStars")
            } ?: Log.e("FavorScreen", "Failed to load requester for userId=$userId")
        }
    }

    // Load user if not loaded
    LaunchedEffect(userInfo) {
        if (userInfo == null) {
            Log.d("FavorScreen", "UserInfo is null, attempting to load user")
            userViewModel.loadUserClientInfo()
        }
    }

    val isInsideCampus = if (hasLocationPermission) {
        val location = locationHelper.currentLocation.value
        val result = locationHelper.isInsideCampus(location)
        Log.d("FavorScreen", "Inside campus: $result, Location: $location, HasPermission: $hasLocationPermission")
        result
    } else {
        Log.d("FavorScreen", "No location permission")
        false
    }

    val isAcceptEnabled = when (favor.category) {
        "Favor", "Compra" -> isInsideCampus
        "Tutoría" -> true
        else -> true
    }

    // Log button state
    LaunchedEffect(isAcceptEnabled, userInfo?.id, hasLocationPermission, isOnline) {
        Log.d("FavorScreen", "Button state - isAcceptEnabled: $isAcceptEnabled, userInfo.id: ${userInfo?.id}, favor.id: ${favor.id}, category: ${favor.category}, hasLocationPermission: $hasLocationPermission, isOnline: $isOnline")
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            RewardText(favor.reward)

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
                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                    modifier = Modifier
                        .height(35.dp)
                        .wrapContentWidth()
                        .defaultMinSize(minWidth = 0.dp)
                        .padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = favor.category,
                        fontSize = 14.sp,
                        color = BlackTextColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center
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

            if (!isOnline) {
                Text(
                    text = "No hay conexión a internet",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isAcceptEnabled && userInfo?.id != null) {
                        coroutineScope.launch {
                            try {
                                Log.d(
                                    "FavorScreen",
                                    "Attempting to accept favor: favorId=${favor.id}, userId=${userInfo!!.id}"
                                )
                                favorViewModel.acceptFavor(favor.id.toString(), userInfo!!.id)
                                Log.d("FavorScreen", "Favor accepted successfully")
                                showDialog = true
                            } catch (e: Exception) {
                                Log.e(
                                    "FavorScreen",
                                    "Error accepting favor: ${e.localizedMessage}",
                                    e
                                )
                                snackbarHostState.showSnackbar("No se pudo aceptar el favor: ${e.localizedMessage}")
                            }
                        }
                    } else {
                        val reason = when {
                            userInfo?.id == null -> "Usuario no autenticado"
                            !isAcceptEnabled && !hasLocationPermission -> "Permisos de ubicación no concedidos"
                            !isAcceptEnabled -> "No estás dentro del campus"
                            else -> "Condiciones no cumplidas"
                        }
                        Log.w("FavorScreen", "Cannot accept favor: $reason")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(reason)
                        }
                        showNoConnectionDialog = true
                    }
                },
                enabled = isAcceptEnabled && userInfo?.id != null && isOnline,
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

            // Dialog for accepting favor
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Favor Accepted!") },
                    text = {
                        Column {
                            Text("You have accepted the favor: ${favor.title}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Requested by: $userName")
                            Text("Phone: $userPhone")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                navController.navigate("history") { launchSingleTop = true }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MikadoYellow,
                                contentColor = BlackTextColor
                            )
                        ) {
                            Text("Ready")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = BlackTextColor
                            )
                        ) {
                            Text("Cancel")
                        }
                    },
                    containerColor = Color.White,
                    titleContentColor = BlackTextColor,
                    textContentColor = BlackTextColor
                )
            }

            // Dialog for no connection
            if (showNoConnectionDialog) {
                AlertDialog(
                    onDismissRequest = { /* opcional: showNoConnectionDialog = false */ },
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin conexión",
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Estás sin conexión",
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = {
                                showNoConnectionDialog = false
                                navController.navigate("home") {
                                    launchSingleTop = true
                                }
                            }) {
                                Text("Ir al inicio")
                            }
                        }
                    }
                )
            }
        }
    }
}

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