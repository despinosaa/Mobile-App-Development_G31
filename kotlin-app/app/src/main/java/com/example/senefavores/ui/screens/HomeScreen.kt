package com.example.senefavores.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.senefavores.util.TelemetryLogger
import com.example.senefavores.util.parseDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    onScreenChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val startTime = remember { System.currentTimeMillis() } // Start time for response time measurement

    // Notify the parent of the current screen for crash reporting
    LaunchedEffect(Unit) {
        onScreenChange("HomeScreen")
    }

    // Log response time after the screen is composed
    LaunchedEffect(Unit) {
        val responseTime = System.currentTimeMillis() - startTime
        scope.launch {
            telemetryLogger.logResponseTime("HomeScreen", responseTime)
        }
    }

    val userInfo by userViewModel.user.collectAsState()
    val hasCompletedInfo by userViewModel.hasCompletedInfo.collectAsState()
    val allFavors by favorViewModel.favors.collectAsState()

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var hasChecked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userInfo, hasCompletedInfo, hasChecked) {
        if (!hasChecked) {
            Log.d("Dialog", "Checking user info: showDialog=$showDialog, hasCompletedInfo=$hasCompletedInfo")
            Log.d("UserInfo", "Loading user info...")

            // Delay to ensure session is set after sign-in
            delay(500)
            val user = userViewModel.loadUserClientInfo()
            Log.d("UserInfo", "User loaded: $user")

            if (user == null) {
                Log.d("UserInfo", "No client found, inserting new client")
                userViewModel.insertUserInClients()
                showDialog = true
            } else if (user.name.isNullOrEmpty() || user.phone.isNullOrEmpty()) {
                showDialog = true
            }
            hasChecked = true
            Log.d("Dialog", "Updated showDialog: $showDialog")
        }
        // Fetch favors with user ID (null if user not loaded)
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
    var isLoading by remember { mutableStateOf(false) }

    val acceptedFavorsHistory = listOf("Favor", "Favor", "Compra", "Tutoría", "Favor")

    val filteredFavors = allFavors.let { favors ->
        if (selectedCategory == null) {
            Log.d("Favors", "$favors")
            favors
        } else {
            favors.filter { it.category == selectedCategory }
        }
    }.let { favors ->
        if (isSortDescending) {
            favors.sortedBy { parseDateTime(it.created_at) }.reversed()
        } else {
            favors.sortedBy { parseDateTime(it.created_at) }
        }
    }

    val displayedFavors by remember(filteredFavors, isSmartSortActive) {
        mutableStateOf(
            if (isSmartSortActive) smartSortFavors(filteredFavors, acceptedFavorsHistory) else filteredFavors
        )
    }

    // LazyColumn state to detect scroll position
    val listState = rememberLazyListState()

    // Detect when the user reaches the end of the list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull()
                if (lastVisibleItem != null && lastVisibleItem.index == displayedFavors.size - 1 && !isLoading) {
                    isLoading = true
                    favorViewModel.loadMoreFavors(userInfo?.id)
                    isLoading = false
                }
            }
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
                IconButton(onClick = {
                    isSmartSortActive = !isSmartSortActive
                    if (isSmartSortActive) isSortDescending = false
                }) {
                    Image(
                        painter = painterResource(R.drawable.ic_smart_sort),
                        contentDescription = "Smart Sort",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                IconButton(onClick = {
                    isSortDescending = !isSortDescending
                    isSmartSortActive = false
                }) {
                    Image(
                        painter = painterResource(
                            if (isSortDescending) R.drawable.ic_sort_down else R.drawable.ic_sort_up
                        ),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                state = listState
            ) {
                items(displayedFavors) { favor ->
                    FavorCard(favor = favor, onClick = {
                        val favorJson = Json.encodeToString(favor)
                        navController.navigate("favorScreen/$favorJson")
                    })
                }
                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
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
        Log.e("Dialog", "Entered dialog function")
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
                        Log.d("Dialog", "Save clicked: name=$name, phone=$phone, isPhoneValid=$isPhoneValid")
                        if (name.isNotBlank() && phone.isNotBlank() && isPhoneValid) {
                            userViewModel.updateClientsUser(name = name, phone = phone)
                            Log.d("Dialog", "Calling onDismiss after save")
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