package com.uvg.localhands.data.repository

import com.uvg.localhands.data.model.NewServiceRequest
import com.uvg.localhands.data.model.Provider
import com.uvg.localhands.data.model.RepositorySnapshot
import com.uvg.localhands.data.model.RequestDraft
import com.uvg.localhands.data.model.ServiceRequest
import com.uvg.localhands.data.model.TimeSlot
import com.uvg.localhands.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class FirestoreLocalHandsRepository(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    private val draftDao: RequestDraftDao
) : LocalHandsRepository {

    private val session = FirebaseSession(auth)
    private val reader = FirestoreLocalHandsReader(firestore)
    private val requestWriter = FirestoreRequestWriter(firestore)
    private val ratingWriter = FirestoreRatingWriter(firestore)
    private val userWriter = FirestoreUserWriter(firestore)

    override fun observeCurrentUserSnapshot():
            Flow<RepositorySnapshot<UserProfile?>> {
        return flow {
            val userId = session.requireUserId()
            emitAll(reader.observeUserSnapshot(userId))
        }
    }

    override suspend fun saveCurrentUser(profile: UserProfile) {
        val userId = session.requireUserId()

        userWriter.saveUser(
            userId = userId,
            profile = profile
        )
    }

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

    override suspend fun getRequestDraft(providerId: String): RequestDraft? {
        require(providerId.isNotBlank()) { "Provider ID cannot be blank" }

        val entity = draftDao.getDraft(providerId) ?: return null
        return RequestDraft(
            providerId = entity.providerId,
            description = entity.description,
            zone = entity.zone,
            date = entity.date?.let { LocalDate.parse(it) },
            timeSlot = entity.timeSlot?.let { TimeSlot.valueOf(it) }
        )
    }

    override suspend fun saveRequestDraft(draft: RequestDraft) {
        require(draft.providerId.isNotBlank()) { "Provider ID cannot be blank" }

        // Un borrador conserva también campos incompletos y el texto tal como se escribió.
        draftDao.saveDraft(
            RequestDraftEntity(
                providerId = draft.providerId,
                description = draft.description,
                zone = draft.zone,
                date = draft.date?.toString(),
                timeSlot = draft.timeSlot?.name
            )
        )
    }

    override suspend fun deleteRequestDraft(providerId: String) {
        require(providerId.isNotBlank()) { "Provider ID cannot be blank" }
        draftDao.deleteDraft(providerId)
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
