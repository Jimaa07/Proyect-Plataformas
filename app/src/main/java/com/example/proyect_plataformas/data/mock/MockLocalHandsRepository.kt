package com.example.proyect_plataformas.data.mock

import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.example.proyect_plataformas.ui.format.formatLongDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Implementación temporal en memoria, con los datos de MockLocalHandsData.
 * Cumple el mismo contrato que tendrá la versión con Firestore, así que
 * los ViewModels y las pantallas no cambian cuando se reemplace en AppContainer.
 */
class MockLocalHandsRepository(
    private val today: () -> LocalDate = { LocalDate.now() },
    initialProviders: List<Provider> = MockLocalHandsData.providers,
    initialRequests: List<ServiceRequest> = MockLocalHandsData.initialRequests
) : LocalHandsRepository {

    private val providers = MutableStateFlow(initialProviders)
    private val requests = MutableStateFlow(initialRequests)

    override fun currentUser(): UserProfile = MockLocalHandsData.currentUser

    override fun observeProviders(): Flow<DataState<List<Provider>>> {
        return providers.map { list -> DataState.Ready(list) }
    }

    override fun observeProvider(providerId: String): Flow<DataState<Provider?>> {
        return providers.map { list ->
            DataState.Ready(list.firstOrNull { provider -> provider.id == providerId })
        }
    }

    override fun observeRequests(): Flow<DataState<List<ServiceRequest>>> {
        return requests.map { list -> DataState.Ready(list) }
    }

    override fun observeRequest(requestId: String): Flow<DataState<ServiceRequest?>> {
        return requests.map { list ->
            DataState.Ready(list.firstOrNull { request -> request.id == requestId })
        }
    }

    override suspend fun createRequest(
        providerId: String,
        draft: RequestDraft
    ): Result<String> {
        val provider = providers.value.firstOrNull { current -> current.id == providerId }
            ?: return Result.failure(IllegalArgumentException("El prestador no existe."))

        val request = ServiceRequest(
            id = nextRequestId(requests.value),
            providerId = provider.id,
            providerName = provider.name,
            category = provider.category,
            description = draft.description.trim(),
            zone = draft.zone,
            date = draft.date,
            timeSlot = draft.timeSlot,
            status = RequestStatus.SENT
        )

        // La solicitud nueva va primero, como la entregará Firestore (más reciente primero).
        requests.update { current -> listOf(request) + current }

        return Result.success(request.id)
    }

    override suspend fun rateRequest(
        requestId: String,
        stars: Int,
        comment: String
    ): Result<Unit> {
        val request = requests.value.firstOrNull { current -> current.id == requestId }
            ?: return Result.failure(IllegalArgumentException("La solicitud no existe."))

        if (request.status != RequestStatus.TO_RATE) {
            return Result.failure(IllegalStateException("Esta solicitud no está pendiente de calificar."))
        }

        val review = CommunityReview(
            id = "${request.id}-review",
            reviewerName = currentUser().name,
            rating = stars,
            comment = comment.trim(),
            date = formatLongDate(today())
        )

        requests.update { current ->
            current.map { item ->
                if (item.id == requestId) {
                    item.copy(
                        status = RequestStatus.RATED,
                        rating = stars,
                        reviewComment = comment.trim()
                    )
                } else {
                    item
                }
            }
        }

        providers.update { current ->
            current.map { provider ->
                if (provider.id == request.providerId) {
                    provider.withReview(review)
                } else {
                    provider
                }
            }
        }

        return Result.success(Unit)
    }
}

fun nextRequestId(requests: List<ServiceRequest>): String {
    val lastNumber = requests
        .mapNotNull { request -> request.id.removePrefix("req-").toIntOrNull() }
        .maxOrNull() ?: 0

    return "req-${lastNumber + 1}"
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
