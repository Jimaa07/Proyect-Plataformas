package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.RequestStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private const val REQUEST_OPERATION_TIMEOUT_MILLIS = 15_000L

internal class FirestoreRequestWriter(
    private val firestore: FirebaseFirestore
) {
    suspend fun createRequest(request: NewServiceRequest): String {
        require(request.providerId.isNotBlank()) {
            "Provider ID cannot be blank"
        }
        require(request.description.isNotBlank()) {
            "Description cannot be blank"
        }
        require(request.zone.isNotBlank()) {
            "Zone cannot be blank"
        }

        val providerSnapshot = withTimeout(
            REQUEST_OPERATION_TIMEOUT_MILLIS
        ) {
            firestore.collection("providers")
                .document(request.providerId)
                .get(Source.SERVER)
                .await()
        }

        require(providerSnapshot.exists()) {
            "Provider ${request.providerId} does not exist"
        }

        val providerDocument = requireNotNull(
            providerSnapshot.toObject(ProviderDocument::class.java)
        ) {
            "Cannot read provider ${request.providerId}"
        }

        val provider = providerDocument.toDomain(providerSnapshot.id)

        require(provider.isAvailable) {
            "Provider ${provider.id} is unavailable"
        }

        val coverageZones = provider.coverageZones.ifEmpty {
            listOf(provider.zone)
        }

        require(
            coverageZones.any {
                it.equals(request.zone.trim(), ignoreCase = true)
            }
        ) {
            "Provider ${provider.id} does not cover the selected zone"
        }

        val requestDocument = ServiceRequestDocument(
            providerId = provider.id,
            providerName = provider.name,
            category = provider.category.name,
            description = request.description.trim(),
            zone = request.zone.trim(),
            date = request.date.toString(),
            timeSlot = request.timeSlot.name,
            status = RequestStatus.SENT.name
        )

        val documentReference = firestore
            .collection("serviceRequests")
            .document()

        try {
            withTimeout(REQUEST_OPERATION_TIMEOUT_MILLIS) {
                documentReference.set(requestDocument).await()
            }
        } catch (exception: TimeoutCancellationException) {
            currentCoroutineContext().ensureActive()

            throw RequestConfirmationPendingException(
                requestId = documentReference.id,
                cause = exception
            )
        }

        return documentReference.id
    }
}