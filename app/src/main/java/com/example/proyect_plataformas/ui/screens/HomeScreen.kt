package com.example.proyect_plataformas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.ui.components.ScreenContainer
import com.example.proyect_plataformas.ui.components.ServiceCard

private data class ServiceCategory(
    val title: String,
    val description: String
)

private val mockCategories = listOf(
    ServiceCategory("Electricidad", "Instalaciones, reparaciones y mantenimiento."),
    ServiceCategory("Plomería", "Reparaciones de tuberías, grifos y drenajes."),
    ServiceCategory("Carpintería", "Muebles, puertas y reparaciones en madera.")
)

@Composable
fun HomeScreen(
    onCategoryClick: () -> Unit,
    onRateServiceClick: () -> Unit
) {
    ScreenContainer(
        title = "Inicio",
        onHomeClick = {},
        onRateServiceClick = onRateServiceClick
    ) { innerPadding ->
        HomeContent(
            innerPadding = innerPadding,
            onCategoryClick = onCategoryClick
        )
    }
}

@Composable
private fun HomeContent(
    innerPadding: PaddingValues,
    onCategoryClick: () -> Unit
) {
    LazyColumn(
        contentPadding = innerPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Encuentra ayuda cerca de ti",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona una categoría para ver profesionales disponibles.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(mockCategories) { category ->
            ServiceCard(
                title = category.title,
                description = category.description,
                onClick = onCategoryClick
            )
        }
    }
}