package com.example.proyect_plataformas.data

import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RepositorySnapshot
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update

/** Lo que entrega una lectura del fake en este momento. */
sealed interface FakeSource<out T> {

    /** Todavía no llega ninguna respuesta (la pantalla debe seguir en Cargando). */
    data object Waiting : FakeSource<Nothing>

    data class Ready<T>(val snapshot: RepositorySnapshot<T>) : FakeSource<T>

    /** La lectura falla con este error, como cuando el listener de Firestore se cae. */
    data class Fails(val error: Throwable) : FakeSource<Nothing>
}

fun <T> ready(
    value: T,
    isFromCache: Boolean = false,
    hasPendingWrites: Boolean = false
): FakeSource<T> = FakeSource.Ready(RepositorySnapshot(value, isFromCache, hasPendingWrites))

data class SubmittedRating(
    val requestId: String,
    val stars: Int,
    val comment: String,
    val reviewerName: String
)

/**
 * Repository en memoria que cumple el mismo contrato que el de Firestore.
 * La prueba decide qué emite cada lectura y si una escritura falla o espera;
 * si no se le dice nada, las escrituras cambian los datos como lo haría
 * Firestore (la solicitud aparece en Mis pedidos, la reseña en el perfil).
 */
class FakeLocalHandsRepository(
    providers: List<Provider> = MockLocalHandsData.providers,
    requests: List<ServiceRequest> = MockLocalHandsData.initialRequests
) : LocalHandsRepository {

    val providersSource = MutableStateFlow<FakeSource<List<Provider>>>(ready(providers))
    val requestsSource = MutableStateFlow<FakeSource<List<ServiceRequest>>>(ready(requests))

    var createRequestError: Exception? = null
    var submitRatingError: Exception? = null

    // Si no es null, las escrituras esperan a que la prueba lo complete.
    var writeGate: CompletableDeferred<Unit>? = null

    val createdRequests = mutableListOf<NewServiceRequest>()
    val ratings = mutableListOf<SubmittedRating>()

    override fun currentUser(): UserProfile = MockLocalHandsData.currentUser

    override fun observeProvidersSnapshot(): Flow<RepositorySnapshot<List<Provider>>> =
        providersSource.emissions()

    override fun observeRequestsSnapshot(): Flow<RepositorySnapshot<List<ServiceRequest>>> =
        requestsSource.emissions()

    override fun observeProviders(): Flow<List<Provider>> =
        observeProvidersSnapshot().map { snapshot -> snapshot.value }

    override fun observeProvider(providerId: String): Flow<Provider?> =
        observeProviders().map { list -> list.firstOrNull { provider -> provider.id == providerId } }

    override fun observeRequests(): Flow<List<ServiceRequest>> =
        observeRequestsSnapshot().map { snapshot -> snapshot.value }

    override suspend fun createRequest(request: NewServiceRequest): String {
        createdRequests += request
        writeGate?.await()
        createRequestError?.let { error -> throw error }

        val provider = currentValue(providersSource).first { provider -> provider.id == request.providerId }
        val id = "req-nuevo-${createdRequests.size}"
        val created = ServiceRequest(
            id = id,
            providerId = provider.id,
            providerName = provider.name,
            category = provider.category,
            description = request.description,
            zone = request.zone,
            date = request.date,
            timeSlot = request.timeSlot,
            status = RequestStatus.SENT
        )

        requestsSource.update { ready(listOf(created) + currentValue(requestsSource)) }
        return id
    }

    override suspend fun submitRating(
        requestId: String,
        stars: Int,
        comment: String,
        reviewerName: String
    ) {
        ratings += SubmittedRating(requestId, stars, comment, reviewerName)
        writeGate?.await()
        submitRatingError?.let { error -> throw error }

        val request = currentValue(requestsSource).first { request -> request.id == requestId }
        val review = CommunityReview(
            id = "$requestId-review",
            reviewerName = reviewerName,
            rating = stars,
            comment = comment,
            date = "2026-10-15"
        )

        requestsSource.update {
            ready(
                currentValue(requestsSource).map { current ->
                    if (current.id == requestId) {
                        current.copy(status = RequestStatus.RATED, rating = stars, reviewComment = comment)
                    } else {
                        current
                    }
                }
            )
        }

        providersSource.update {
            ready(
                currentValue(providersSource).map { provider ->
                    if (provider.id == request.providerId) {
                        val count = provider.reviewCount + 1
                        provider.copy(
                            rating = (provider.rating * provider.reviewCount + stars) / count,
                            reviewCount = count,
                            reviews = listOf(review) + provider.reviews
                        )
                    } else {
                        provider
                    }
                }
            )
        }
    }

    private fun <T> MutableStateFlow<FakeSource<T>>.emissions(): Flow<RepositorySnapshot<T>> {
        return transform { source ->
            when (source) {
                FakeSource.Waiting -> Unit
                is FakeSource.Ready -> emit(source.snapshot)
                is FakeSource.Fails -> throw source.error
            }
        }
    }

    private fun <T> currentValue(source: MutableStateFlow<FakeSource<T>>): T {
        return (source.value as FakeSource.Ready).snapshot.value
    }
}
