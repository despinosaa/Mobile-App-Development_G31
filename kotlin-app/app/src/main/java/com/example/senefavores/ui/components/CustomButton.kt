package com.example.senefavores.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    hasBorder: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = if (hasBorder) BorderStroke(2.dp, Color.Black) else null,  // Add border if true
        modifier = Modifier
            .width(200.dp)
            .height(56.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = textColor
        )
    }
}
