package com.example.proyect_plataformas.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.state.observeWithRetry
import com.example.proyect_plataformas.ui.state.toContentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class OrdersUiState(
    val requests: List<ServiceRequest> = emptyList(),
    val status: ContentStatus = ContentStatus.Loading
)

class OrdersViewModel(
    private val repository: LocalHandsRepository
) : ViewModel() {

    private val retryCount = MutableStateFlow(0)

    // Una solicitud recién creada aparece sola porque se observa el Repository.
    val uiState: StateFlow<OrdersUiState> = retryCount
        .observeWithRetry { repository.observeRequests() }
        .map { state ->
            OrdersUiState(
                requests = ordersForDisplay((state as? DataState.Ready)?.data.orEmpty()),
                status = state.toContentStatus(
                    errorMessage = "No pudimos cargar tus pedidos."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OrdersUiState()
        )

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                OrdersViewModel(application.container.localHandsRepository)
            }
        }
    }
}
