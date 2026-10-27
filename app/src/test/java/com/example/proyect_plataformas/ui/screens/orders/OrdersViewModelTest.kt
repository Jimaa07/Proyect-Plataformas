package com.example.proyect_plataformas.ui.screens.orders

import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.mock.MockLocalHandsRepository
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.PENDING_SYNC_NOTICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersViewModelTest {

    private val repository = FakeLocalHandsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.newViewModel(source: LocalHandsRepository = repository): OrdersViewModel {
        val viewModel = OrdersViewModel(source)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun noRequests_isReadyAndEmpty_notAnError() = runTest {
        repository.requestsState.value = DataState.Ready(emptyList())

        val state = newViewModel().uiState.value

        assertEquals(ContentStatus.Ready(), state.status)
        assertTrue(state.requests.isEmpty())
    }

    @Test
    fun failure_showsError() = runTest {
        repository.requestsState.value = DataState.Failure(IllegalStateException("Permiso denegado"))

        assertTrue(newViewModel().uiState.value.status is ContentStatus.Error)
    }

    @Test
    fun pendingWrites_showNotice_andKeepTheRequest() = runTest {
        val pending = MockLocalHandsData.initialRequests.first().copy(id = "req-9", isPendingSync = true)
        repository.requestsState.value = DataState.Ready(listOf(pending), hasPendingWrites = true)

        val state = newViewModel().uiState.value

        assertEquals(ContentStatus.Ready(notice = PENDING_SYNC_NOTICE), state.status)
        assertTrue(state.requests.single().isPendingSync)
    }

    @Test
    fun newRequest_appearsWithoutReopening_andJobsToRateGoFirst() = runTest {
        val mock = MockLocalHandsRepository(today = { LocalDate.of(2026, 10, 15) })
        val viewModel = newViewModel(source = mock)

        mock.createRequest(
            providerId = "carlos-hernandez",
            draft = RequestDraft(
                description = "La regadera del baño gotea toda la noche",
                zone = "Zona 11",
                date = LocalDate.of(2026, 10, 20),
                timeSlot = TimeSlot.MORNING
            )
        )

        val requests = viewModel.uiState.value.requests
        assertEquals(2, requests.size)
        assertEquals(listOf(RequestStatus.TO_RATE, RequestStatus.SENT), requests.map { request -> request.status })
    }
}
