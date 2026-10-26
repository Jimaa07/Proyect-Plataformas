package com.example.proyect_plataformas.ui.components

/** Pestañas de la barra inferior. Todas llevan a una pantalla real. */
enum class BottomTab(
    val label: String,
    val symbol: String
) {
    HOME(label = "Inicio", symbol = "⌂"),
    EXPLORE(label = "Explorar", symbol = "⌕"),
    ORDERS(label = "Mis pedidos", symbol = "▤"),
    PROFILE(label = "Perfil", symbol = "○")
}
