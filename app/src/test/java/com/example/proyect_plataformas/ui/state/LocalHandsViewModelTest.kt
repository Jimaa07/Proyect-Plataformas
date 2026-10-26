package com.example.proyect_plataformas.ui.state

import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Prueba el ViewModel como lo usan las pantallas: se le mandan eventos
 * y se revisa el estado que publica en uiState.
 */
class LocalHandsViewModelTest {

    private val today = LocalDate.of(2026, 10, 15)

    private fun newViewModel() = LocalHandsViewModel(
        initialState = initialLocalHandsState(today)
    )

    private val LocalHandsViewModel.state: LocalHandsUiState
        get() = uiState.value

    // ---------- Resultados ----------

    @Test
    fun openResults_fromCategory_showsOnlyThatCategory() {
        val viewModel = newViewModel()

        viewModel.openResults(ServiceCategory.PLUMBING)

        assertEquals(
            setOf("carlos-hernandez", "luis-ramirez"),
            viewModel.state.visibleProviders.map { provider -> provider.id }.toSet()
        )
    }

    @Test
    fun filtersAndSearch_updateTheVisibleList() {
        val viewModel = newViewModel()
        viewModel.openResults(ServiceCategory.PLUMBING)

        viewModel.selectFilter(ProviderFilter.AVAILABLE)
        assertEquals(
            listOf("carlos-hernandez"),
            viewModel.state.visibleProviders.map { provider -> provider.id }
        )

        viewModel.updateQuery("no existe nadie así")
        assertTrue(viewModel.state.visibleProviders.isEmpty())

        viewModel.clearResultsFilters()
        assertEquals(viewModel.state.providers.size, viewModel.state.visibleProviders.size)
    }

    @Test
    fun openResults_again_startsWithCleanFilters() {
        val viewModel = newViewModel()
        viewModel.openResults(ServiceCategory.PLUMBING)
        viewModel.updateQuery("luis")

        viewModel.openResults(ServiceCategory.GARDENING)

        assertEquals(ResultsState(category = ServiceCategory.GARDENING), viewModel.state.results)
    }

    // ---------- Solicitud ----------

    private fun LocalHandsViewModel.fillValidRequest(providerId: String) {
        updateDescription(providerId, "El tomacorriente de la cocina no funciona")
        selectZone(providerId, "Zona 10")
        selectDate(providerId, today.plusDays(2))
        selectTimeSlot(providerId, TimeSlot.MORNING)
    }

    @Test
    fun request_incomplete_isNotSent() {
        val viewModel = newViewModel()
        viewModel.startRequest("carlos-mendez")
        viewModel.updateDescription("carlos-mendez", "Se fue la luz")

        viewModel.submitRequest("carlos-mendez")

        assertEquals(1, viewModel.state.requests.size)
        assertFalse(viewModel.state.requestFormFor("carlos-mendez").submitted)
        assertEquals(
            "Agrega más detalle para el profesional (faltan 7 caracteres).",
            viewModel.state.requestErrorsFor("carlos-mendez").description
        )
    }

    @Test
    fun request_valid_isSentOnceAndAppearsInOrders() {
        val viewModel = newViewModel()
        viewModel.startRequest("carlos-mendez")
        viewModel.fillValidRequest("carlos-mendez")

        assertTrue(viewModel.state.requestErrorsFor("carlos-mendez").isValid)

        viewModel.submitRequest("carlos-mendez")
        viewModel.submitRequest("carlos-mendez") // doble toque: no duplica

        val created = viewModel.state.requests.first()
        assertEquals(2, viewModel.state.requests.size)
        assertEquals(RequestStatus.SENT, created.status)
        assertEquals("carlos-mendez", created.providerId)
        assertTrue(viewModel.state.requestFormFor("carlos-mendez").submitted)
    }

    @Test
    fun request_zoneOutsideCoverage_showsSpecificError() {
        val viewModel = newViewModel()
        viewModel.startRequest("carlos-mendez")
        viewModel.selectZone("carlos-mendez", "Mixco")

        assertEquals(
            "Carlos Méndez no trabaja en Mixco. Cubre: Zona 10, Zona 14, Zona 15, Zona 16.",
            viewModel.state.requestErrorsFor("carlos-mendez").zone
        )
    }

    @Test
    fun startRequest_forAnotherProvider_startsAnEmptyForm() {
        val viewModel = newViewModel()
        viewModel.startRequest("carlos-mendez")
        viewModel.fillValidRequest("carlos-mendez")

        viewModel.startRequest("ana-lopez")

        assertEquals(RequestFormState("ana-lopez"), viewModel.state.requestFormFor("ana-lopez"))
    }

    // ---------- Calificación ----------

    @Test
    fun rating_shortComment_isNotSaved() {
        val viewModel = newViewModel()
        viewModel.startRating("req-1")
        viewModel.selectStars("req-1", 5)
        viewModel.updateComment("req-1", "Bien")

        viewModel.submitRating("req-1")

        assertFalse(viewModel.state.ratingFormFor("req-1").isValid)
        assertEquals(RequestStatus.TO_RATE, viewModel.state.requestById("req-1")!!.status)
    }

    @Test
    fun rating_valid_updatesOrderAndProviderProfile() {
        val viewModel = newViewModel()
        viewModel.startRating("req-1")
        viewModel.selectStars("req-1", 5)
        viewModel.updateComment("req-1", "Excelente trabajo, muy puntual")

        viewModel.submitRating("req-1")
        viewModel.submitRating("req-1") // doble toque: no duplica la reseña

        val carlos = viewModel.state.providerById("carlos-mendez")!!

        assertEquals(RequestStatus.RATED, viewModel.state.requestById("req-1")!!.status)
        assertEquals(143, carlos.reviewCount)
        assertEquals("Excelente trabajo, muy puntual", carlos.reviews.first().comment)
        assertEquals("15 de octubre de 2026", carlos.reviews.first().date)
        assertTrue(viewModel.state.requestsToRate.isEmpty())
    }
}
