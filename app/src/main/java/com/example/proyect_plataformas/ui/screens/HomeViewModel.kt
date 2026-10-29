package com.example.proyect_plataformas.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.DataState
import com.example.proyect_plataformas.ui.state.observeWithRetry
import com.example.proyect_plataformas.ui.state.toContentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val userName: String,
    val recommendedProviders: List<Provider> = emptyList(),
    val recommendedStatus: ContentStatus = ContentStatus.Loading,
    val pendingRatings: Int = 0
)

class HomeViewModel(
    private val repository: LocalHandsRepository
) : ViewModel() {

    private val retryCount = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        retryCount.observeWithRetry { repository.observeProvidersSnapshot() },
        retryCount.observeWithRetry { repository.observeRequestsSnapshot() }
    ) { providersState, requestsState ->
        val providers = (providersState as? DataState.Ready)?.data.orEmpty()
        val requests = (requestsState as? DataState.Ready)?.data.orEmpty()

        HomeUiState(
            userName = repository.currentUser().name,
            recommendedProviders = recommendedProviders(providers),
            recommendedStatus = providersState.toContentStatus(
                errorMessage = "No pudimos cargar los recomendados."
            ),
            pendingRatings = requests.count { request ->
                request.status == RequestStatus.TO_RATE
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(userName = repository.currentUser().name)
    )

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                HomeViewModel(application.appContainer.repository)
            }
        }
    }
}

/** Los mejor calificados primero; si empatan, los que tienen más reseñas. */
fun recommendedProviders(
    providers: List<Provider>,
    limit: Int = 3
): List<Provider> {
    return providers
        .sortedWith(
            compareByDescending<Provider> { provider -> provider.rating }
                .thenByDescending { provider -> provider.reviewCount }
        )
        .take(limit)
}
