package com.uvg.localhands.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uvg.localhands.LocalHandsApplication
import com.uvg.localhands.data.model.RequestStatus
import com.uvg.localhands.data.model.UserProfile
import com.uvg.localhands.data.repository.LocalHandsRepository
import com.uvg.localhands.ui.state.ContentStatus
import com.uvg.localhands.ui.state.DataState
import com.uvg.localhands.ui.state.observeWithRetry
import com.uvg.localhands.ui.state.toContentStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Formulario del perfil mientras el vecino lo crea o lo edita. */
data class ProfileFormState(
    val name: String = "",
    val zone: String = "",
    val phone: String = "",
    val email: String = "",
    val showErrors: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null
)

data class AccountUiState(
    val profileStatus: ContentStatus = ContentStatus.Loading,
    val profile: UserProfile? = null,
    // Abierto cuando todavía no hay perfil guardado o cuando se está editando.
    val form: ProfileFormState? = null,
    val formErrors: ProfileFormErrors = ProfileFormErrors(),
    val activeRequests: Int = 0,
    val pendingRatings: Int = 0,
    val reviewsWritten: Int = 0
)

class AccountViewModel(
    private val repository: LocalHandsRepository
) : ViewModel() {

    private val retryCount = MutableStateFlow(0)

    // null = no se está editando; el formulario de creación sale del perfil vacío.
    private val editingForm = MutableStateFlow<ProfileFormState?>(null)

    val uiState: StateFlow<AccountUiState> = combine(
        retryCount.observeWithRetry { repository.observeCurrentUserSnapshot() },
        retryCount.observeWithRetry { repository.observeRequestsSnapshot() },
        editingForm
    ) { profileState, requestsState, form ->
        val profile = (profileState as? DataState.Ready)?.content
        val requests = (requestsState as? DataState.Ready)?.content.orEmpty()
        val needsProfile = profileState is DataState.Ready && profile == null
        val visibleForm = form ?: if (needsProfile) ProfileFormState() else null

        AccountUiState(
            profileStatus = profileState.toContentStatus(
                errorMessage = "No pudimos cargar tu perfil."
            ),
            profile = profile,
            form = visibleForm,
            formErrors = visibleForm?.let(::errorsFor) ?: ProfileFormErrors(),
            activeRequests = requests.count { request -> request.status == RequestStatus.SENT },
            pendingRatings = requests.count { request -> request.status == RequestStatus.TO_RATE },
            reviewsWritten = requests.count { request -> request.status == RequestStatus.RATED }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountUiState()
    )

    fun startEditing() {
        val profile = uiState.value.profile ?: return

        editingForm.value = ProfileFormState(
            name = profile.name,
            zone = profile.zone,
            phone = profile.phone,
            email = profile.email
        )
    }

    fun cancelEditing() {
        if (editingForm.value?.isSaving == true) return
        editingForm.value = null
    }

    fun updateName(name: String) {
        editForm { form -> form.copy(name = name) }
    }

    fun updateZone(zone: String) {
        editForm { form -> form.copy(zone = zone) }
    }

    fun updatePhone(phone: String) {
        editForm { form -> form.copy(phone = phone) }
    }

    fun updateEmail(email: String) {
        editForm { form -> form.copy(email = email) }
    }

    /** Guarda en Firestore solo si el formulario es válido y no hay otro guardado en curso. */
    fun saveProfile() {
        val current = editingForm.value ?: ProfileFormState()

        if (current.isSaving) return

        if (!errorsFor(current).isValid) {
            editingForm.value = current.copy(showErrors = true)
            return
        }

        editingForm.value = current.copy(
            showErrors = true,
            isSaving = true,
            saveError = null
        )

        viewModelScope.launch {
            val saved = try {
                repository.saveCurrentUser(
                    UserProfile(
                        name = current.name.trim(),
                        zone = current.zone.trim(),
                        phone = current.phone.trim(),
                        email = current.email.trim()
                    )
                )
                true
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                false
            }

            if (saved) {
                editingForm.value = null
            } else {
                editingForm.update { form ->
                    form?.copy(isSaving = false, saveError = SAVE_ERROR)
                }
            }
        }
    }

    fun retry() {
        retryCount.update { count -> count + 1 }
    }

    // Mientras se guarda no se edita; escribir de nuevo borra el error anterior.
    private fun editForm(change: (ProfileFormState) -> ProfileFormState) {
        editingForm.update { current ->
            val form = current ?: ProfileFormState()

            if (form.isSaving) {
                form
            } else {
                change(form).copy(saveError = null)
            }
        }
    }

    private fun errorsFor(form: ProfileFormState): ProfileFormErrors {
        return validateProfileForm(
            name = form.name,
            zone = form.zone,
            phone = form.phone,
            email = form.email
        )
    }

    companion object {
        const val SAVE_ERROR = "No pudimos guardar tu perfil. Revisa tu conexión e intenta de nuevo."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LocalHandsApplication
                AccountViewModel(application.appContainer.repository)
            }
        }
    }
}
