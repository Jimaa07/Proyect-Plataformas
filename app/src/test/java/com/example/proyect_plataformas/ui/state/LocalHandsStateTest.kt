package com.example.proyect_plataformas.ui.state

import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.ui.screens.orders.ordersForDisplay
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LocalHandsStateTest {

    private val initial = initialLocalHandsState()

    private val draft = RequestDraft(
        description = "La regadera del baño gotea toda la noche",
        zone = "Zona 11",
        date = LocalDate.of(2026, 10, 20),
        timeSlot = TimeSlot.MORNING
    )

    @Test
    fun initialState_hasOneJobToRate() {
        assertEquals(listOf("req-1"), initial.requestsToRate.map { request -> request.id })
    }

    @Test
    fun newRequest_isAddedFirstAsSent() {
        val state = initial.withNewRequest("carlos-hernandez", draft)
        val created = state.requests.first()

        assertEquals(2, state.requests.size)
        assertEquals("req-2", created.id)
        assertEquals(RequestStatus.SENT, created.status)
        assertEquals("Carlos Hernández", created.providerName)
        assertEquals(ServiceCategory.PLUMBING, created.category)
    }

    @Test
    fun newRequest_unknownProvider_changesNothing() {
        assertSame(initial, initial.withNewRequest("no-existe", draft))
    }

    @Test
    fun rating_marksRequestAndUpdatesProvider() {
        val state = initial.withRating(
            requestId = "req-1",
            stars = 5,
            comment = "Excelente trabajo, muy puntual",
            reviewDate = "15 de octubre de 2026"
        )

        val request = state.requestById("req-1")!!
        val carlos = state.providerById("carlos-mendez")!!

        assertEquals(RequestStatus.RATED, request.status)
        assertEquals(5, request.rating)
        assertEquals(143, carlos.reviewCount)
        assertEquals(4.9, carlos.rating, 0.0)
        assertEquals("Valeria Soto", carlos.reviews.first().reviewerName)
    }

    @Test
    fun rating_withInvalidForm_changesNothing() {
        assertSame(initial, initial.withRating("req-1", 5, "Bien", "15 de octubre de 2026"))
        assertSame(initial, initial.withRating("req-1", 0, "Excelente trabajo, muy puntual", "hoy"))
    }

    @Test
    fun rating_cannotBeRepeated() {
        val once = initial.withRating("req-1", 4, "Buen trabajo y puntual", "hoy")
        val twice = once.withRating("req-1", 1, "Cambié de opinión totalmente", "hoy")

        assertSame(once, twice)
    }

    @Test
    fun review_recalculatesAverage() {
        val provider = initial.providerById("silvia-paz")!!.copy(rating = 4.0, reviewCount = 1)
        val updated = provider.withReview(CommunityReview(reviewerName = "Ana", rating = 5, comment = "Muy bien", date = "hoy"))

        assertEquals(4.5, updated.rating, 0.0)
        assertEquals(2, updated.reviewCount)
    }

    @Test
    fun recommended_areBestRatedThenMostReviewed() {
        assertEquals(
            listOf("ana-lopez", "maria-garcia", "carlos-mendez"),
            initial.recommendedProviders(limit = 3).map { provider -> provider.id }
        )
    }

    @Test
    fun orders_showJobsToRateFirstAndRatedLast() {
        val withNew = initial.withNewRequest("carlos-hernandez", draft)
        val rated = withNew.withRating("req-1", 5, "Excelente trabajo, muy puntual", "hoy")
        val pending = rated.withNewRequest("rosa-cifuentes", draft)
            .let { state ->
                state.copy(
                    requests = state.requests.map { request ->
                        if (request.id == "req-3") request.copy(status = RequestStatus.TO_RATE) else request
                    }
                )
            }

        assertEquals(
            listOf(RequestStatus.TO_RATE, RequestStatus.SENT, RequestStatus.RATED),
            ordersForDisplay(pending.requests).map { request -> request.status }
        )
    }

    @Test
    fun nextRequestId_startsAtOne() {
        assertEquals("req-1", nextRequestId(emptyList()))
    }
}
