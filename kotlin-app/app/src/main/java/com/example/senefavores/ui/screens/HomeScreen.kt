package com.example.senefavores.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.FavorCard
import com.example.senefavores.ui.components.SenefavoresHeader
import com.example.senefavores.ui.theme.FavorCategoryColor // Importar colores
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.theme.BackgroundColor
import com.example.senefavores.ui.theme.BlackTextColor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.data.model.User
import com.example.senefavores.ui.viewmodel.FavorViewModel

// Modelo de datos para un Favor (serializable para pasar por navegación)


// Función para convertir el formato de hora (HH:mm) a minutos desde la medianoche
fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    val hours = parts[0].toInt()
    val minutes = parts[1].toInt()
    return hours * 60 + minutes
}

// Función para ordenar por smart sort según el historial
fun smartSortFavors(favors: List<Favor>, history: List<String>): List<Favor> {
    // Contar la frecuencia de cada categoría en el historial
    val categoryFrequency = history.groupingBy { it }.eachCount()
    // Ordenar por frecuencia (descendente) y, en caso de empate, por la última aparición en el historial
    return favors.sortedWith(compareByDescending<Favor> { categoryFrequency[it.category] ?: 0 }
        .thenByDescending { history.indexOfLast { cat -> cat == it.category } })
}

@Composable
fun HomeScreen(navController: NavController, userViewModel: UserViewModel = hiltViewModel(), favorViewModel: FavorViewModel = hiltViewModel()) {
    val userInfo by userViewModel.user.collectAsState()
    val allFavors  by favorViewModel.favors.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var hasUpdated by remember { mutableStateOf(false) }

    LaunchedEffect(userViewModel.hasCompletedInfo) {
        Log.d("Dialog", "The dialog: $showDialog")
        Log.d("UserInfo", "Loading user info...")
        val user = userViewModel.loadUserInfo()
        Log.d("UserInfo", "User loaded: $user")
        Log.d("UserInfo", "hasCompletedInfo: ${userViewModel.hasCompletedInfo.value}")

        if (user != null) {
            if (user.name?.isNotEmpty() == true && user.phone?.isNotEmpty() == true) {
                showDialog = true
                hasUpdated = true
            }
        }
        Log.d("Dialog", "The dialog: $showDialog")
        favorViewModel.fetchFavors()
    }

    ShowUserInfoDialog(
        showDialog = showDialog,
        userInfo = userInfo,
        userViewModel = userViewModel
    ) { showDialog = true }


    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedItem by remember { mutableStateOf(0) } // Estado para el ítem seleccionado
    var isSortDescending by remember { mutableStateOf(true) } // Estado para el modo de ordenamiento
    var isSmartSortActive by remember { mutableStateOf(false) } // Estado para activar/desactivar smart sort

    // Historial fijo de categorías aceptadas (provisional para probar smart sort)
    val acceptedFavorsHistory = listOf("Favor", "Favor", "Compra", "Tutoría", "Favor")



    // Filtrar y ordenar los favores según el modo seleccionado
    val filteredFavors = if (selectedCategory == null) {
        Log.d("Favors", "$allFavors")
        allFavors
    } else {
        allFavors.filter { it.category == selectedCategory }
    }.let { favors ->
        if (isSortDescending) {
            favors.sortedBy { timeToMinutes(it.favor_time) }.reversed()
        } else {
            favors.sortedBy { timeToMinutes(it.favor_time) }
        }
    }

    // Aplicar smart sort si se selecciona
    val displayedFavors by remember(filteredFavors, isSmartSortActive) {
        mutableStateOf(
            if (isSmartSortActive) smartSortFavors(filteredFavors, acceptedFavorsHistory) else filteredFavors
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
                .background(BackgroundColor) // Usar color estandarizado del tema
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 0.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de smart sort a la izquierda
                IconButton(onClick = {
                    isSmartSortActive = !isSmartSortActive // Alternar smart sort
                    if (isSmartSortActive) isSortDescending = false // Desactivar sort normal al activar smart sort
                }) {
                    Image(
                        painter = painterResource(R.drawable.ic_smart_sort),
                        contentDescription = "Smart Sort",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                // Ícono de ordenamiento a la derecha del smart sort
                IconButton(onClick = {
                    isSortDescending = !isSortDescending // Alternar el modo de ordenamiento
                    isSmartSortActive = false // Desactivar smart sort al usar sort normal
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
                // Botones de categoría distribuidos equitativamente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryButton(
                        category = "Favor",
                        isSelected = selectedCategory == "Favor",
                        backgroundColor = FavorCategoryColor, // Usar color estandarizado
                        onClick = { selectedCategory = if (selectedCategory == "Favor") null else "Favor" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CategoryButton(
                        category = "Compra",
                        isSelected = selectedCategory == "Compra",
                        backgroundColor = CompraCategoryColor, // Usar color estandarizado
                        onClick = { selectedCategory = if (selectedCategory == "Compra") null else "Compra" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CategoryButton(
                        category = "Tutoría",
                        isSelected = selectedCategory == "Tutoría",
                        backgroundColor = TutoriaCategoryColor, // Usar color estandarizado
                        onClick = { selectedCategory = if (selectedCategory == "Tutoría") null else "Tutoría" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(displayedFavors) { favor ->
                    FavorCard(favor = favor, onClick = {
                        // Navegar a FavorScreen pasando el favor como argumento
                        val favorJson = Json.encodeToString(favor)
                        navController.navigate("favorScreen/$favorJson")
                    })
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
            color = BlackTextColor, // Usar color estandarizado
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun ShowUserInfoDialog(
    showDialog: Boolean,
    userInfo: User?, // Pass userInfo directly
    userViewModel: UserViewModel, // Pass the ViewModel
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(userInfo?.name ?: "") }
    var phone by remember { mutableStateOf(userInfo?.phone ?: "") }
    var isPhoneValid by remember { mutableStateOf(true) }

    // Update fields when userInfo changes
    LaunchedEffect(userInfo) {
        name = userInfo?.name ?: ""
        phone = userInfo?.phone ?: ""
    }

    if (!showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
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
                            isPhoneValid = it.matches(Regex("^\\+?[0-9]{7,15}$")) // Basic phone validation
                        },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        isError = !isPhoneValid
                    )

                    if (!isPhoneValid) {
                        Text("Número de teléfono no válido", color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && isPhoneValid) {
                            userInfo?.id?.let { clientId ->
                                userViewModel.updateUserInfo(
                                    clientId = clientId,
                                    phone = phone.takeIf { it.isNotBlank() }, // Only pass if not blank
                                    name = name.takeIf { it.isNotBlank() }, // Only pass if not blank
                                    profilePic = null, // Not updating profilePic
                                    stars = null // Not updating stars
                                )
                                onDismiss()
                            }
                        }
                    },
                    enabled = name.isNotBlank() && isPhoneValid
                ) {
                    Text("Guardar")
                }
            }
        )
    }
}