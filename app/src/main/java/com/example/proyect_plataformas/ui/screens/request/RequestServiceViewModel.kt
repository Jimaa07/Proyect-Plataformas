package com.example.proyect_plataformas.ui.screens.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.ui.state.DetailState
import com.example.proyect_plataformas.ui.state.RequestFormState
import com.example.proyect_plataformas.ui.state.coverageZonesOf
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
import java.time.LocalDate

data class RequestServiceUiState(
    val providerState: DetailState<Provider>,
    val form: RequestFormState,
    val formErrors: RequestFormErrors,
    val today: LocalDate
)

class RequestServiceViewModel(
    private val repository: LocalHandsRepository,
    savedStateHandle: SavedStateHandle,
    clock: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    val providerId: String = savedStateHandle.get<String>(ARG_PROVIDER_ID).orEmpty()

    private val today: LocalDate = clock()

    private val retryCount = MutableStateFlow(0)

    private val _form = MutableStateFlow(RequestFormState(providerId))

    private val providerState: StateFlow<DetailState<Provider>> = retryCount
        .observeWithRetry { repository.observeProvider(providerId) }
        .map { state ->
            state.toDetailState(
                errorMessage = "No pudimos cargar al prestador.",
                offlineMessage = "Sin conexión y este prestador no está guardado en el teléfono."
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailState.Loading
        )

    val uiState: StateFlow<RequestServiceUiState> = combine(
        providerState,
        _form
    ) { provider, currentForm ->
        RequestServiceUiState(
            providerState = provider,
            form = currentForm,
            formErrors = errorsFor(currentForm, (provider as? DetailState.Ready)?.data),
            today = today
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RequestServiceUiState(
            providerState = DetailState.Loading,
            form = _form.value,
            formErrors = errorsFor(_form.value, provider = null),
            today = today
        )
    )

    fun updateDescription(description: String) {
        editForm { current -> current.copy(description = description) }
    }

    fun selectZone(zone: String) {
        editForm { current -> current.copy(zone = zone) }
    }

    fun selectDate(date: LocalDate) {
        editForm { current -> current.copy(date = date) }
    }

    fun selectTimeSlot(timeSlot: TimeSlot) {
        editForm { current -> current.copy(timeSlot = timeSlot) }
    }

    /** Envía solo si el formulario es válido y no hay otro envío en curso. */
    fun submit() {
        val provider = (providerState.value as? DetailState.Ready)?.data ?: return
        val current = _form.value

        if (current.submitted || current.isSubmitting) return
        if (!errorsFor(current, provider).isValid) return

        val zone = current.zone ?: return
        val date = current.date ?: return
        val timeSlot = current.timeSlot ?: return

        val draft = RequestDraft(
            description = current.description.trim(),
            zone = zone,
            date = date,
            timeSlot = timeSlot
        )

        _form.update { form -> form.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            val result = try {
                repository.createRequest(provider.id, draft)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(error)
            }

            _form.update { form ->
                if (result.isSuccess) {
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

    // Mientras se envía o después de enviar, el formulario ya no se edita.
    private fun editForm(change: (RequestFormState) -> RequestFormState) {
        _form.update { current ->
            if (current.submitted || current.isSubmitting) {
                current
            } else {
                change(current).copy(submitError = null)
            }
        }
    }

    private fun errorsFor(
        form: RequestFormState,
        provider: Provider?
    ): RequestFormErrors {
        return validateRequestForm(
            description = form.description,
            zone = form.zone,
            date = form.date,
            timeSlot = form.timeSlot,
            coverageZones = provider?.let(::coverageZonesOf).orEmpty(),
            providerName = provider?.name.orEmpty(),
            today = today
        )
    }

    companion object {
        const val ARG_PROVIDER_ID = "providerId"
        const val SUBMIT_ERROR = "No pudimos enviar la solicitud. Revisa tu conexión e intenta de nuevo."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                RequestServiceViewModel(
                    repository = application.container.localHandsRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
