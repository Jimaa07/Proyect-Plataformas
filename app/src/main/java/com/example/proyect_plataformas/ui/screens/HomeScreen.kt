package com.example.proyect_plataformas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_plataformas.ui.theme.LocalHandsStar

private data class HomeCategory(
    val name: String,
    val symbol: String
)

private data class RecommendedProvider(
    val name: String,
    val specialty: String,
    val rating: Double,
    val recommendations: Int,
    val initials: String
)

private val homeCategories = listOf(
    HomeCategory(
        name = "Plomería",
        symbol = "🔧"
    ),
    HomeCategory(
        name = "Electricidad",
        symbol = "⚡"
    ),
    HomeCategory(
        name = "Carpintería",
        symbol = "🪚"
    ),
    HomeCategory(
        name = "Jardinería",
        symbol = "🌿"
    ),
    HomeCategory(
        name = "Electrodomésticos",
        symbol = "▣"
    )
)

private val recommendedProviders = listOf(
    RecommendedProvider(
        name = "Carlos Hernández",
        specialty = "Plomería",
        rating = 4.9,
        recommendations = 124,
        initials = "CH"
    ),
    RecommendedProvider(
        name = "Juan Pérez",
        specialty = "Electricidad",
        rating = 4.8,
        recommendations = 89,
        initials = "JP"
    ),
    RecommendedProvider(
        name = "María García",
        specialty = "Carpintería",
        rating = 5.0,
        recommendations = 42,
        initials = "MG"
    )
)

@Composable
fun HomeScreen(
    onCategoryClick: () -> Unit,
    onRateServiceClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LocalHandsBottomBar(
                onExploreClick = onCategoryClick
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 22.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                Text(
                    text = "¿Qué servicio necesitas\nhoy?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                SearchServiceBar(
                    onClick = onCategoryClick
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    CategoryGrid(
                        categories = homeCategories,
                        onCategoryClick = onCategoryClick
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Prestadores recomendados en tu comunidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    recommendedProviders.forEach { provider ->
                        RecommendedProviderCard(
                            provider = provider
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                }
            }

            item {
                RateCompletedJobsButton(
                    onClick = onRateServiceClick
                )
            }
        }
    }
}

@Composable
private fun SearchServiceBar(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⌕",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Buscar un servicio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<HomeCategory>,
    onCategoryClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCard(
                category = categories[0],
                modifier = Modifier.weight(1f),
                onClick = onCategoryClick
            )

            CategoryCard(
                category = categories[1],
                modifier = Modifier.weight(1f),
                onClick = onCategoryClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCard(
                category = categories[2],
                modifier = Modifier.weight(1f),
                onClick = onCategoryClick
            )

            CategoryCard(
                category = categories[3],
                modifier = Modifier.weight(1f),
                onClick = onCategoryClick
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CategoryCard(
                category = categories[4],
                modifier = Modifier.fillMaxWidth(0.49f),
                onClick = onCategoryClick
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: HomeCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.symbol,
                    fontSize = 22.sp
                )
            }

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            Text(
                text = category.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecommendedProviderCard(
    provider: RecommendedProvider
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(9.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = provider.initials,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = provider.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "★ ${provider.rating}  (${provider.recommendations} recomendaciones)",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalHandsStar
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RateCompletedJobsButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 13.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "↻",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )

            Text(
                text = "  Calificar Trabajos Finalizados",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LocalHandsBottomBar(
    onExploreClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Text(
                    text = "⌂",
                    fontSize = 20.sp
                )
            },
            label = {
                Text("Inicio")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onExploreClick,
            icon = {
                Text(
                    text = "⌕",
                    fontSize = 20.sp
                )
            },
            label = {
                Text("Explorar")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Text(
                    text = "▤",
                    fontSize = 19.sp
                )
            },
            label = {
                Text("Mis pedidos")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Text(
                    text = "○",
                    fontSize = 20.sp
                )
            },
            label = {
                Text("Perfil")
            }
        )
    }
}