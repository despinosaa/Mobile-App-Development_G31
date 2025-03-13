package com.example.senefavores.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.senefavores.ui.screens.Favor
import com.example.senefavores.R
import com.example.senefavores.ui.theme.FavorCategoryColor // Importar colores
import com.example.senefavores.ui.theme.CompraCategoryColor
import com.example.senefavores.ui.theme.TutoriaCategoryColor
import com.example.senefavores.ui.theme.BlackTextColor

@Composable
fun FavorCard(favor: Favor, onClick: () -> Unit) {
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
                // Hora en la esquina superior izquierda
                Text(
                    text = favor.time,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Categoría e ícono en la esquina superior derecha
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón de categoría
                    Button(
                        onClick = { /* No action por ahora */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (favor.category) {
                                "Favor" -> FavorCategoryColor
                                "Compra" -> CompraCategoryColor
                                "Tutoría" -> TutoriaCategoryColor
                                else -> Color.Gray // Por defecto
                            }
                        ),
                        modifier = Modifier
                            .height(32.dp) // Mantener la altura
                            .width(72.dp) // Aumentar el ancho para "Tutoría"
                            .padding(end = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp) // Reducir padding interno
                    ) {
                        Text(
                            text = favor.category,
                            fontSize = 14.sp, // Aumentar a 14sp para mayor visibilidad
                            color = BlackTextColor, // Usar color estandarizado
                            maxLines = 1 // Limitar a una línea para evitar truncamiento
                        )
                    }
                    // Remuneración
                    Text(
                        text = favor.reward,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre
            Text(
                text = favor.name,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Descripción
            Text(
                text = favor.description,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Usuario y calificación
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
                Text(
                    text = favor.user,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                RatingStars(rating = favor.rating.toFloat())
            }
        }
    }
}