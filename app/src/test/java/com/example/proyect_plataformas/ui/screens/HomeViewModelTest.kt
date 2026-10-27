package com.example.proyect_plataformas.ui.screens

import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.ui.state.ContentStatus
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = FakeLocalHandsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.newViewModel(): HomeViewModel {
        val viewModel = HomeViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun recommended_areBestRatedThenMostReviewed() = runTest {
        val state = newViewModel().uiState.value

        assertEquals(
            listOf("ana-lopez", "maria-garcia", "carlos-mendez"),
            state.recommendedProviders.map { provider -> provider.id }
        )
        assertEquals("Valeria Soto", state.userName)
        assertEquals(1, state.pendingRatings)
    }

    @Test
    fun providersFailure_showsError_andRetryRecovers() = runTest {
        repository.failNextProvidersObservation = true
        val viewModel = newViewModel()

        assertTrue(viewModel.uiState.value.recommendedStatus is ContentStatus.Error)

        viewModel.retry()

        assertEquals(ContentStatus.Ready(), viewModel.uiState.value.recommendedStatus)
        assertEquals(3, viewModel.uiState.value.recommendedProviders.size)
    }

    @Test
    fun whileLoading_noRecommendationsYet() = runTest {
        repository.providersState.value = DataState.Loading

        val state = newViewModel().uiState.value

        assertEquals(ContentStatus.Loading, state.recommendedStatus)
        assertTrue(state.recommendedProviders.isEmpty())
    }
}
