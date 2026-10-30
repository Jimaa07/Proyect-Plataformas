package com.example.proyect_plataformas.ui.screens.results

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.ui.components.AppBottomBar
import com.example.proyect_plataformas.ui.components.BottomTab
import com.example.proyect_plataformas.ui.components.ErrorMessage
import com.example.proyect_plataformas.ui.components.LoadingIndicator
import com.example.proyect_plataformas.ui.components.NoticeBanner
import com.example.proyect_plataformas.ui.components.ProviderCard
import com.example.proyect_plataformas.ui.components.ProviderFilterChip
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.ResultsState
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

/**
 * Lista de profesionales. La categoría, la búsqueda y el filtro los guarda
 * el ViewModel (ResultsState); esta pantalla solo los muestra y avisa cambios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    state: ResultsState,
    providers: List<Provider>,
    onBackClick: () -> Unit,
    onProviderClick: (String) -> Unit,
    onCategorySelected: (ServiceCategory?) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFilterSelected: (ProviderFilter) -> Unit,
    onClearFilters: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    status: ContentStatus = ContentStatus.Ready(),
    onRetry: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LocalHands",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = BottomTab.EXPLORE,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        ResultsContent(
            providers = providers,
            status = status,
            selectedCategory = state.category,
            query = state.query,
            selectedFilter = state.filter,
            innerPadding = innerPadding,
            onCategorySelected = onCategorySelected,
            onQueryChanged = onQueryChanged,
            onFilterSelected = onFilterSelected,
            onClearFilters = onClearFilters,
            onProviderClick = onProviderClick,
            onRetry = onRetry
        )
    }
}

@Composable
private fun ResultsContent(
    providers: List<Provider>,
    status: ContentStatus,
    selectedCategory: ServiceCategory?,
    query: String,
    selectedFilter: ProviderFilter,
    innerPadding: PaddingValues,
    onCategorySelected: (ServiceCategory?) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFilterSelected: (ProviderFilter) -> Unit,
    onClearFilters: () -> Unit,
    onProviderClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 8.dp,
            end = 12.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {
            ResultsHeader(
                category = selectedCategory,
                resultCount = providers.size
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar por nombre, servicio o zona")
                },
                leadingIcon = {
                    Text("⌕")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        TextButton(
                            onClick = { onQueryChanged("") }
                        ) {
                            Text("Borrar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            CategoryFilters(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        item {
            ProviderFilters(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }

        when (status) {
            ContentStatus.Loading -> {
                item {
                    LoadingIndicator(
                        message = "Buscando profesionales..."
                    )
                }
            }

            is ContentStatus.Error -> {
                item {
                    ErrorMessage(
                        message = status.message,
                        onRetry = onRetry
                    )
                }
            }

            is ContentStatus.Ready -> {
                status.notice?.let { notice ->
                    item {
                        NoticeBanner(
                            text = notice
                        )
                    }
                }

                if (providers.isEmpty()) {
                    item {
                        EmptyResults(
                            category = selectedCategory,
                            onClearFilters = onClearFilters
                        )
                    }
                } else {
                    items(
                        items = providers,
                        key = { provider -> provider.id }
                    ) { provider ->
                        ProviderCard(
                            provider = provider,
                            onClick = {
                                onProviderClick(provider.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    category: ServiceCategory?,
    resultCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = if (category == null) {
                "Profesionales cerca de ti"
            } else {
                "${category.pluralTitle} cerca de ti"
            },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = when (resultCount) {
                0 -> "Sin resultados con estos filtros."
                1 -> "1 profesional encontrado."
                else -> "$resultCount profesionales encontrados."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryFilters(
    selectedCategory: ServiceCategory?,
    onCategorySelected: (ServiceCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ProviderFilterChip(
            text = "Todas",
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) }
        )

        ServiceCategory.entries.forEach { category ->
            ProviderFilterChip(
                text = "${category.symbol} ${category.label}",
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun ProviderFilters(
    selectedFilter: ProviderFilter,
    onFilterSelected: (ProviderFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ProviderFilterChip(
            text = "Mejor calificados",
            selected = selectedFilter == ProviderFilter.TOP_RATED,
            onClick = { onFilterSelected(ProviderFilter.TOP_RATED) }
        )

        ProviderFilterChip(
            text = "Disponibles",
            selected = selectedFilter == ProviderFilter.AVAILABLE,
            onClick = { onFilterSelected(ProviderFilter.AVAILABLE) }
        )

        ProviderFilterChip(
            text = "Más recomendados",
            selected = selectedFilter == ProviderFilter.MOST_RECOMMENDED,
            onClick = { onFilterSelected(ProviderFilter.MOST_RECOMMENDED) }
        )
    }
}

@Composable
private fun EmptyResults(
    category: ServiceCategory?,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (category == null) {
                "No encontramos profesionales con esa búsqueda."
            } else {
                "No encontramos ${category.pluralTitle.lowercase()} con esos filtros."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onClearFilters
        ) {
            Text("Quitar filtros")
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ResultsScreenPreview() {
    val state = ResultsState(category = ServiceCategory.PLUMBING)

    ProyectplataformasTheme {
        ResultsScreen(
            state = state,
            providers = providersForResults(
                providers = MockLocalHandsData.providers,
                category = state.category,
                query = state.query,
                filter = state.filter
            ),
            onBackClick = {},
            onProviderClick = {},
            onCategorySelected = {},
            onQueryChanged = {},
            onFilterSelected = {},
            onClearFilters = {},
            onTabSelected = {}
        )
    }
}
