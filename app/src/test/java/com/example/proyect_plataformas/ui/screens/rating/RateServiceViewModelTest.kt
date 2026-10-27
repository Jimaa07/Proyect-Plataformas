package com.example.proyect_plataformas.ui.screens.rating

import androidx.lifecycle.SavedStateHandle
import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.mock.MockLocalHandsRepository
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
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
import java.time.LocalDate

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

    private fun TestScope.newViewModel(
        requestId: String = "req-1",
        source: LocalHandsRepository = repository
    ): RateServiceViewModel {
        val viewModel = RateServiceViewModel(
            repository = source,
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

        assertEquals(listOf(Triple("req-1", 5, "Excelente trabajo, muy puntual")), repository.ratings)
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
        repository.rateRequestResult = Result.failure(IOException("Sin conexión"))
        val viewModel = newViewModel()
        viewModel.selectStars(4)
        viewModel.updateComment("Buen trabajo y puntual")

        viewModel.submit()

        val form = viewModel.uiState.value.form
        assertEquals(RateServiceViewModel.SUBMIT_ERROR, form.submitError)
        assertFalse(form.submitted)
        assertEquals(4, form.stars)
    }

    @Test
    fun unknownRequest_isNotFound() = runTest {
        val viewModel = newViewModel(requestId = "no-existe")

        assertEquals(DetailState.NotFound, viewModel.uiState.value.requestState)
    }

    @Test
    fun savedRating_marksTheRequestAndUpdatesTheProfile() = runTest {
        val mock = MockLocalHandsRepository(today = { LocalDate.of(2026, 10, 15) })
        val viewModel = newViewModel(source = mock)
        viewModel.selectStars(5)
        viewModel.updateComment("Excelente trabajo, muy puntual")

        viewModel.submit()

        val request = (mock.observeRequest("req-1").first() as DataState.Ready).data
        val carlos = (mock.observeProvider("carlos-mendez").first() as DataState.Ready).data

        assertEquals(RequestStatus.RATED, request?.status)
        assertEquals(143, carlos?.reviewCount)
        assertEquals("Excelente trabajo, muy puntual", carlos?.reviews?.first()?.comment)
    }
}
