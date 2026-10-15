package com.example.proyect_plataformas.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Barra inferior única para las pantallas principales (Inicio, Explorar,
 * Mis pedidos y Perfil). Cada pestaña lleva a una pantalla funcional;
 * la navegación la decide quien la usa por medio de onTabSelected.
 */
@Composable
fun AppBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = {
                    if (tab != selectedTab) {
                        onTabSelected(tab)
                    }
                },
                icon = {
                    Text(
                        text = tab.symbol,
                        fontSize = 20.sp
                    )
                },
                label = {
                    Text(tab.label)
                }
            )
        }
    }
}
