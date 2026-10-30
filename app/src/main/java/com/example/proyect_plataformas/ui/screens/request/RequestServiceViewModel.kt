package com.example.proyect_plataformas.ui.screens.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_plataformas.LocalHandsApplication
import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.data.repository.RequestConfirmationPendingException
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.DetailState
import com.example.proyect_plataformas.ui.state.RequestFormState
import com.example.proyect_plataformas.ui.state.coverageZonesOf
import com.example.proyect_plataformas.ui.state.findProvider
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

    val providerId: String = savedStateHandle.get<String>(Route.RequestService.ARG_PROVIDER_ID).orEmpty()

    private val today: LocalDate = clock()

    private val retryCount = MutableStateFlow(0)

    private val _form = MutableStateFlow(RequestFormState(providerId))

    private val providerState: StateFlow<DetailState<Provider>> = retryCount
        .observeWithRetry { repository.observeProvidersSnapshot() }
        .map { state ->
            state.mapData { providers -> providers.findProvider(providerId) }.toDetailState(
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

    init {
        restoreDraft()
    }

    private fun restoreDraft() {
        if (providerId.isBlank()) return

        _form.update { form -> form.copy(isLoadingDraft = true) }

        viewModelScope.launch {
            try {
                val draft = repository.getRequestDraft(providerId)
                _form.update { form ->
                    if (draft == null) {
                        form.copy(isLoadingDraft = false)
                    } else {
                        form.copy(
                            description = draft.description,
                            zone = draft.zone,
                            date = draft.date,
                            timeSlot = draft.timeSlot,
                            isLoadingDraft = false,
                            draftMessage = DRAFT_RESTORED
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _form.update { form ->
                    form.copy(isLoadingDraft = false, draftError = DRAFT_LOAD_ERROR)
                }
            }
        }
    }

    /** Confirma el guardado solo después de que Room termina la escritura. */
    fun saveDraft() {
        val current = _form.value
        if (providerId.isBlank() || current.isLoadingDraft || current.isSavingDraft ||
            current.isSubmitting || current.submitted
        ) return

        val draft = RequestDraft(
            providerId = providerId,
            description = current.description,
            zone = current.zone,
            date = current.date,
            timeSlot = current.timeSlot
        )

        _form.update { form ->
            form.copy(isSavingDraft = true, draftMessage = null, draftError = null)
        }

        viewModelScope.launch {
            try {
                repository.saveRequestDraft(draft)
                _form.update { form ->
                    form.copy(isSavingDraft = false, draftMessage = DRAFT_SAVED)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _form.update { form ->
                    form.copy(isSavingDraft = false, draftError = DRAFT_SAVE_ERROR)
                }
            }
        }
    }

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

        if (current.submitted || current.isSubmitting || current.isLoadingDraft || current.isSavingDraft) return

        // Firestore rechaza solicitudes a prestadores que no están disponibles.
        if (!provider.isAvailable) {
            _form.update { form -> form.copy(submitError = UNAVAILABLE_ERROR) }
            return
        }

        if (!errorsFor(current, provider).isValid) return

        val zone = current.zone ?: return
        val date = current.date ?: return
        val timeSlot = current.timeSlot ?: return

        val request = NewServiceRequest(
            providerId = provider.id,
            description = current.description.trim(),
            zone = zone,
            date = date,
            timeSlot = timeSlot
        )

        _form.update { form -> form.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            val sent = try {
                repository.createRequest(request)
                true
            } catch (error: CancellationException) {
                throw error
            } catch (error: RequestConfirmationPendingException) {
                // Quedó guardada en el teléfono y se enviará al volver la conexión;
                // en Mis pedidos aparece como pendiente de sincronizar.
                true
            } catch (error: Exception) {
                false
            }

            // El envío y la limpieza son operaciones distintas: si falla Room,
            // no se informa un envío fallido que pueda duplicar la solicitud.
            var cleanupError: String? = null
            if (sent) {
                try {
                    repository.deleteRequestDraft(providerId)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    cleanupError = DRAFT_DELETE_ERROR
                }
            }

            _form.update { form ->
                if (sent) {
                    form.copy(
                        isSubmitting = false,
                        submitted = true,
                        draftMessage = null,
                        draftError = cleanupError
                    )
                } else {
                    form.copy(isSubmitting = false, submitError = SUBMIT_ERROR)
                }
            }
        }
    }

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    // Evita editar mientras se recupera/guarda el borrador o se envía la solicitud.
    private fun editForm(change: (RequestFormState) -> RequestFormState) {
        _form.update { current ->
            if (current.submitted || current.isSubmitting || current.isLoadingDraft || current.isSavingDraft) {
                current
            } else {
                change(current).copy(
                    submitError = null,
                    draftMessage = DRAFT_UNSAVED,
                    draftError = null
                )
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
        const val SUBMIT_ERROR = "No pudimos enviar la solicitud. Revisa tu conexión e intenta de nuevo."
        const val UNAVAILABLE_ERROR = "Este prestador no está recibiendo solicitudes por ahora."
        const val DRAFT_SAVED = "Borrador guardado en este teléfono. Puedes cerrar la aplicación."
        const val DRAFT_RESTORED = "Borrador recuperado de este teléfono."
        const val DRAFT_UNSAVED = "Hay cambios sin guardar. Toca Guardar borrador para conservarlos."
        const val DRAFT_LOAD_ERROR = "No pudimos recuperar el borrador. Vuelve a abrir este formulario para reintentar."
        const val DRAFT_SAVE_ERROR = "No pudimos guardar el borrador. Conservamos el formulario; intenta guardarlo de nuevo."
        const val DRAFT_DELETE_ERROR = "La solicitud fue enviada, pero no pudimos eliminar el borrador. No lo envíes otra vez."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                RequestServiceViewModel(
                    repository = application.appContainer.repository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
