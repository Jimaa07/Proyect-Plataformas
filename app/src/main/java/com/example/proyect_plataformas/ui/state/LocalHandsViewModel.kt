package com.example.proyect_plataformas.ui.state

import androidx.lifecycle.ViewModel
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * Único dueño del estado de LocalHands (MVVM, flujo unidireccional):
 *
 * - Expone un StateFlow inmutable; las pantallas lo observan y no lo modifican.
 * - Las pantallas le mandan eventos (funciones públicas de abajo).
 * - Cada evento produce un estado nuevo con una transformación pura de
 *   LocalHandsUiState.kt, que es donde vive la lógica y la validación.
 *
 * Sobrevive a la rotación y a la navegación. Lo efímero de la interfaz
 * (si el calendario está abierto, un snackbar) se queda en cada pantalla.
 */
class LocalHandsViewModel(
    initialState: LocalHandsUiState = initialLocalHandsState()
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<LocalHandsUiState> = _uiState.asStateFlow()

    // ---------- Resultados ----------

    /** Se llama al entrar a Resultados desde una categoría, el buscador o Explorar. */
    fun openResults(category: ServiceCategory?) {
        _uiState.update { state ->
            state.copy(results = ResultsState(category = category))
        }
    }

    fun selectCategory(category: ServiceCategory?) {
        _uiState.update { state ->
            state.withResults { results -> results.copy(category = category) }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { state ->
            state.withResults { results -> results.copy(query = query) }
        }
    }

    fun selectFilter(filter: ProviderFilter) {
        _uiState.update { state ->
            state.withResults { results -> results.copy(filter = filter) }
        }
    }

    fun clearResultsFilters() {
        _uiState.update { state ->
            state.withResults { ResultsState() }
        }
    }

    // ---------- Solicitud de servicio ----------

    /** Empieza un formulario limpio para ese prestador. */
    fun startRequest(providerId: String) {
        _uiState.update { state ->
            state.copy(requestForm = RequestFormState(providerId))
        }
    }

    fun updateDescription(providerId: String, description: String) {
        _uiState.update { state ->
            state.withRequestForm(providerId) { form -> form.copy(description = description) }
        }
    }

    fun selectZone(providerId: String, zone: String) {
        _uiState.update { state ->
            state.withRequestForm(providerId) { form -> form.copy(zone = zone) }
        }
    }

    fun selectDate(providerId: String, date: LocalDate) {
        _uiState.update { state ->
            state.withRequestForm(providerId) { form -> form.copy(date = date) }
        }
    }

    fun selectTimeSlot(providerId: String, timeSlot: TimeSlot) {
        _uiState.update { state ->
            state.withRequestForm(providerId) { form -> form.copy(timeSlot = timeSlot) }
        }
    }

    fun submitRequest(providerId: String) {
        _uiState.update { state ->
            state.withSubmittedRequest(providerId)
        }
    }

    // ---------- Calificación ----------

    /** Empieza un formulario limpio para calificar ese pedido. */
    fun startRating(requestId: String) {
        _uiState.update { state ->
            state.copy(ratingForm = RatingFormState(requestId))
        }
    }

    fun selectStars(requestId: String, stars: Int) {
        _uiState.update { state ->
            state.withRatingForm(requestId) { form -> form.copy(stars = stars) }
        }
    }

    fun updateComment(requestId: String, comment: String) {
        _uiState.update { state ->
            state.withRatingForm(requestId) { form -> form.copy(comment = comment) }
        }
    }

    fun submitRating(requestId: String) {
        _uiState.update { state ->
            state.withSubmittedRating(requestId)
        }
    }
}
