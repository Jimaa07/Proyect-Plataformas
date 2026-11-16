package com.uvg.localhands.data.repository

import com.uvg.localhands.data.model.NewServiceRequest
import com.uvg.localhands.data.model.Provider
import com.uvg.localhands.data.model.RepositorySnapshot
import com.uvg.localhands.data.model.RequestDraft
import com.uvg.localhands.data.model.ServiceRequest
import com.uvg.localhands.data.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Envuelve al Repository real y registra cada error como no fatal antes de
 * devolverlo. El error sigue llegando al ViewModel igual que antes, así que
 * las pantallas muestran el mismo mensaje y la opción de reintentar.
 * La cancelación no es un error (se sale de la pantalla) y no se registra.
 */
class ReportingLocalHandsRepository(
    private val delegate: LocalHandsRepository,
    private val reporter: ErrorReporter
) : LocalHandsRepository {

    override fun observeCurrentUserSnapshot(): Flow<RepositorySnapshot<UserProfile?>> =
        delegate.observeCurrentUserSnapshot().reportErrors("observeCurrentUserSnapshot")

    override suspend fun saveCurrentUser(profile: UserProfile) =
        reported("saveCurrentUser") { delegate.saveCurrentUser(profile) }

    override fun observeProvidersSnapshot(): Flow<RepositorySnapshot<List<Provider>>> =
        delegate.observeProvidersSnapshot().reportErrors("observeProvidersSnapshot")

    override fun observeRequestsSnapshot(): Flow<RepositorySnapshot<List<ServiceRequest>>> =
        delegate.observeRequestsSnapshot().reportErrors("observeRequestsSnapshot")

    override fun observeProviders(): Flow<List<Provider>> =
        delegate.observeProviders().reportErrors("observeProviders")

    override fun observeProvider(providerId: String): Flow<Provider?> =
        delegate.observeProvider(providerId).reportErrors("observeProvider")

    override fun observeRequests(): Flow<List<ServiceRequest>> =
        delegate.observeRequests().reportErrors("observeRequests")

    override suspend fun getRequestDraft(providerId: String): RequestDraft? =
        reported("getRequestDraft") { delegate.getRequestDraft(providerId) }

    override suspend fun saveRequestDraft(draft: RequestDraft) =
        reported("saveRequestDraft") { delegate.saveRequestDraft(draft) }

    override suspend fun deleteRequestDraft(providerId: String) =
        reported("deleteRequestDraft") { delegate.deleteRequestDraft(providerId) }

    override suspend fun createRequest(request: NewServiceRequest): String =
        reported("createRequest") { delegate.createRequest(request) }

    override suspend fun submitRating(
        requestId: String,
        stars: Int,
        comment: String,
        reviewerName: String
    ) = reported("submitRating") {
        delegate.submitRating(requestId, stars, comment, reviewerName)
    }

    private suspend fun <T> reported(operation: String, block: suspend () -> T): T {
        return try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            reporter.recordNonFatal(operation, error)
            throw error
        }
    }

    private fun <T> Flow<T>.reportErrors(operation: String): Flow<T> =
        catch { error ->
            if (error !is CancellationException) {
                reporter.recordNonFatal(operation, error)
            }
            throw error
        }
}
