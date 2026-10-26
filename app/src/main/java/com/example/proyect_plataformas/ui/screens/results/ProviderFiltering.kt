package com.example.proyect_plataformas.ui.screens.results

import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.ServiceCategory
import java.text.Normalizer

fun applyProviderFilter(
    providers: List<Provider>,
    filter: ProviderFilter
): List<Provider> {
    return when (filter) {
        ProviderFilter.TOP_RATED -> {
            providers.sortedByDescending { provider ->
                provider.rating
            }
        }

        ProviderFilter.AVAILABLE -> {
            providers
                .filter { provider ->
                    provider.isAvailable
                }
                .sortedByDescending { provider ->
                    provider.rating
                }
        }

        ProviderFilter.MOST_RECOMMENDED -> {
            providers.sortedByDescending { provider ->
                provider.reviewCount
            }
        }
    }
}

/** null significa "todas las categorías" (pestaña Explorar o buscador). */
fun filterByCategory(
    providers: List<Provider>,
    category: ServiceCategory?
): List<Provider> {
    if (category == null) {
        return providers
    }

    return providers.filter { provider ->
        provider.category == category
    }
}

/** Busca por nombre, especialidad, categoría o zona, sin importar tildes. */
fun searchProviders(
    providers: List<Provider>,
    query: String
): List<Provider> {
    val normalizedQuery = normalizeForSearch(query)

    if (normalizedQuery.isEmpty()) {
        return providers
    }

    return providers.filter { provider ->
        val searchableTexts =
            listOf(
                provider.name,
                provider.specialty,
                provider.category.label,
                provider.zone
            ) + provider.coverageZones

        searchableTexts.any { text ->
            normalizeForSearch(text).contains(normalizedQuery)
        }
    }
}

/**
 * Lista que muestra Resultados: primero la categoría, luego la búsqueda
 * y al final el orden o filtro de los chips.
 */
fun providersForResults(
    providers: List<Provider>,
    category: ServiceCategory?,
    query: String,
    filter: ProviderFilter
): List<Provider> {
    val inCategory = filterByCategory(providers, category)
    val matchingQuery = searchProviders(inCategory, query)

    return applyProviderFilter(matchingQuery, filter)
}

/** "Plomería " -> "plomeria". */
fun normalizeForSearch(text: String): String {
    return Normalizer
        .normalize(text.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
}
