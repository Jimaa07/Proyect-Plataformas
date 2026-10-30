package com.example.proyect_plataformas.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.DetailState
import com.example.proyect_plataformas.ui.state.findProvider
import com.example.proyect_plataformas.ui.state.mapData
import com.example.proyect_plataformas.ui.state.observeWithRetry
import com.example.proyect_plataformas.ui.state.toDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val repository: LocalHandsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val providerId: String = savedStateHandle.get<String>(Route.Profile.ARG_PROVIDER_ID).orEmpty()

    private val retryCount = MutableStateFlow(0)

    // Se observa la lista de prestadores (trae los metadatos de caché) y se
    // busca este por id: si alguien lo califica, el perfil se actualiza solo.
    val uiState: StateFlow<DetailState<Provider>> = retryCount
        .observeWithRetry { repository.observeProvidersSnapshot() }
        .map { state ->
            state.mapData { providers -> providers.findProvider(providerId) }.toDetailState(
                errorMessage = "No pudimos cargar el perfil.",
                offlineMessage = "Sin conexión y este perfil no está guardado en el teléfono."
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailState.Loading
        )

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                ProfileViewModel(
                    repository = application.appContainer.repository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
