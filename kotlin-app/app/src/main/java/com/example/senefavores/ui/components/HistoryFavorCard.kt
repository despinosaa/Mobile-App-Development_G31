package com.example.senefavores.ui.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.User
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import com.example.senefavores.util.formatTime2
import kotlinx.coroutines.launch

fun truncateText(text: String, maxLength: Int = 32): String {
    return if (text.length > maxLength) {
        text.take(maxLength) + "..."
    } else {
        text
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryFavorCard(
    favor: Favor,
    isSolicitados: Boolean,
    hasReview: Boolean,
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    favorViewModel: FavorViewModel = hiltViewModel(),
    onStatusUpdate: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("Cargando...") }
    var userRating by remember { mutableStateOf(0.0f) }
    val userInfo by userViewModel.user.collectAsState()
    var localStatus by remember(favor.id) { mutableStateOf(favor.status) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch user name based on tab
    LaunchedEffect(favor.id, isSolicitados) {
        if (isSolicitados) {
            // Solicitados: Use accept_user_id
            userName = favor.accept_user_id?.let { acceptId ->
                userViewModel.getClientById(acceptId)?.name ?: "Usuario desconocido"
            } ?: "Ninguno"
            userRating = favor.accept_user_id?.let { acceptId ->
                userViewModel.getClientById(acceptId)?.stars ?: 0.0f
            } ?: 0.0f
        } else {
            // Aceptados: Use request_user_id
            userViewModel.getClientById(favor.request_user_id)?.let { user ->
                userName = user.name ?: "Usuario desconocido"
                userRating = user.stars ?: 0.0f
            }
        }
    }

    LaunchedEffect(favor.status) {
        localStatus = favor.status
    }

    // Dialog for Senetendero info
    if (showDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Información del Senetendero")
                    if (!selectedUser?.profilePic.isNullOrEmpty()) {
                        AsyncImage(
                            model = selectedUser?.profilePic,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_account),
                            placeholder = painterResource(R.drawable.ic_account)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_account),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                    Text("Nombre: ${truncateText(selectedUser?.name ?: "Desconocido")}")
                    Text("Correo: ${truncateText(selectedUser?.email ?: "N/A")}")
                    Text("Teléfono: ${truncateText(selectedUser?.phone ?: "N/A")}")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Calificación:")
                        RatingStars(rating = selectedUser?.stars ?: 0.0f)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MikadoYellow,
                            contentColor = BlackTextColor
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayTime = if (favor.created_at != null) {
                    try {
                        formatTime2(favor.created_at)
                    } catch (e: IllegalArgumentException) {
                        Log.e("HistoryFavorCard", "Invalid created_at format for favor ${favor.id}: ${favor.created_at}")
                        "Invalid time"
                    }
                } else {
                    Log.w("HistoryFavorCard", "created_at is null for favor ${favor.id}")
                    "Not specified"
                }
                Text(
                    text = truncateText(displayTime),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { /* No action */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (favor.category) {
                                "Favor" -> FavorCategoryColor
                                "Compra" -> CompraCategoryColor
                                "Tutoría" -> TutoriaCategoryColor
                                else -> Color.Gray
                            }
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .width(72.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        Text(
                            text = truncateText(favor.category),
                            fontSize = 14.sp,
                            color = BlackTextColor,
                            maxLines = 1
                        )
                    }
                    RewardText(favor.reward)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = truncateText(favor.title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = truncateText(favor.description),
                fontSize = 14.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_account),
                    contentDescription = "Usuario",
                    modifier = Modifier
                        .size(20.dp)
                )
                Text(
                    text = truncateText(if (isSolicitados) "Senetendero: $userName" else "Solicitante: $userName"),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                RatingStars(rating = userRating)
            }

            if (isSolicitados) {
                Spacer(modifier = Modifier.height(8.dp))
                when (localStatus) {
                    "pending" -> {
                        Button(
                            onClick = {
                                localStatus = "cancelled"
                                favorViewModel.updateFavorStatus(favor.id.toString(), "cancelled")
                                onStatusUpdate()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text(text = "Cancelar", fontSize = 14.sp)
                        }
                    }
                    "accepted" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    localStatus = "cancelled"
                                    favorViewModel.updateFavorStatus(favor.id.toString(), "cancelled")
                                    onStatusUpdate()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text(text = "Cancelar", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    localStatus = "done"
                                    favorViewModel.updateFavorStatus(favor.id.toString(), "done")
                                    onStatusUpdate()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MikadoYellow,
                                    contentColor = BlackTextColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text(text = "Finalizar", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    favor.accept_user_id?.let { acceptId ->
                                        coroutineScope.launch {
                                            userViewModel.getClientById(acceptId)?.let { user ->
                                                selectedUser = user
                                                showDialog = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                enabled = favor.accept_user_id != null
                            ) {
                                Text(text = "Senetendero", fontSize = 11.sp)
                            }
                        }
                    }
                    "done", "cancelled" -> {
                        if (!hasReview && (localStatus == "done" || (localStatus == "cancelled" && favor.accept_user_id != null))) {
                            Button(
                                onClick = {
                                    if (favor.accept_user_id?.isNotEmpty() == true) {
                                        navController.navigate("review/${favor.id}/${favor.request_user_id}/${favor.accept_user_id}")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MikadoYellow,
                                    contentColor = BlackTextColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Text(text = "Hacer Reseña", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}