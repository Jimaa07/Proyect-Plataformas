package com.example.proyect_plataformas.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppBottomBar(
    onHomeClick: () -> Unit,
    onRateServiceClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            icon = {},
            label = { Text("Inicio") }
        )

        NavigationBarItem(
            selected = false,
            onClick = onRateServiceClick,
            icon = {},
            label = { Text("Calificar") }
        )
    }
}