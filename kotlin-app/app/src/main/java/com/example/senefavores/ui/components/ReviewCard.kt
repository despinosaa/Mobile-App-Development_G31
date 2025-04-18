package com.example.senefavores.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.senefavores.data.model.Review

@Composable
fun ReviewCard(review: Review) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Column {
            // Title
            Text(
                text = review.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Stars
            RatingStars(rating = review.stars.toFloat())
            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = review.description,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Reviewer ID (optional, can be replaced with reviewer name if available)
            Text(
                text = "Por: ${review.reviewer_id}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}