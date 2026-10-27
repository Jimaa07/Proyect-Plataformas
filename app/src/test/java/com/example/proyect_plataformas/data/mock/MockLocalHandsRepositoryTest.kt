package com.example.proyect_plataformas.data.mock

import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.repository.DataState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MockLocalHandsRepositoryTest {

    private val today = LocalDate.of(2026, 10, 15)

    private val draft = RequestDraft(
        description = "La regadera del baño gotea toda la noche",
        zone = "Zona 11",
        date = LocalDate.of(2026, 10, 20),
        timeSlot = TimeSlot.MORNING
    )

    private fun newRepository() = MockLocalHandsRepository(today = { today })

    private fun <T> DataState<T>.dataOrNull(): T? = (this as? DataState.Ready)?.data

    @Test
    fun createRequest_addsItFirstAsSent() = runBlocking {
        val repository = newRepository()

        val id = repository.createRequest("carlos-hernandez", draft).getOrThrow()
        val requests = repository.observeRequests().first().dataOrNull().orEmpty()

        assertEquals("req-2", id)
        assertEquals(2, requests.size)
        assertEquals(RequestStatus.SENT, requests.first().status)
        assertEquals("Carlos Hernández", requests.first().providerName)
        assertEquals(ServiceCategory.PLUMBING, requests.first().category)
    }

    @Test
    fun createRequest_unknownProvider_fails() = runBlocking {
        val repository = newRepository()

        assertTrue(repository.createRequest("no-existe", draft).isFailure)
        assertEquals(1, repository.observeRequests().first().dataOrNull().orEmpty().size)
    }

    @Test
    fun rateRequest_marksRequestAndUpdatesProvider() = runBlocking {
        val repository = newRepository()

        repository.rateRequest("req-1", 5, "Excelente trabajo, muy puntual").getOrThrow()

        val request = repository.observeRequest("req-1").first().dataOrNull()
        val carlos = repository.observeProvider("carlos-mendez").first().dataOrNull()

        assertEquals(RequestStatus.RATED, request?.status)
        assertEquals(5, request?.rating)
        assertEquals(143, carlos?.reviewCount)
        assertEquals(4.9, carlos?.rating ?: 0.0, 0.0)
        assertEquals("Valeria Soto", carlos?.reviews?.first()?.reviewerName)
        assertEquals("15 de octubre de 2026", carlos?.reviews?.first()?.date)
    }

    @Test
    fun rateRequest_cannotBeRepeated() = runBlocking {
        val repository = newRepository()

        repository.rateRequest("req-1", 4, "Buen trabajo y puntual").getOrThrow()
        val second = repository.rateRequest("req-1", 1, "Cambié de opinión totalmente")

        assertTrue(second.isFailure)
        assertEquals(4, repository.observeRequest("req-1").first().dataOrNull()?.rating)
    }

    @Test
    fun observeProvider_unknownId_isNull() = runBlocking {
        assertNull(newRepository().observeProvider("no-existe").first().dataOrNull())
    }

    @Test
    fun reviews_haveUniqueIds() {
        val ids = MockLocalHandsData.providers.flatMap { provider -> provider.reviews.map { review -> review.id } }

        assertTrue(ids.none { id -> id.isEmpty() })
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun review_recalculatesAverage() {
        val provider = MockLocalHandsData.providers.first().copy(rating = 4.0, reviewCount = 1)
        val updated = provider.withReview(
            CommunityReview(reviewerName = "Ana", rating = 5, comment = "Muy bien", date = "hoy")
        )

        assertEquals(4.5, updated.rating, 0.0)
        assertEquals(2, updated.reviewCount)
    }

    @Test
    fun nextRequestId_startsAtOne() {
        assertEquals("req-1", nextRequestId(emptyList()))
    }
}
