package com.uvg.localhands.ui.screens.results

import com.uvg.localhands.data.mock.MockLocalHandsData
import com.uvg.localhands.data.model.ProviderFilter
import com.uvg.localhands.data.model.ServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderFilteringTest {

    // Estos tests se escribieron para los 3 electricistas del mock; ahora el mock
    // tiene más categorías, así que se prueban sobre Electricidad.
    private val providers = filterByCategory(
        providers = MockLocalHandsData.providers,
        category = ServiceCategory.ELECTRICAL
    )

    @Test
    fun topRated_ordersProvidersByRatingDescending() {
        val result = applyProviderFilter(
            providers = providers,
            filter = ProviderFilter.TOP_RATED
        )

        assertEquals(
            "ana-lopez",
            result.first().id
        )

        assertEquals(
            listOf(
                "ana-lopez",
                "carlos-mendez",
                "roberto-garcia"
            ),
            result.map { provider ->
                provider.id
            }
        )
    }

    @Test
    fun available_returnsOnlyAvailableProviders() {
        val result = applyProviderFilter(
            providers = providers,
            filter = ProviderFilter.AVAILABLE
        )

        assertEquals(
            2,
            result.size
        )

        assertTrue(
            result.all { provider ->
                provider.isAvailable
            }
        )

        assertEquals(
            listOf(
                "ana-lopez",
                "carlos-mendez"
            ),
            result.map { provider ->
                provider.id
            }
        )
    }

    @Test
    fun mostRecommended_ordersProvidersByReviewCountDescending() {
        val result = applyProviderFilter(
            providers = providers,
            filter = ProviderFilter.MOST_RECOMMENDED
        )

        assertEquals(
            "carlos-mendez",
            result.first().id
        )

        assertEquals(
            listOf(
                "carlos-mendez",
                "roberto-garcia",
                "ana-lopez"
            ),
            result.map { provider ->
                provider.id
            }
        )
    }

    @Test
    fun filtering_doesNotModifyOriginalList() {
        val originalOrder = providers.map { provider ->
            provider.id
        }

        applyProviderFilter(
            providers = providers,
            filter = ProviderFilter.TOP_RATED
        )

        assertEquals(
            originalOrder,
            providers.map { provider ->
                provider.id
            }
        )
    }
}