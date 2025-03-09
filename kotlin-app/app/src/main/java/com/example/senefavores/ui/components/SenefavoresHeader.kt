package com.example.senefavores.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.senefavores.ui.screens.loadBitmapFromAssets

@Composable
fun SenefavoresHeader() {
    val context = LocalContext.current
    val imageBitmap = loadBitmapFromAssets(context, "senefavores_logoHi.png")
    val imageBitmap2 = loadBitmapFromAssets(context, "profile_logo.png")

    // Una row donde se mete todo lo necesario
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Dentro de esta row que se alinea a la izq se pone el logo y texto
        Row(verticalAlignment = Alignment.CenterVertically) {
            imageBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(60.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = "Senefavores")
        }

        // el logo
        Box(
            modifier = Modifier
                .size(40.dp)
        ) {
            imageBitmap2?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Profile Icon",
                    modifier = Modifier
                        .size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}