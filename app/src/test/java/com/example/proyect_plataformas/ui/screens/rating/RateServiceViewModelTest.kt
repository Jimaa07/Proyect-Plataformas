package com.example.proyect_plataformas.ui.screens.rating

import androidx.lifecycle.SavedStateHandle
import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.FakeSource
import com.example.proyect_plataformas.data.SubmittedRating
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.repository.RatingConfirmationPendingException
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.DetailState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RateServiceViewModelTest {

    private val repository = FakeLocalHandsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.newViewModel(requestId: String = "req-1"): RateServiceViewModel {
        val viewModel = RateServiceViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf(Route.RateService.ARG_REQUEST_ID to requestId))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun shortComment_isNotSaved() = runTest {
        val viewModel = newViewModel()
        viewModel.selectStars(5)
        viewModel.updateComment("Bien")

        viewModel.submit()

        assertFalse(viewModel.uiState.value.form.isValid)
        assertTrue(repository.ratings.isEmpty())
    }

    @Test
    fun validRating_isSavedOnce() = runTest {
        val viewModel = newViewModel()
        viewModel.selectStars(5)
        viewModel.updateComment("  Excelente trabajo, muy puntual  ")

        viewModel.submit()
        viewModel.submit()

        assertEquals(
            listOf(SubmittedRating("req-1", 5, "Excelente trabajo, muy puntual", "Valeria Soto")),
            repository.ratings
        )
        assertTrue(viewModel.uiState.value.form.submitted)
    }

    @Test
    fun doubleTapWhileSaving_savesOnce() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate
        val viewModel = newViewModel()
        viewModel.selectStars(4)
        viewModel.updateComment("Buen trabajo y puntual")

        viewModel.submit()
        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.isSubmitting)
        assertEquals(1, repository.ratings.size)

        gate.complete(Unit)

        assertTrue(viewModel.uiState.value.form.submitted)
    }

    @Test
    fun failedSave_keepsTheForm_andShowsAnError() = runTest {
        repository.submitRatingError = IOException("Sin conexión")
        val viewModel = newViewModel()
        viewModel.selectStars(4)
        viewModel.updateComment("Buen trabajo y puntual")

        viewModel.submit()

        val form = viewModel.uiState.value.form
        assertEquals(RateServiceViewModel.SUBMIT_ERROR, form.submitError)
        assertFalse(form.submitted)
        assertEquals(4, form.stars)

        // Reintentar es seguro: repetir la misma calificación no duplica la reseña.
        repository.submitRatingError = null
        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.submitted)
        assertEquals(2, repository.ratings.size)
    }

    @Test
    fun confirmationTimeout_isAnError_notSaved() = runTest {
        repository.submitRatingError = RatingConfirmationPendingException("req-1", IllegalStateException("Tiempo agotado"))
        val viewModel = newViewModel()
        viewModel.selectStars(4)
        viewModel.updateComment("Buen trabajo y puntual")

        viewModel.submit()

        assertEquals(RateServiceViewModel.SUBMIT_ERROR, viewModel.uiState.value.form.submitError)
        assertFalse(viewModel.uiState.value.form.isSubmitting)
    }

    @Test
    fun requestsFailure_isAnError_notInfiniteLoading() = runTest {
        repository.requestsSource.value = FakeSource.Fails(IOException("Sin respuesta"))

        assertTrue(newViewModel().uiState.value.requestState is DetailState.Error)
    }

    @Test
    fun unknownRequest_isNotFound() = runTest {
        val viewModel = newViewModel(requestId = "no-existe")

        assertEquals(DetailState.NotFound, viewModel.uiState.value.requestState)
    }

    @Test
    fun savedRating_marksTheRequestAndUpdatesTheProfile() = runTest {
        val viewModel = newViewModel()
        viewModel.selectStars(5)
        viewModel.updateComment("Excelente trabajo, muy puntual")

        viewModel.submit()

        val request = repository.observeRequests().first().firstOrNull { it.id == "req-1" }
        val carlos = repository.observeProvider("carlos-mendez").first()

        assertEquals(RequestStatus.RATED, request?.status)
        assertEquals(143, carlos?.reviewCount)
        assertEquals("Excelente trabajo, muy puntual", carlos?.reviews?.first()?.comment)
    }
}
