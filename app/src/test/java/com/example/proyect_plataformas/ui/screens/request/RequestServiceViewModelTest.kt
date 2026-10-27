package com.example.proyect_plataformas.ui.screens.request

import androidx.lifecycle.SavedStateHandle
import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.mock.MockLocalHandsRepository
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.TimeSlot
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
class RequestServiceViewModelTest {

    private val today = LocalDate.of(2026, 10, 15)
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
        providerId: String = "carlos-mendez",
        source: LocalHandsRepository = repository
    ): RequestServiceViewModel {
        val viewModel = RequestServiceViewModel(
            repository = source,
            savedStateHandle = SavedStateHandle(mapOf(Route.RequestService.ARG_PROVIDER_ID to providerId)),
            clock = { today }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    private fun RequestServiceViewModel.fillValidRequest() {
        updateDescription("El tomacorriente de la cocina no funciona")
        selectZone("Zona 10")
        selectDate(today.plusDays(2))
        selectTimeSlot(TimeSlot.MORNING)
    }

    @Test
    fun loadsTheProviderFromTheArgument() = runTest {
        val viewModel = newViewModel()

        val providerState = viewModel.uiState.value.providerState as DetailState.Ready
        assertEquals("carlos-mendez", providerState.data.id)
    }

    @Test
    fun unknownProvider_isNotFound_andCannotSubmit() = runTest {
        val viewModel = newViewModel(providerId = "no-existe")
        viewModel.fillValidRequest()

        viewModel.submit()

        assertEquals(DetailState.NotFound, viewModel.uiState.value.providerState)
        assertTrue(repository.createdRequests.isEmpty())
    }

    @Test
    fun incompleteForm_isNotSent() = runTest {
        val viewModel = newViewModel()
        viewModel.updateDescription("Se fue la luz")

        viewModel.submit()

        assertTrue(repository.createdRequests.isEmpty())
        assertFalse(viewModel.uiState.value.form.submitted)
        assertEquals(
            "Agrega más detalle para el profesional (faltan 7 caracteres).",
            viewModel.uiState.value.formErrors.description
        )
    }

    @Test
    fun zoneOutsideCoverage_showsSpecificError() = runTest {
        val viewModel = newViewModel()

        viewModel.selectZone("Mixco")

        assertEquals(
            "Carlos Méndez no trabaja en Mixco. Cubre: Zona 10, Zona 14, Zona 15, Zona 16.",
            viewModel.uiState.value.formErrors.zone
        )
    }

    @Test
    fun validForm_isSentOnceWithTrimmedText() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()
        viewModel.updateDescription("  El tomacorriente de la cocina no funciona  ")

        viewModel.submit()
        viewModel.submit()

        assertEquals(1, repository.createdRequests.size)
        assertEquals("carlos-mendez", repository.createdRequests.single().first)
        assertEquals("El tomacorriente de la cocina no funciona", repository.createdRequests.single().second.description)
        assertTrue(viewModel.uiState.value.form.submitted)
        assertFalse(viewModel.uiState.value.form.isSubmitting)
    }

    @Test
    fun doubleTapWhileSending_sendsOnce_andFormCannotChange() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.submit()
        viewModel.submit()
        viewModel.updateDescription("Otro texto distinto para la solicitud")

        assertTrue(viewModel.uiState.value.form.isSubmitting)
        assertEquals(1, repository.createdRequests.size)
        assertEquals("El tomacorriente de la cocina no funciona", viewModel.uiState.value.form.description)

        gate.complete(Unit)

        assertTrue(viewModel.uiState.value.form.submitted)
        assertFalse(viewModel.uiState.value.form.isSubmitting)
    }

    @Test
    fun failedSubmit_keepsTheForm_andShowsAnError() = runTest {
        repository.createRequestResult = Result.failure(IOException("Sin conexión"))
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.submit()

        val form = viewModel.uiState.value.form
        assertEquals(RequestServiceViewModel.SUBMIT_ERROR, form.submitError)
        assertFalse(form.submitted)
        assertFalse(form.isSubmitting)
        assertEquals("El tomacorriente de la cocina no funciona", form.description)

        repository.createRequestResult = Result.success("req-2")
        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.submitted)
        assertEquals(null, viewModel.uiState.value.form.submitError)
        assertEquals(2, repository.createdRequests.size)
    }

    @Test
    fun offlineWithoutCache_showsError_notInfiniteLoading() = runTest {
        repository.providersState.value = DataState.Ready(emptyList(), isFromCache = true)

        val viewModel = newViewModel()

        assertTrue(viewModel.uiState.value.providerState is DetailState.Error)
    }

    @Test
    fun sentRequest_appearsFirstInTheRequests() = runTest {
        val mock = MockLocalHandsRepository(today = { today })
        val viewModel = newViewModel(source = mock)
        viewModel.fillValidRequest()

        viewModel.submit()

        val requests = (mock.observeRequests().first() as DataState.Ready).data
        assertEquals("carlos-mendez", requests.first().providerId)
        assertEquals(RequestStatus.SENT, requests.first().status)
        assertEquals(2, requests.size)
    }
}
