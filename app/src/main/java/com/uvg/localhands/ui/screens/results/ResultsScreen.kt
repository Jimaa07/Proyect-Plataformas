package com.uvg.localhands.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uvg.localhands.data.mock.MockLocalHandsData
import com.uvg.localhands.data.model.Provider
import com.uvg.localhands.data.model.ProviderFilter
import com.uvg.localhands.data.model.ServiceCategory
import com.uvg.localhands.ui.components.AppBottomBar
import com.uvg.localhands.ui.components.BottomTab
import com.uvg.localhands.ui.components.ErrorMessage
import com.uvg.localhands.ui.components.LoadingIndicator
import com.uvg.localhands.ui.components.NoticeBanner
import com.uvg.localhands.ui.components.ProviderCard
import com.uvg.localhands.ui.components.ProviderFilterChip
import com.uvg.localhands.ui.state.ContentStatus
import com.uvg.localhands.ui.state.ResultsState
import com.uvg.localhands.ui.theme.ProyectplataformasTheme
import kotlinx.coroutines.launch

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

/**
 * Fila de categorías. En el task test la persona no notó que había más
 * categorías a la derecha, así que mientras quedan opciones ocultas el borde
 * se desvanece y aparece una flecha que desplaza la fila hasta el final.
 */
@Composable
private fun CategoryFilters(
    selectedCategory: ServiceCategory?,
    onCategorySelected: (ServiceCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember { listOf<ServiceCategory?>(null) + ServiceCategory.entries }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = options.indexOf(selectedCategory)

    // Si la categoría elegida quedó fuera de la pantalla (por ejemplo, al
    // llegar desde Inicio con la última categoría), la fila se desplaza a ella.
    LaunchedEffect(selectedIndex) {
        val layout = listState.layoutInfo
        val chip = layout.visibleItemsInfo.firstOrNull { item -> item.index == selectedIndex }
        val isFullyVisible = chip != null &&
                chip.offset >= 0 &&
                chip.offset + chip.size <= layout.viewportEndOffset

        if (!isFullyVisible) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            // Espacio final para que la flecha no tape la última categoría.
            contentPadding = PaddingValues(end = 44.dp)
        ) {
            items(
                items = options,
                key = { option -> option?.name ?: ALL_CATEGORIES_KEY }
            ) { option ->
                ProviderFilterChip(
                    text = option?.let { category -> "${category.symbol} ${category.label}" } ?: "Todas",
                    selected = option == selectedCategory,
                    onClick = { onCategorySelected(option) }
                )
            }
        }

        if (listState.canScrollForward) {
            MoreCategoriesHint(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(options.lastIndex)
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/** Degradado y flecha que avisan que la fila sigue a la derecha. */
@Composable
private fun MoreCategoriesHint(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = MaterialTheme.colorScheme.background

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .width(28.dp)
                .height(36.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, background)
                    )
                )
        )

        Box(
            modifier = Modifier
                .background(background)
                .padding(start = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier
                    .size(32.dp)
                    .semantics { contentDescription = "Ver más categorías" },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private const val ALL_CATEGORIES_KEY = "ALL"

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
