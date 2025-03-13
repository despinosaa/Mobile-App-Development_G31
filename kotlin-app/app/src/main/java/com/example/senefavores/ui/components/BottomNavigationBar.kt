package com.example.senefavores.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.senefavores.R

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavigationItem(
            iconRes = R.drawable.ic_home,
            label = "Home",
            isSelected = selectedItem == 0,
            onClick = { onItemClick(0) }
        )
        NavigationItem(
            iconRes = R.drawable.ic_create_favor,
            label = "Crear Favor",
            isSelected = selectedItem == 1,
            onClick = { onItemClick(1) }
        )
        NavigationItem(
            iconRes = R.drawable.ic_history,
            label = "Historial",
            isSelected = selectedItem == 2,
            onClick = { onItemClick(2) }
        )
    }
}

@Composable
fun NavigationItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        IconButton(onClick = onClick) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = "$label Icon",
                modifier = Modifier.size(32.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    if (isSelected) Color.Black else Color.Gray // Cambiar el color según selección
                )
            )
        }
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}