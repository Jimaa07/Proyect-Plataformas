package com.example.proyect_plataformas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.ui.components.AppBottomBar
import com.example.proyect_plataformas.ui.components.BottomTab
import com.example.proyect_plataformas.ui.components.InitialsAvatar
import com.example.proyect_plataformas.ui.theme.LocalHandsStar
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

@Composable
fun HomeScreen(
    userName: String,
    recommendedProviders: List<Provider>,
    pendingRatings: Int,
    onSearchClick: () -> Unit,
    onCategoryClick: (ServiceCategory) -> Unit,
    onProviderClick: (String) -> Unit,
    onPendingRatingsClick: () -> Unit,
    onTabSelected: (BottomTab) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                selectedTab = BottomTab.HOME,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->

        // Un solo espaciado (16 dp) entre bloques para que la jerarquía se lea
        // compacta y no queden huecos antes de la barra inferior.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Hola, ${userName.substringBefore(" ")}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "¿Qué servicio necesitas hoy?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                SearchServiceBar(
                    onClick = onSearchClick
                )
            }

            if (pendingRatings > 0) {
                item {
                    PendingRatingsCard(
                        pendingRatings = pendingRatings,
                        onClick = onPendingRatingsClick
                    )
                }
            }

            item {
                SectionTitle(
                    text = "Categorías"
                )
            }

            item {
                CategoryGrid(
                    onCategoryClick = onCategoryClick
                )
            }

            item {
                SectionTitle(
                    text = "Recomendados por tus vecinos"
                )
            }

            items(
                items = recommendedProviders,
                key = { provider -> provider.id }
            ) { provider ->
                RecommendedProviderCard(
                    provider = provider,
                    onClick = { onProviderClick(provider.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SearchServiceBar(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
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
                text = "Buscar por servicio, nombre o zona",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PendingRatingsCard(
    pendingRatings: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "★",
                fontSize = 22.sp,
                color = LocalHandsStar
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (pendingRatings == 1) {
                        "Tienes 1 trabajo por calificar"
                    } else {
                        "Tienes $pendingRatings trabajos por calificar"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "Tu reseña ayuda a otros vecinos a decidir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
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

/**
 * 5 categorías: dos filas de dos y la última a lo ancho en formato horizontal,
 * para no dejar una tarjeta suelta con espacio vacío a los lados.
 */
@Composable
private fun CategoryGrid(
    onCategoryClick: (ServiceCategory) -> Unit
) {
    val categories = ServiceCategory.entries
    val squareCategories = categories.dropLast(1)
    val wideCategory = categories.last()

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        squareCategories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCategories.forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        WideCategoryCard(
            category = wideCategory,
            onClick = { onCategoryClick(wideCategory) }
        )
    }
}

@Composable
private fun CategoryCard(
    category: ServiceCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(92.dp),
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
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            CategoryIcon(
                symbol = category.symbol
            )

            Text(
                text = category.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WideCategoryCard(
    category: ServiceCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryIcon(
                symbol = category.symbol
            )

            Text(
                text = category.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryIcon(
    symbol: String
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun RecommendedProviderCard(
    provider: Provider,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            InitialsAvatar(
                name = provider.name,
                size = 48.dp,
                textStyle = MaterialTheme.typography.labelLarge
            )

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
                    text = "${provider.category.symbol} ${provider.specialty} · ${provider.zone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "★ ${provider.rating}  (${provider.reviewCount} reseñas)",
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    ProyectplataformasTheme {
        HomeScreen(
            userName = MockLocalHandsData.currentUser.name,
            recommendedProviders = MockLocalHandsData.providers.take(3),
            pendingRatings = 1,
            onSearchClick = {},
            onCategoryClick = {},
            onProviderClick = {},
            onPendingRatingsClick = {},
            onTabSelected = {}
        )
    }
}
