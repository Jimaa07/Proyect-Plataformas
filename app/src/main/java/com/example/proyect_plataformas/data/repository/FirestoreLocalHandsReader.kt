package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FirestoreLocalHandsReader(
    private val firestore: FirebaseFirestore
) {
    fun observeProviders(): Flow<List<Provider>> {
        return firestore.collection("providers")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { document ->
                    val providerDocument = requireNotNull(
                        document.toObject(ProviderDocument::class.java)
                    ) {
                        "Cannot read provider ${document.id}"
                    }

                    providerDocument.toDomain(document.id)
                }
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
                if (!document.exists()) {
                    null
                } else {
                    val providerDocument = requireNotNull(
                        document.toObject(ProviderDocument::class.java)
                    ) {
                        "Cannot read provider ${document.id}"
                    }

                    providerDocument.toDomain(document.id)
                }
            }
    }

    fun observeRequests(): Flow<List<ServiceRequest>> {
        return firestore.collection("serviceRequests")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { document ->
                    val requestDocument = requireNotNull(
                        document.toObject(ServiceRequestDocument::class.java)
                    ) {
                        "Cannot read request ${document.id}"
                    }

                    requestDocument.toDomain(document.id)
                }
            }
    }
}