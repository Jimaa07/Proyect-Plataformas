package com.example.proyect_plataformas.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.ui.state.observeWithRetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AccountUiState(
    val user: UserProfile,
    val activeRequests: Int = 0,
    val pendingRatings: Int = 0,
    val reviewsWritten: Int = 0
)

class AccountViewModel(
    private val repository: LocalHandsRepository
) : ViewModel() {

    val uiState: StateFlow<AccountUiState> = MutableStateFlow(0)
        .observeWithRetry { repository.observeRequests() }
        .map { state ->
            val requests = (state as? DataState.Ready)?.data.orEmpty()

            AccountUiState(
                user = repository.currentUser(),
                activeRequests = requests.count { request -> request.status == RequestStatus.SENT },
                pendingRatings = requests.count { request -> request.status == RequestStatus.TO_RATE },
                reviewsWritten = requests.count { request -> request.status == RequestStatus.RATED }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountUiState(user = repository.currentUser())
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                AccountViewModel(application.container.localHandsRepository)
            }
        }
    }
}
