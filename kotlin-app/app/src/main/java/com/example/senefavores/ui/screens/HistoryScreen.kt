package com.example.senefavores.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.senefavores.ui.components.BottomNavigationBar
import com.example.senefavores.ui.components.SenefavoresHeader

enum class Tab {
    SOLICITADOS,
    ACEPTADOS
}

@Composable
fun TabRow(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Solicitados" tab
        if (selectedTab == Tab.SOLICITADOS) {
            Button(
                onClick = { onTabSelected(Tab.SOLICITADOS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Solicitados")
            }
        } else {
            TextButton(onClick = { onTabSelected(Tab.SOLICITADOS) }) {
                Text(text = "Solicitados")
            }
        }
        // "Aceptados" tab
        if (selectedTab == Tab.ACEPTADOS) {
            Button(
                onClick = { onTabSelected(Tab.ACEPTADOS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Aceptados")
            }
        } else {
            TextButton(onClick = { onTabSelected(Tab.ACEPTADOS) }) {
                Text(text = "Aceptados")
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: NavController) {
    // State variables: one for the TabRow and one for the bottom navigation
    var selectedTab by remember { mutableStateOf(Tab.SOLICITADOS) }
    var selectedItem by remember { mutableStateOf(2) } // 2 corresponds to History

    Scaffold(
        topBar = {
            // Assuming SenefavoresHeader accepts these parameters
            SenefavoresHeader(
                title = "SeneFavores",
                onAccountClick = { navController.navigate("account") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedItem,
                onItemClick = { index ->
                    selectedItem = index
                    when (index) {
                        0 -> navController.navigate("home") { launchSingleTop = true }
                        1 -> navController.navigate("createFavor") { launchSingleTop = true }
                        2 -> navController.navigate("history") { launchSingleTop = true }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tab row with two tabs
            TabRow(
                selectedTab = selectedTab,
                onTabSelected = { tab -> selectedTab = tab }
            )

            Text(text = "Pantalla vacía de History")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("home") }) {
                Text(text = "Go Back to Home")
            }
        }
    }
}
