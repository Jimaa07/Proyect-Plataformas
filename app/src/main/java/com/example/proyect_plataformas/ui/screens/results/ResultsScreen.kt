package com.example.proyect_plataformas.ui.screens.results

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.ui.components.ProviderCard
import com.example.proyect_plataformas.ui.components.ProviderFilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    providers: List<Provider>,
    onBackClick: () -> Unit,
    onProviderClick: (String) -> Unit
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf(ProviderFilter.TOP_RATED)
    }

    val displayedProviders = applyProviderFilter(
        providers = providers,
        filter = selectedFilter
    )

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
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Text(
                            text = "○",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },

        bottomBar = {
            ResultsBottomBar(
                onHomeClick = onBackClick
            )
        }
    ) { innerPadding ->

        ResultsContent(
            providers = displayedProviders,
            selectedFilter = selectedFilter,
            onFilterSelected = { filter ->
                selectedFilter = filter
            },
            onProviderClick = onProviderClick,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun ResultsContent(
    providers: List<Provider>,
    selectedFilter: ProviderFilter,
    onFilterSelected: (ProviderFilter) -> Unit,
    onProviderClick: (String) -> Unit,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 12.dp,
            end = 12.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Electricistas cerca de ti",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Encontramos los mejores profesionales para tu necesidad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            ProviderFilters(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }

        if (providers.isEmpty()) {
            item {
                Text(
                    text = "No encontramos profesionales para este filtro.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = providers,
                key = { provider ->
                    provider.id
                }
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
            onClick = {
                onFilterSelected(
                    ProviderFilter.TOP_RATED
                )
            }
        )

        ProviderFilterChip(
            text = "Disponibles",
            selected = selectedFilter == ProviderFilter.AVAILABLE,
            onClick = {
                onFilterSelected(
                    ProviderFilter.AVAILABLE
                )
            }
        )

        ProviderFilterChip(
            text = "Más recomendados",
            selected = selectedFilter == ProviderFilter.MOST_RECOMMENDED,
            onClick = {
                onFilterSelected(
                    ProviderFilter.MOST_RECOMMENDED
                )
            }
        )
    }
}

@Composable
private fun ResultsBottomBar(
    onHomeClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {

        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
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
            selected = true,
            onClick = {},
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