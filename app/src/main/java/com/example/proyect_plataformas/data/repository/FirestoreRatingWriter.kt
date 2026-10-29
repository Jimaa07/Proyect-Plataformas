package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.RequestStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import kotlin.math.roundToInt

private const val RATING_OPERATION_TIMEOUT_MILLIS = 15_000L
private const val MIN_REVIEW_LENGTH = 10

internal class FirestoreRatingWriter(
    private val firestore: FirebaseFirestore
) {
    suspend fun submitRating(
        requestId: String,
        stars: Int,
        comment: String,
        reviewerName: String,
        userId: String
    ) {
        val trimmedComment = comment.trim()
        val trimmedName = reviewerName.trim()

        require(userId.isNotBlank()) {
            "User ID cannot be blank"
        }
        require(requestId.isNotBlank()) {
            "Request ID cannot be blank"
        }
        require(stars in 1..5) {
            "Rating must be between 1 and 5"
        }
        require(trimmedComment.length >= MIN_REVIEW_LENGTH) {
            "Review must contain at least $MIN_REVIEW_LENGTH characters"
        }
        require(trimmedName.isNotBlank()) {
            "Reviewer name cannot be blank"
        }
        val requestReference = firestore
            .collection("serviceRequests")
            .document(requestId)

        val reviewDate = LocalDate.now().toString()

        try {
            withTimeout(RATING_OPERATION_TIMEOUT_MILLIS) {
                firestore.runTransaction { transaction ->
                    val requestSnapshot = transaction.get(requestReference)

                    require(requestSnapshot.exists()) {
                        "Request $requestId does not exist"
                    }

                    require(requestSnapshot.getString("ownerId") == userId) {
                        "Request $requestId does not belong to the current user"
                    }

                    val requestDocument = requireNotNull(
                        requestSnapshot.toObject(
                            ServiceRequestDocument::class.java
                        )
                    )

                    val request = requestDocument.toDomain(requestId)

                    // Repetir el mismo envío no agrega otra reseña.
                    if (request.status == RequestStatus.RATED) {
                        require(
                            request.rating == stars &&
                                    request.reviewComment == trimmedComment
                        ) {
                            "Request $requestId already has a different rating"
                        }

                        return@runTransaction Unit
                    }

                    require(request.status == RequestStatus.TO_RATE) {
                        "Request $requestId is not ready to be rated"
                    }

                    val providerReference = firestore
                        .collection("providers")
                        .document(request.providerId)

                    val providerSnapshot = transaction.get(providerReference)

                    require(providerSnapshot.exists()) {
                        "Provider ${request.providerId} does not exist"
                    }

                    val providerDocument = requireNotNull(
                        providerSnapshot.toObject(
                            ProviderDocument::class.java
                        )
                    )

                    val provider = providerDocument.toDomain(
                        providerSnapshot.id
                    )

                    val previousTotal = providerDocument.ratingTotal
                        ?: (provider.rating * provider.reviewCount)

                    val newTotal = previousTotal + stars
                    val newCount = provider.reviewCount + 1
                    val newAverage =
                        (newTotal / newCount * 10).roundToInt() / 10.0

                    val review = ReviewDocument(
                        reviewerName = trimmedName,
                        rating = stars,
                        comment = trimmedComment,
                        date = reviewDate
                    )

                    transaction.update(
                        providerReference,
                        mapOf(
                            "lastRatedRequestId" to requestId,
                            "ratingTotal" to newTotal,
                            "rating" to newAverage,
                            "reviewCount" to newCount,
                            "reviews" to (
                                    listOf(review) + providerDocument.reviews
                                    )
                        )
                    )

                    transaction.update(
                        requestReference,
                        mapOf(
                            "status" to RequestStatus.RATED.name,
                            "rating" to stars,
                            "reviewComment" to trimmedComment
                        )
                    )

                    Unit
                }.await()
            }
        } catch (exception: TimeoutCancellationException) {
            currentCoroutineContext().ensureActive()

            throw RatingConfirmationPendingException(
                requestId = requestId,
                cause = exception
            )
        }
    }
}