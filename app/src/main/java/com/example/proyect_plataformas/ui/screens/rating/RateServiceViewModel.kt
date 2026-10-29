package com.example.proyect_plataformas.ui.screens.rating

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.DetailState
import com.example.proyect_plataformas.ui.state.RatingFormState
import com.example.proyect_plataformas.ui.state.mapData
import com.example.proyect_plataformas.ui.state.observeWithRetry
import com.example.proyect_plataformas.ui.state.toDetailState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RateServiceUiState(
    val requestState: DetailState<ServiceRequest> = DetailState.Loading,
    val form: RatingFormState
)

class RateServiceViewModel(
    private val repository: LocalHandsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val requestId: String = savedStateHandle.get<String>(Route.RateService.ARG_REQUEST_ID).orEmpty()

    private val retryCount = MutableStateFlow(0)

    private val _form = MutableStateFlow(RatingFormState(requestId))

    private val requestState: StateFlow<DetailState<ServiceRequest>> = retryCount
        .observeWithRetry { repository.observeRequestsSnapshot() }
        .map { state ->
            state.mapData { requests -> requests.firstOrNull { request -> request.id == requestId } }.toDetailState(
                errorMessage = "No pudimos cargar el servicio.",
                offlineMessage = "Sin conexión y este servicio no está guardado en el teléfono."
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailState.Loading
        )

    val uiState: StateFlow<RateServiceUiState> = combine(
        requestState,
        _form
    ) { request, form ->
        RateServiceUiState(requestState = request, form = form)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RateServiceUiState(form = _form.value)
    )

    fun selectStars(stars: Int) {
        editForm { form -> form.copy(stars = stars) }
    }

    fun updateComment(comment: String) {
        editForm { form -> form.copy(comment = comment) }
    }

    /** Guarda la reseña en el Repository; el perfil del prestador se actualiza solo. */
    fun submit() {
        val current = _form.value

        if (current.submitted || current.isSubmitting || !current.isValid) return
        if (requestState.value !is DetailState.Ready) return

        _form.update { form -> form.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            // Si falla (también si la confirmación tarda demasiado), se puede
            // reintentar: repetir la misma calificación no agrega otra reseña.
            val saved = try {
                repository.submitRating(
                    requestId = requestId,
                    stars = current.stars,
                    comment = current.comment.trim(),
                    reviewerName = repository.currentUser().name
                )
                true
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                false
            }

            _form.update { form ->
                if (saved) {
                    form.copy(isSubmitting = false, submitted = true)
                } else {
                    form.copy(isSubmitting = false, submitError = SUBMIT_ERROR)
                }
            }
        }
    }

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    private fun editForm(change: (RatingFormState) -> RatingFormState) {
        _form.update { current ->
            if (current.submitted || current.isSubmitting) {
                current
            } else {
                change(current).copy(submitError = null)
            }
        }
    }

    companion object {
        const val SUBMIT_ERROR = "No pudimos guardar tu calificación. Intenta de nuevo."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                RateServiceViewModel(
                    repository = application.appContainer.repository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
