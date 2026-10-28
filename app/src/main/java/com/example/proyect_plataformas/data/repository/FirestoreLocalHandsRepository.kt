package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class FirestoreLocalHandsRepository(
    firestore: FirebaseFirestore
) : LocalHandsRepository {

    private val reader = FirestoreLocalHandsReader(firestore)
    private val requestWriter = FirestoreRequestWriter(firestore)
    private val ratingWriter = FirestoreRatingWriter(firestore)

    override fun observeProviders(): Flow<List<Provider>> {
        return reader.observeProviders()
    }

    override fun observeProvider(providerId: String): Flow<Provider?> {
        return reader.observeProvider(providerId)
    }

    override fun observeRequests(): Flow<List<ServiceRequest>> {
        return reader.observeRequests()
    }

    override suspend fun createRequest(
        request: NewServiceRequest
    ): String {
        return requestWriter.createRequest(request)
    }

    override suspend fun submitRating(
        requestId: String,
        stars: Int,
        comment: String,
        reviewerName: String
    ) {
        ratingWriter.submitRating(
            requestId = requestId,
            stars = stars,
            comment = comment,
            reviewerName = reviewerName
        )
    }
}