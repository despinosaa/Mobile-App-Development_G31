package com.example.senefavores.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    hasBorder: Boolean = false,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled, // Use the enabled parameter
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color.Gray // Optional: customize disabled color
        ),
        border = if (hasBorder) BorderStroke(2.dp, Color.Black) else null,
        modifier = Modifier
            .width(200.dp)
            .height(56.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = if (enabled) textColor else Color.White // Adjust text color when disabled
        )
    }
}