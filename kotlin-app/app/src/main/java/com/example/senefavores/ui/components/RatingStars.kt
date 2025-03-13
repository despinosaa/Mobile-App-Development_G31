package com.example.senefavores.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.senefavores.R

@Composable
fun RatingStars(rating: Float) {
    Row {
        val fullStars = rating.toInt()
        val decimalPart = rating - fullStars
        val hasHalfStar = decimalPart in 0.3f..0.7f
        val roundedRating = if (decimalPart > 0.7f) fullStars + 1 else fullStars

        repeat(5) { index ->
            val starType = when {
                index < fullStars -> R.drawable.ic_star_filled
                index == fullStars && hasHalfStar -> R.drawable.ic_star_half
                index < roundedRating -> R.drawable.ic_star_filled
                else -> R.drawable.ic_star_empty
            }

            Image(
                painter = painterResource(starType),
                contentDescription = "Estrella",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}