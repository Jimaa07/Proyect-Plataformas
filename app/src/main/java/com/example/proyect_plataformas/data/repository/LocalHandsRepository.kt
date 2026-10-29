package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import com.example.proyect_plataformas.data.model.RepositorySnapshot

interface LocalHandsRepository {

    /** Vecino que usa la app. Todavía no hay inicio de sesión con perfil propio. */
    fun currentUser(): UserProfile

    fun observeProvidersSnapshot():
            Flow<RepositorySnapshot<List<Provider>>>

    fun observeRequestsSnapshot():
            Flow<RepositorySnapshot<List<ServiceRequest>>>

    fun observeProviders(): Flow<List<Provider>>

    fun observeProvider(providerId: String): Flow<Provider?>

    fun observeRequests(): Flow<List<ServiceRequest>>

    suspend fun createRequest(request: NewServiceRequest): String

    suspend fun submitRating(
        requestId: String,
        stars: Int,
        comment: String,
        reviewerName: String
    )
}