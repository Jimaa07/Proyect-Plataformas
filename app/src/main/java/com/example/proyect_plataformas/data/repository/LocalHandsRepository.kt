package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Única puerta de acceso a los datos de LocalHands. Los ViewModels solo
 * conocen esta interface y reciben modelos de dominio; los documentos de
 * Firestore (o cualquier otra fuente) se convierten dentro de la implementación.
 *
 * Lecturas: devuelven un Flow que emite cada vez que los datos cambian.
 * Sin conexión, la implementación entrega lo que haya en caché con
 * isFromCache = true. Si no hay nada guardado, emite Ready con una lista
 * vacía (o null) e isFromCache = true, nunca se queda en Loading para siempre.
 *
 * Escrituras: son funciones suspend que devuelven en cuanto el cambio quedó
 * guardado en el dispositivo, sin esperar la confirmación del servidor.
 * Si no hay conexión, el cambio se sincroniza después y los Flow lo marcan
 * con hasPendingWrites (y la solicitud con isPendingSync). Devuelven
 * Result.failure solo cuando el cambio no se puede hacer (por ejemplo,
 * el prestador no existe o el pedido ya fue calificado).
 */
interface LocalHandsRepository {

    /** Vecino que usa la app. Todavía no hay inicio de sesión en esta entrega. */
    fun currentUser(): UserProfile

    fun observeProviders(): Flow<DataState<List<Provider>>>

    /** Ready(null) con isFromCache = false significa que el prestador no existe. */
    fun observeProvider(providerId: String): Flow<DataState<Provider?>>

    /** Solicitudes del vecino, de la más reciente a la más antigua. */
    fun observeRequests(): Flow<DataState<List<ServiceRequest>>>

    /** Ready(null) con isFromCache = false significa que la solicitud no existe. */
    fun observeRequest(requestId: String): Flow<DataState<ServiceRequest?>>

    /** Crea la solicitud con estado SENT y devuelve su id. */
    suspend fun createRequest(
        providerId: String,
        draft: RequestDraft
    ): Result<String>

    /**
     * Guarda la reseña de una solicitud TO_RATE: la marca como RATED y
     * actualiza el promedio, la cantidad y la lista de reseñas del prestador.
     */
    suspend fun rateRequest(
        requestId: String,
        stars: Int,
        comment: String
    ): Result<Unit>
}
