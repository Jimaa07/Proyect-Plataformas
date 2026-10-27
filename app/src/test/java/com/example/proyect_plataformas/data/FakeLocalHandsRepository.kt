package com.example.proyect_plataformas.data

import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.data.repository.DataState
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Repository controlable desde las pruebas: decide qué emite y qué responde cada escritura. */
class FakeLocalHandsRepository(
    providers: List<Provider> = MockLocalHandsData.providers,
    requests: List<ServiceRequest> = MockLocalHandsData.initialRequests
) : LocalHandsRepository {

    val providersState = MutableStateFlow<DataState<List<Provider>>>(DataState.Ready(providers))
    val requestsState = MutableStateFlow<DataState<List<ServiceRequest>>>(DataState.Ready(requests))

    var failNextProvidersObservation = false

    var createRequestResult: Result<String> = Result.success("req-nuevo")
    var rateRequestResult: Result<Unit> = Result.success(Unit)

    // Si no es null, las escrituras esperan a que la prueba lo complete.
    var writeGate: CompletableDeferred<Unit>? = null

    val createdRequests = mutableListOf<Pair<String, RequestDraft>>()
    val ratings = mutableListOf<Triple<String, Int, String>>()

    override fun currentUser(): UserProfile = MockLocalHandsData.currentUser

    override fun observeProviders(): Flow<DataState<List<Provider>>> {
        if (failNextProvidersObservation) {
            failNextProvidersObservation = false
            return flow { throw IOException("Sin respuesta") }
        }

        return providersState
    }

    override fun observeProvider(providerId: String): Flow<DataState<Provider?>> {
        return providersState.map { state ->
            when (state) {
                is DataState.Ready -> DataState.Ready(
                    data = state.data.firstOrNull { provider -> provider.id == providerId },
                    isFromCache = state.isFromCache,
                    hasPendingWrites = state.hasPendingWrites
                )

                is DataState.Failure -> state
                DataState.Loading -> DataState.Loading
            }
        }
    }

    override fun observeRequests(): Flow<DataState<List<ServiceRequest>>> = requestsState

    override fun observeRequest(requestId: String): Flow<DataState<ServiceRequest?>> {
        return requestsState.map { state ->
            when (state) {
                is DataState.Ready -> DataState.Ready(
                    data = state.data.firstOrNull { request -> request.id == requestId },
                    isFromCache = state.isFromCache,
                    hasPendingWrites = state.hasPendingWrites
                )

                is DataState.Failure -> state
                DataState.Loading -> DataState.Loading
            }
        }
    }

    override suspend fun createRequest(
        providerId: String,
        draft: RequestDraft
    ): Result<String> {
        createdRequests += providerId to draft
        writeGate?.await()
        return createRequestResult
    }

    override suspend fun rateRequest(
        requestId: String,
        stars: Int,
        comment: String
    ): Result<Unit> {
        ratings += Triple(requestId, stars, comment)
        writeGate?.await()
        return rateRequestResult
    }
}
