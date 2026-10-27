package com.example.proyect_plataformas.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.mock.MockLocalHandsRepository
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.DetailState
import com.example.proyect_plataformas.ui.state.OFFLINE_NOTICE
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
class ProfileViewModelTest {

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
        providerId: String,
        source: LocalHandsRepository = repository
    ): ProfileViewModel {
        val viewModel = ProfileViewModel(
            repository = source,
            savedStateHandle = SavedStateHandle(mapOf(Route.Profile.ARG_PROVIDER_ID to providerId))
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun existingProvider_isShown() = runTest {
        val viewModel = newViewModel("carlos-mendez")

        val state = viewModel.uiState.value as DetailState.Ready
        assertEquals("Carlos Méndez", state.data.name)
        assertEquals(null, state.notice)
    }

    @Test
    fun unknownProvider_isNotFound() = runTest {
        assertEquals(DetailState.NotFound, newViewModel("no-existe").uiState.value)
    }

    @Test
    fun offlineWithoutCache_isAnError_notNotFound() = runTest {
        repository.providersState.value = DataState.Ready(emptyList(), isFromCache = true)

        assertTrue(newViewModel("carlos-mendez").uiState.value is DetailState.Error)
    }

    @Test
    fun cachedProvider_showsOfflineNotice() = runTest {
        repository.providersState.value = DataState.Ready(MockLocalHandsData.providers, isFromCache = true)

        val state = newViewModel("carlos-mendez").uiState.value as DetailState.Ready
        assertEquals(OFFLINE_NOTICE, state.notice)
    }

    @Test
    fun ratingElsewhere_updatesTheOpenProfile() = runTest {
        val mock = MockLocalHandsRepository(today = { LocalDate.of(2026, 10, 15) })
        val viewModel = newViewModel("carlos-mendez", source = mock)

        mock.rateRequest("req-1", 5, "Excelente trabajo, muy puntual")

        val state = viewModel.uiState.value as DetailState.Ready
        assertEquals(143, state.data.reviewCount)
        assertEquals("Excelente trabajo, muy puntual", state.data.reviews.first().comment)
    }
}
