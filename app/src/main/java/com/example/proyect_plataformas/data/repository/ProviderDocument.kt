package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ServiceCategory

internal data class ProviderDocument(
    val name: String = "",
    val specialty: String = "",
    val category: String = "",
    val zone: String = "",
    val experienceYears: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val available: Boolean = false,
    val description: String = "",
    val coverageZones: List<String> = emptyList(),
    val contactPhone: String = "",
    val contactEmail: String = "",
    val reviews: List<ReviewDocument> = emptyList()
) {
    fun toDomain(documentId: String): Provider {
        require(name.isNotBlank()) {
            "Provider $documentId has no name"
        }

        val serviceCategory = ServiceCategory.entries.firstOrNull {
            it.name == category
        } ?: throw IllegalArgumentException(
            "Provider $documentId has an invalid category: $category"
        )

        return Provider(
            id = documentId,
            name = name,
            specialty = specialty,
            category = serviceCategory,
            zone = zone,
            experienceYears = experienceYears,
            rating = rating,
            reviewCount = reviewCount,
            isAvailable = available,
            description = description,
            coverageZones = coverageZones,
            contactPhone = contactPhone,
            contactEmail = contactEmail,
            reviews = reviews.map { it.toDomain() }
        )
    }
}

internal data class ReviewDocument(
    val reviewerName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val date: String = ""
) {
    fun toDomain(): CommunityReview {
        return CommunityReview(
            reviewerName = reviewerName,
            rating = rating,
            comment = comment,
            date = date
        )
    }
}