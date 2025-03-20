package com.example.senefavores.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.senefavores.R
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.repository.UserRepository
import com.example.senefavores.ui.theme.FavorCategoryColor // Importar colores
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.theme.BlackTextColor
import com.example.senefavores.ui.viewmodel.UserViewModel

@Composable
fun FavorCard(favor: Favor, userViewModel: UserViewModel = hiltViewModel(), onClick: () -> Unit) {
    var userName by remember { mutableStateOf("Cargando...") }
    var userRating by remember { mutableStateOf(0.0f) }

    LaunchedEffect(favor.requested_user_id) {
        userViewModel.getClientById(favor.requested_user_id.toString())?.let { user ->
            userName = user.name ?: "Usuario desconocido"
            userRating = user.stars ?: 0.0f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior con hora y categoría/remuneración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = favor.favor_time, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))

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
                    Text(text = favor.reward, fontSize = 14.sp)
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
        }
    }
}
