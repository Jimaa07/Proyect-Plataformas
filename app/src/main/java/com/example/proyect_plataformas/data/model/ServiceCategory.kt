package com.example.proyect_plataformas.data.model

/** Categorías de servicio que se muestran en el inicio y en Resultados. */
enum class ServiceCategory(
    val label: String,
    val symbol: String,
    val pluralTitle: String
) {
    PLUMBING(
        label = "Plomería",
        symbol = "🔧",
        pluralTitle = "Plomeros"
    ),
    ELECTRICAL(
        label = "Electricidad",
        symbol = "⚡",
        pluralTitle = "Electricistas"
    ),
    CARPENTRY(
        label = "Carpintería",
        symbol = "🪚",
        pluralTitle = "Carpinteros"
    ),
    GARDENING(
        label = "Jardinería",
        symbol = "🌿",
        pluralTitle = "Jardineros"
    ),
    APPLIANCES(
        label = "Electrodomésticos",
        symbol = "▣",
        pluralTitle = "Técnicos de electrodomésticos"
    )
}
