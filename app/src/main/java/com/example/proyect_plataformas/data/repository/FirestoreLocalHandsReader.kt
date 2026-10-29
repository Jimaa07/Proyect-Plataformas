package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RepositorySnapshot
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FirestoreLocalHandsReader(
    private val firestore: FirebaseFirestore
) {
    fun observeProviders(): Flow<List<Provider>> {
        return observeProvidersSnapshot().map { it.value }
    }

    fun observeProvidersSnapshot():
            Flow<RepositorySnapshot<List<Provider>>> {
        return firestore.collection("providers")
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                RepositorySnapshot(
                    value = snapshot.documents.map {
                        it.toProvider()
                    },
                    isFromCache = snapshot.metadata.isFromCache,
                    hasPendingWrites =
                        snapshot.metadata.hasPendingWrites()
                )
            }
    }

    fun observeProvider(providerId: String): Flow<Provider?> {
        require(providerId.isNotBlank()) {
            "Provider ID cannot be blank"
        }

        return firestore.collection("providers")
            .document(providerId)
            .snapshots()
            .map { document ->
                if (document.exists()) {
                    document.toProvider()
                } else {
                    null
                }
            }
    }

    fun observeRequests(
        userId: String
    ): Flow<List<ServiceRequest>> {
        return observeRequestsSnapshot(userId).map { it.value }
    }

    fun observeRequestsSnapshot(
        userId: String
    ): Flow<RepositorySnapshot<List<ServiceRequest>>> {
        require(userId.isNotBlank()) {
            "User ID cannot be blank"
        }

        return firestore.collection("serviceRequests")
            .whereEqualTo("ownerId", userId)
            .snapshots(MetadataChanges.INCLUDE)
            .map { snapshot ->
                RepositorySnapshot(
                    value = snapshot.documents.map {
                        it.toServiceRequest()
                    },
                    isFromCache = snapshot.metadata.isFromCache,
                    hasPendingWrites =
                        snapshot.metadata.hasPendingWrites()
                )
            }
    }

    private fun DocumentSnapshot.toProvider(): Provider {
        val providerDocument = requireNotNull(
            toObject(ProviderDocument::class.java)
        ) {
            "Cannot read provider $id"
        }

        return providerDocument.toDomain(id)
    }

    private fun DocumentSnapshot.toServiceRequest(): ServiceRequest {
        val requestDocument = requireNotNull(
            toObject(ServiceRequestDocument::class.java)
        ) {
            "Cannot read request $id"
        }

        return requestDocument.toDomain(id)
    }
}