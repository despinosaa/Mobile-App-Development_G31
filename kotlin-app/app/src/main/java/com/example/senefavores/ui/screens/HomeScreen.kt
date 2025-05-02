package com.example.senefavores.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.User
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.FavorCard
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.BackgroundColor
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import com.example.senefavores.util.parseDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
fun timeToMinutes(favorTime: String): Int {
    return try {
        val dateTime = parseDateTime(favorTime)
        dateTime.hour * 60 + dateTime.minute
    } catch (e: IllegalArgumentException) {
        Log.e("HomeScreen", "Invalid date format in timeToMinutes: $favorTime", e)
        0
    }
}

fun smartSortFavors(favors: List<Favor>, history: List<String>): List<Favor> {
    val categoryFrequency = history.groupingBy { it }.eachCount()
    return favors.sortedWith(compareByDescending<Favor> { categoryFrequency[it.category] ?: 0 }
        .thenByDescending { history.indexOfLast { cat -> cat == it.category } })
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    telemetryLogger: TelemetryLogger,
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        onScreenChange("HomeScreen")
    }

    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("HomeScreen", responseTime)
        }
    }

    val userInfo by userViewModel.user.collectAsState()
    val hasCompletedInfo by userViewModel.hasCompletedInfo.collectAsState()
    val allFavorsOr by favorViewModel.favors.collectAsState()
    val allFavors = allFavorsOr.take(25)
    val isOnline by networkChecker.networkStatus.collectAsState(initial = false)

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var hasChecked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userInfo, hasCompletedInfo, hasChecked) {
        if (!hasChecked) {
            Log.d("HomeScreen", "Checking user session and info: showDialog=$showDialog, hasCompletedInfo=$hasCompletedInfo")
            var user = userViewModel.loadUserClientInfo()
            delay(2000)
            user = userViewModel.loadUserClientInfo()
            Log.d("UserInfo", "User loaded: $user, session exists: ${user != null}")

            if (user == null && isOnline) {
                Log.d("UserInfo", "No client found, inserting new client")
                userViewModel.insertUserInClients()
                showDialog = true
            } else if (isOnline && (user?.name.isNullOrEmpty() || user?.phone.isNullOrEmpty())) {
                Log.d("UserInfo", "Incomplete user info: name=${user?.name}, phone=${user?.phone}")
                showDialog = true
            }
            hasChecked = true
            Log.d("HomeScreen", "Session check complete: showDialog=$showDialog")
        }
        favorViewModel.fetchFavors(userInfo?.id)
    }

    ShowUserInfoDialog(
        showDialog = showDialog,
        userInfo = userInfo,
        userViewModel = userViewModel,
        onDismiss = { showDialog = false }
    )

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf(0) }
    var isSortDescending by remember { mutableStateOf(true) }
    var isSmartSortActive by remember { mutableStateOf(false) }

    val acceptedFavorsHistory = listOf("Favor", "Favor", "Compra", "Tutoría", "Favor")

    val filteredFavors = allFavors.let { favors ->
        if (selectedCategory == null) favors else favors.filter { it.category == selectedCategory }
    }.let { favors ->
        val validFavors = favors.filter { favor -> favor.created_at != null }
        try {
            if (isSortDescending) validFavors.sortedBy { timeToMinutes(it.created_at!!) }.reversed()
            else validFavors.sortedBy { timeToMinutes(it.created_at!!) }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error sorting favors: ${e.message}", e)
            validFavors
        }
    }

    val displayedFavors by remember(filteredFavors, isSmartSortActive) {
        mutableStateOf(if (isSmartSortActive) smartSortFavors(filteredFavors, acceptedFavorsHistory) else filteredFavors)
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
                .background(BackgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 0.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isSmartSortActive = !isSmartSortActive; if (isSmartSortActive) isSortDescending = false }) {
                    Image(painter = painterResource(R.drawable.ic_smart_sort), contentDescription = "Smart Sort", modifier = Modifier.size(24.dp), contentScale = ContentScale.Fit)
                }
                IconButton(onClick = { isSortDescending = !isSortDescending; isSmartSortActive = false }) {
                    Image(
                        painter = painterResource(if (isSortDescending) R.drawable.ic_sort_down else R.drawable.ic_sort_up),
                        contentDescription = "Ordenar",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryButton(
                        category = "Favor",
                        isSelected = selectedCategory == "Favor",
                        backgroundColor = FavorCategoryColor,
                        onClick = { selectedCategory = if (selectedCategory == "Favor") null else "Favor" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CategoryButton(
                        category = "Compra",
                        isSelected = selectedCategory == "Compra",
                        backgroundColor = CompraCategoryColor,
                        onClick = { selectedCategory = if (selectedCategory == "Compra") null else "Compra" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CategoryButton(
                        category = "Tutoría",
                        isSelected = selectedCategory == "Tutoría",
                        backgroundColor = TutoriaCategoryColor,
                        onClick = { selectedCategory = if (selectedCategory == "Tutoría") null else "Tutoría" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (!isOnline) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estás sin conexión",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(displayedFavors) { favor ->
                            FavorCard(favor = favor, userViewModel = userViewModel, onClick = {
                                val favorJson = Json.encodeToString(favor)
                                navController.navigate("favorScreen/$favorJson")
                            })
                        }
                    }
                }
            } else {
                favorViewModel.processQueue()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(displayedFavors) { favor ->
                        FavorCard(favor = favor, userViewModel = userViewModel, onClick = {
                            val favorJson = Json.encodeToString(favor)
                            navController.navigate("favorScreen/$favorJson")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    category: String,
    isSelected: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            fontSize = 16.sp,
            color = BlackTextColor,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShowUserInfoDialog(
    showDialog: Boolean,
    userInfo: User?,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(userInfo?.name ?: "") }
    var phone by remember { mutableStateOf(userInfo?.phone ?: "") }
    var isPhoneValid by remember { mutableStateOf(true) }

    LaunchedEffect(userInfo) {
        name = userInfo?.name ?: ""
        phone = userInfo?.phone ?: ""
    }

    if (showDialog) {
        Log.d("HomeScreen", "Showing user info dialog")
        AlertDialog(
            onDismissRequest = { /* Do nothing to prevent dismissal */ },
            title = { Text("Información Incompleta") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            isPhoneValid = it.isNotBlank() && it.matches(Regex("^[0-9]{10}$"))
                        },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = !isPhoneValid
                    )

                    if (!isPhoneValid) {
                        Text(
                            text = "El número de teléfono debe tener exactamente 10 dígitos",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("HomeScreen", "Dialog save clicked: name=$name, phone=$phone, isPhoneValid=$isPhoneValid")
                        if (name.isNotBlank() && phone.isNotBlank() && isPhoneValid) {
                            userViewModel.updateClientsUser(name = name, phone = phone)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank() && isPhoneValid
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = null
        )
    }
}