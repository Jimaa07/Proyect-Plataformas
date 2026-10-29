package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import com.example.proyect_plataformas.data.model.RepositorySnapshot

class FirestoreLocalHandsRepository(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : LocalHandsRepository {

    private val session = FirebaseSession(auth)
    private val reader = FirestoreLocalHandsReader(firestore)
    private val requestWriter = FirestoreRequestWriter(firestore)
    private val ratingWriter = FirestoreRatingWriter(firestore)

    override fun observeProviders(): Flow<List<Provider>> {
        return flow {
            session.requireUserId()
            emitAll(reader.observeProviders())
        }
    }

    override fun observeProvider(providerId: String): Flow<Provider?> {
        return flow {
            session.requireUserId()
            emitAll(reader.observeProvider(providerId))
        }
    }

    override fun observeRequests(): Flow<List<ServiceRequest>> {
        return flow {
            val userId = session.requireUserId()
            emitAll(reader.observeRequests(userId))
        }
    }

    override fun observeProvidersSnapshot():
            Flow<RepositorySnapshot<List<Provider>>> {
        return flow {
            session.requireUserId()
            emitAll(reader.observeProvidersSnapshot())
        }
    }

    override fun observeRequestsSnapshot():
            Flow<RepositorySnapshot<List<ServiceRequest>>> {
        return flow {
            val userId = session.requireUserId()
            emitAll(reader.observeRequestsSnapshot(userId))
        }
    }

    override suspend fun createRequest(
        request: NewServiceRequest
    ): String {
        val userId = session.requireUserId()

        return requestWriter.createRequest(
            request = request,
            userId = userId
        )
    }

    override suspend fun submitRating(
        requestId: String,
        stars: Int,
        comment: String,
        reviewerName: String
    ) {
        val userId = session.requireUserId()

        ratingWriter.submitRating(
            requestId = requestId,
            stars = stars,
            comment = comment,
            reviewerName = reviewerName,
            userId = userId
        )
    }
}