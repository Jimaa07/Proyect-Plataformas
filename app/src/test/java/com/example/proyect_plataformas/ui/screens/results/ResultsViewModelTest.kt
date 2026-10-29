package com.example.proyect_plataformas.ui.screens.results

import androidx.lifecycle.SavedStateHandle
import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.FakeSource
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.ready
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.OFFLINE_NOTICE
import com.example.proyect_plataformas.ui.state.ResultsState
import java.io.IOException
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
class ResultsViewModelTest {

    private val repository = FakeLocalHandsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.newViewModel(category: String? = null): ResultsViewModel {
        val viewModel = ResultsViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf(Route.Results.ARG_CATEGORY to category))
        )
        // WhileSubscribed: el estado solo se calcula mientras alguien lo observa.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    @Test
    fun categoryArgument_showsOnlyThatCategory() = runTest {
        val viewModel = newViewModel(category = "PLUMBING")

        assertEquals(
            setOf("carlos-hernandez", "luis-ramirez"),
            viewModel.uiState.value.providers.map { provider -> provider.id }.toSet()
        )
        assertEquals(ContentStatus.Ready(), viewModel.uiState.value.status)
    }

    @Test
    fun filtersAndSearch_updateTheVisibleList() = runTest {
        val viewModel = newViewModel(category = "PLUMBING")

        viewModel.selectFilter(ProviderFilter.AVAILABLE)
        assertEquals(listOf("carlos-hernandez"), viewModel.uiState.value.providers.map { provider -> provider.id })

        viewModel.updateQuery("no existe nadie así")
        assertTrue(viewModel.uiState.value.providers.isEmpty())

        viewModel.clearFilters()
        assertEquals(ResultsState(), viewModel.uiState.value.filters)
        assertEquals(MockLocalHandsData.providers.size, viewModel.uiState.value.providers.size)
    }

    @Test
    fun unknownCategory_showsEveryProvider() = runTest {
        val viewModel = newViewModel(category = "NO_EXISTE")

        assertEquals(null, viewModel.uiState.value.filters.category)
        assertEquals(MockLocalHandsData.providers.size, viewModel.uiState.value.providers.size)
    }

    @Test
    fun withoutResponse_staysLoading() = runTest {
        repository.providersSource.value = FakeSource.Waiting

        val viewModel = newViewModel()

        assertEquals(ContentStatus.Loading, viewModel.uiState.value.status)
    }

    @Test
    fun failure_showsError_andRetryLoadsAgain() = runTest {
        repository.providersSource.value = FakeSource.Fails(IOException("Sin respuesta"))
        val viewModel = newViewModel()

        assertTrue(viewModel.uiState.value.status is ContentStatus.Error)
        assertTrue(viewModel.uiState.value.providers.isEmpty())

        repository.providersSource.value = ready(MockLocalHandsData.providers)
        viewModel.retry()

        assertEquals(ContentStatus.Ready(), viewModel.uiState.value.status)
        assertEquals(MockLocalHandsData.providers.size, viewModel.uiState.value.providers.size)
    }

    @Test
    fun cachedData_showsOfflineNotice() = runTest {
        repository.providersSource.value = ready(MockLocalHandsData.providers, isFromCache = true)

        val viewModel = newViewModel()

        assertEquals(ContentStatus.Ready(notice = OFFLINE_NOTICE), viewModel.uiState.value.status)
        assertEquals(MockLocalHandsData.providers.size, viewModel.uiState.value.providers.size)
    }

    @Test
    fun providerChanges_updateTheListWithoutReopening() = runTest {
        val viewModel = newViewModel()

        repository.providersSource.value = ready(MockLocalHandsData.providers.take(2))

        assertEquals(2, viewModel.uiState.value.providers.size)
    }
}
