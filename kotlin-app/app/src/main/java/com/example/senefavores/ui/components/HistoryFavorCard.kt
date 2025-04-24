package com.example.senefavores.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.theme.FavorCategoryColor
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.theme.MikadoYellow
import com.example.senefavores.ui.viewmodel.FavorViewModel
import com.example.senefavores.ui.viewmodel.UserViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime2(favorTime: String): String {
    val possibleFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    )

    for (formatter in possibleFormats) {
        try {
            val dateTime = LocalDateTime.parse(favorTime, formatter)
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: DateTimeParseException) {
            // Ignore and try the next format
        }
    }

    throw IllegalArgumentException("Invalid date format: $favorTime")
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
    onStatusUpdate: () -> Unit = {} // Callback for status updates
) {
    var userName by remember { mutableStateOf("Cargando...") }
    var userRating by remember { mutableStateOf(0.0f) }
    val userInfo by userViewModel.user.collectAsState()
    var localStatus by remember(favor.id) { mutableStateOf(favor.status) }

    LaunchedEffect(favor.request_user_id) {
        userViewModel.getClientById(favor.request_user_id.toString())?.let { user ->
            userName = user.name ?: "Usuario desconocido"
            userRating = user.stars ?: 0.0f
        }
    }

    LaunchedEffect(favor.status) {
        localStatus = favor.status
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
                Text(text = formatTime2(favor.created_at), fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))

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
                            .width(72.dp)
                            .padding(end = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = favor.category,
                            fontSize = 14.sp,
                            color = BlackTextColor,
                            maxLines = 1
                        )
                    }
                    Text(text = "$ ${favor.reward}", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = favor.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = favor.description, fontSize = 14.sp)

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
                        .padding(end = 4.dp)
                )
                Text(text = userName, fontSize = 14.sp)
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
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = "Cancelar", fontSize = 14.sp)
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
                                    .padding(end = 4.dp)
                            ) {
                                Text(text = "Finalizar", fontSize = 14.sp)
                            }
                            Button(
                                onClick = { /* Placeholder: No action */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text(text = "Senetendero", fontSize = 14.sp)
                            }
                        }
                    }
                    "done", "cancelled" -> {
                        if (localStatus == "done" || (localStatus == "cancelled" && favor.accept_user_id != null)) {
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
            } else {
                if (isSolicitados && favor.accept_user_id != null && !hasReview) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (favor.accept_user_id.isNotEmpty()) {
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