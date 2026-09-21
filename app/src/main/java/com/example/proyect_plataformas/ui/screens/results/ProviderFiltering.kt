package com.example.proyect_plataformas.ui.screens.results

import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter

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