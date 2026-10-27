package com.example.proyect_plataformas.ui.screens.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.ResultsState
import com.example.proyect_plataformas.ui.state.observeWithRetry
import com.example.proyect_plataformas.ui.state.toContentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ResultsUiState(
    val filters: ResultsState = ResultsState(),
    val providers: List<Provider> = emptyList(),
    val status: ContentStatus = ContentStatus.Loading
)

class ResultsViewModel(
    private val repository: LocalHandsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // La categoría llega como argumento de navegación (null = todas).
    private val initialCategory: ServiceCategory? =
        savedStateHandle.get<String>(Route.Results.ARG_CATEGORY)
            ?.let { name -> ServiceCategory.entries.firstOrNull { category -> category.name == name } }

    private val filters = MutableStateFlow(ResultsState(category = initialCategory))
    private val retryCount = MutableStateFlow(0)

    val uiState: StateFlow<ResultsUiState> = combine(
        retryCount.observeWithRetry { repository.observeProviders() },
        filters
    ) { providersState, currentFilters ->
        val providers = (providersState as? DataState.Ready)?.data.orEmpty()

        ResultsUiState(
            filters = currentFilters,
            // Buscar, filtrar y ordenar se hace en la app sobre lo que ya llegó.
            providers = providersForResults(
                providers = providers,
                category = currentFilters.category,
                query = currentFilters.query,
                filter = currentFilters.filter
            ),
            status = providersState.toContentStatus(
                errorMessage = "No pudimos cargar los prestadores."
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ResultsUiState(filters = ResultsState(category = initialCategory))
    )

    fun selectCategory(category: ServiceCategory?) {
        filters.update { current -> current.copy(category = category) }
    }

    fun updateQuery(query: String) {
        filters.update { current -> current.copy(query = query) }
    }

    fun selectFilter(filter: ProviderFilter) {
        filters.update { current -> current.copy(filter = filter) }
    }

    fun clearFilters() {
        filters.value = ResultsState()
    }

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                ResultsViewModel(
                    repository = application.container.localHandsRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
