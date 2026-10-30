package com.example.proyect_plataformas.ui.screens.orders

import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.FakeSource
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.ready
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.OFFLINE_NOTICE
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

    private fun TestScope.newViewModel(): OrdersViewModel {
        val viewModel = OrdersViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun noRequests_isReadyAndEmpty_notAnError() = runTest {
        repository.requestsSource.value = ready(emptyList())

        val state = newViewModel().uiState.value

        assertEquals(ContentStatus.Ready(), state.status)
        assertTrue(state.requests.isEmpty())
    }

    @Test
    fun failure_showsError_andRetryRecovers() = runTest {
        repository.requestsSource.value = FakeSource.Fails(IllegalStateException("Permiso denegado"))
        val viewModel = newViewModel()

        assertTrue(viewModel.uiState.value.status is ContentStatus.Error)

        repository.requestsSource.value = ready(MockLocalHandsData.initialRequests)
        viewModel.retry()

        assertEquals(ContentStatus.Ready(), viewModel.uiState.value.status)
        assertEquals(1, viewModel.uiState.value.requests.size)
    }

    @Test
    fun withoutResponse_staysLoading() = runTest {
        repository.requestsSource.value = FakeSource.Waiting

        assertEquals(ContentStatus.Loading, newViewModel().uiState.value.status)
    }

    @Test
    fun offlineWithoutCache_isReadyAndEmpty_withNotice() = runTest {
        repository.requestsSource.value = ready(emptyList(), isFromCache = true)

        val state = newViewModel().uiState.value

        assertEquals(ContentStatus.Ready(notice = OFFLINE_NOTICE), state.status)
        assertTrue(state.requests.isEmpty())
    }

    @Test
    fun pendingWrites_showNotice_andKeepTheRequest() = runTest {
        val pending = MockLocalHandsData.initialRequests.first().copy(id = "req-9", isPendingSync = true)
        repository.requestsSource.value = ready(listOf(pending), hasPendingWrites = true)

        val state = newViewModel().uiState.value

        assertEquals(ContentStatus.Ready(notice = PENDING_SYNC_NOTICE), state.status)
        assertTrue(state.requests.single().isPendingSync)
    }

    @Test
    fun newRequest_appearsWithoutReopening_andJobsToRateGoFirst() = runTest {
        val viewModel = newViewModel()

        repository.createRequest(
            NewServiceRequest(
                providerId = "carlos-hernandez",
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
