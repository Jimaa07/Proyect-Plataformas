package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.TimeSlot
import java.time.LocalDate

internal data class ServiceRequestDocument(
    val providerId: String = "",
    val providerName: String = "",
    val category: String = "",
    val description: String = "",
    val zone: String = "",
    val date: String = "",
    val timeSlot: String = "",
    val status: String = "",
    val rating: Int? = null,
    val reviewComment: String? = null
) {
    fun toDomain(documentId: String): ServiceRequest {
        require(providerId.isNotBlank()) {
            "Request $documentId has no providerId"
        }

        val serviceCategory = ServiceCategory.entries.firstOrNull {
            it.name == category
        } ?: throw IllegalArgumentException(
            "Request $documentId has an invalid category: $category"
        )

        val requestTimeSlot = TimeSlot.entries.firstOrNull {
            it.name == timeSlot
        } ?: throw IllegalArgumentException(
            "Request $documentId has an invalid timeSlot: $timeSlot"
        )

        val requestStatus = RequestStatus.entries.firstOrNull {
            it.name == status
        } ?: throw IllegalArgumentException(
            "Request $documentId has an invalid status: $status"
        )

        return ServiceRequest(
            id = documentId,
            providerId = providerId,
            providerName = providerName,
            category = serviceCategory,
            description = description,
            zone = zone,
            date = LocalDate.parse(date),
            timeSlot = requestTimeSlot,
            status = requestStatus,
            rating = rating,
            reviewComment = reviewComment
        )
    }
}