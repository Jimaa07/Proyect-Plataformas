package com.example.proyect_plataformas.ui.screens.results

import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.ServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryFilteringTest {

    private val providers = MockLocalHandsData.providers

    @Test
    fun everyCategory_hasAtLeastTwoProviders() {
        ServiceCategory.entries.forEach { category ->
            val count = filterByCategory(providers, category).size
            assertTrue("$category tiene $count prestadores", count >= 2)
        }
    }

    @Test
    fun plumbing_returnsOnlyPlumbers() {
        val result = filterByCategory(providers, ServiceCategory.PLUMBING)

        assertEquals(
            setOf("carlos-hernandez", "luis-ramirez"),
            result.map { provider -> provider.id }.toSet()
        )
    }

    @Test
    fun differentCategories_showDifferentLists() {
        val plumbers = filterByCategory(providers, ServiceCategory.PLUMBING)
        val gardeners = filterByCategory(providers, ServiceCategory.GARDENING)

        assertTrue(plumbers.intersect(gardeners.toSet()).isEmpty())
    }

    @Test
    fun noCategory_returnsEveryone() {
        assertEquals(providers.size, filterByCategory(providers, null).size)
    }

    @Test
    fun search_ignoresAccentsAndCase() {
        val result = searchProviders(providers, "PLOMERIA")

        assertEquals(
            setOf("carlos-hernandez", "luis-ramirez"),
            result.map { provider -> provider.id }.toSet()
        )
    }

    @Test
    fun search_findsByCoverageZone() {
        val result = searchProviders(providers, "mixco")

        assertTrue(result.any { provider -> provider.id == "roberto-garcia" })
        assertTrue(result.any { provider -> provider.id == "pedro-ajcuc" })
        assertTrue(
            result.all { provider ->
                provider.zone == "Mixco" || "Mixco" in provider.coverageZones
            }
        )
    }

    @Test
    fun search_blankQuery_returnsEveryone() {
        assertEquals(providers.size, searchProviders(providers, "   ").size)
    }

    @Test
    fun results_combineCategorySearchAndFilter() {
        val result = providersForResults(
            providers = providers,
            category = ServiceCategory.PLUMBING,
            query = "",
            filter = ProviderFilter.AVAILABLE
        )

        // Luis Ramírez es plomero pero no está disponible.
        assertEquals(listOf("carlos-hernandez"), result.map { provider -> provider.id })
    }
}
