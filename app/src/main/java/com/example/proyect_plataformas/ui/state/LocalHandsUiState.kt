package com.example.proyect_plataformas.ui.state

import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.CompletedService
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.ui.format.formatLongDate
import com.example.proyect_plataformas.ui.screens.rating.isRatingFormValid
import com.example.proyect_plataformas.ui.screens.request.RequestFormErrors
import com.example.proyect_plataformas.ui.screens.request.validateRequestForm
import com.example.proyect_plataformas.ui.screens.results.providersForResults
import java.time.LocalDate
import kotlin.math.roundToInt

// =====================================================================
// Estado de la app (inmutable). LocalHandsViewModel es su único dueño:
// lo expone como StateFlow, las pantallas solo lo leen y le mandan eventos.
// =====================================================================

data class LocalHandsUiState(
    // Datos compartidos por varias pantallas
    val currentUser: UserProfile,
    val providers: List<Provider>,
    val requests: List<ServiceRequest>,
    val today: LocalDate,

    // Estado de cada flujo
    val results: ResultsState = ResultsState(),
    val requestForm: RequestFormState? = null,
    val ratingForm: RatingFormState? = null
) {
    val requestsToRate: List<ServiceRequest>
        get() = requests.filter { request ->
            request.status == RequestStatus.TO_RATE
        }

    /** Lo que muestra Resultados: se deriva de los filtros, no se guarda aparte. */
    val visibleProviders: List<Provider>
        get() = providersForResults(
            providers = providers,
            category = results.category,
            query = results.query,
            filter = results.filter
        )

    fun providerById(providerId: String): Provider? {
        return providers.firstOrNull { provider ->
            provider.id == providerId
        }
    }

    fun requestById(requestId: String): ServiceRequest? {
        return requests.firstOrNull { request ->
            request.id == requestId
        }
    }

    fun recommendedProviders(limit: Int = 3): List<Provider> {
        return providers
            .sortedWith(
                compareByDescending<Provider> { provider -> provider.rating }
                    .thenByDescending { provider -> provider.reviewCount }
            )
            .take(limit)
    }

    /** Formulario de ese prestador; si no había uno, empieza vacío. */
    fun requestFormFor(providerId: String): RequestFormState {
        return requestForm?.takeIf { form -> form.providerId == providerId }
            ?: RequestFormState(providerId)
    }

    /** Errores del formulario de solicitud: se recalculan con cada cambio. */
    fun requestErrorsFor(providerId: String): RequestFormErrors {
        val form = requestFormFor(providerId)
        val provider = providerById(providerId)

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

    fun ratingFormFor(requestId: String): RatingFormState {
        return ratingForm?.takeIf { form -> form.requestId == requestId }
            ?: RatingFormState(requestId)
    }
}

fun initialLocalHandsState(
    today: LocalDate = LocalDate.now()
): LocalHandsUiState {
    return LocalHandsUiState(
        currentUser = MockLocalHandsData.currentUser,
        providers = MockLocalHandsData.providers,
        requests = MockLocalHandsData.initialRequests,
        today = today
    )
}

// =====================================================================
// Transformaciones puras: reciben un estado y devuelven uno nuevo.
// El ViewModel solo las llama; por eso se pueden probar con JUnit.
// =====================================================================

fun LocalHandsUiState.withResults(
    change: (ResultsState) -> ResultsState
): LocalHandsUiState {
    return copy(results = change(results))
}

fun LocalHandsUiState.withRequestForm(
    providerId: String,
    change: (RequestFormState) -> RequestFormState
): LocalHandsUiState {
    val form = requestFormFor(providerId)

    // Una solicitud ya enviada no se puede editar.
    if (form.submitted) return this

    return copy(requestForm = change(form))
}

/** Envía la solicitud solo si el formulario es válido y no se envió antes. */
fun LocalHandsUiState.withSubmittedRequest(
    providerId: String
): LocalHandsUiState {
    val form = requestFormFor(providerId)

    if (form.submitted) return this
    if (!requestErrorsFor(providerId).isValid) return this

    val zone = form.zone ?: return this
    val date = form.date ?: return this
    val timeSlot = form.timeSlot ?: return this

    val withRequest = withNewRequest(
        providerId = providerId,
        draft = RequestDraft(
            description = form.description.trim(),
            zone = zone,
            date = date,
            timeSlot = timeSlot
        )
    )

    if (withRequest === this) return this

    return withRequest.copy(requestForm = form.copy(submitted = true))
}

fun LocalHandsUiState.withRatingForm(
    requestId: String,
    change: (RatingFormState) -> RatingFormState
): LocalHandsUiState {
    val form = ratingFormFor(requestId)

    if (form.submitted) return this

    return copy(ratingForm = change(form))
}

/** Guarda la reseña solo si es válida y el pedido sigue por calificar. */
fun LocalHandsUiState.withSubmittedRating(
    requestId: String
): LocalHandsUiState {
    val form = ratingFormFor(requestId)

    if (form.submitted || !form.isValid) return this

    val rated = withRating(
        requestId = requestId,
        stars = form.stars,
        comment = form.comment,
        reviewDate = formatLongDate(today)
    )

    if (rated === this) return this

    return rated.copy(ratingForm = form.copy(submitted = true))
}

fun nextRequestId(requests: List<ServiceRequest>): String {
    val lastNumber = requests
        .mapNotNull { request -> request.id.removePrefix("req-").toIntOrNull() }
        .maxOrNull() ?: 0

    return "req-${lastNumber + 1}"
}

fun LocalHandsUiState.withNewRequest(
    providerId: String,
    draft: RequestDraft
): LocalHandsUiState {
    val provider = providerById(providerId) ?: return this

    val request = ServiceRequest(
        id = nextRequestId(requests),
        providerId = provider.id,
        providerName = provider.name,
        category = provider.category,
        description = draft.description.trim(),
        zone = draft.zone,
        date = draft.date,
        timeSlot = draft.timeSlot,
        status = RequestStatus.SENT
    )

    // El pedido nuevo va primero para que aparezca arriba en "Mis pedidos".
    return copy(requests = listOf(request) + requests)
}

fun LocalHandsUiState.withRating(
    requestId: String,
    stars: Int,
    comment: String,
    reviewDate: String
): LocalHandsUiState {
    val request = requestById(requestId) ?: return this

    if (request.status != RequestStatus.TO_RATE) return this
    if (!isRatingFormValid(stars, comment)) return this

    val review = CommunityReview(
        reviewerName = currentUser.name,
        rating = stars,
        comment = comment.trim(),
        date = reviewDate
    )

    return copy(
        requests = requests.map { current ->
            if (current.id == requestId) {
                current.copy(
                    status = RequestStatus.RATED,
                    rating = stars,
                    reviewComment = comment.trim()
                )
            } else {
                current
            }
        },
        providers = providers.map { provider ->
            if (provider.id == request.providerId) {
                provider.withReview(review)
            } else {
                provider
            }
        }
    )
}

/** Agrega la reseña al perfil y recalcula el promedio (1 decimal). */
fun Provider.withReview(review: CommunityReview): Provider {
    val newCount = reviewCount + 1
    val newAverage = (rating * reviewCount + review.rating) / newCount

    return copy(
        rating = (newAverage * 10).roundToInt() / 10.0,
        reviewCount = newCount,
        reviews = listOf(review) + reviews
    )
}

