package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.UserProfile

/** Documento users/{uid} de Firestore; no sale de la capa de datos. */
internal data class UserDocument(
    val name: String = "",
    val zone: String = "",
    val phone: String = "",
    val email: String = ""
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            name = name,
            zone = zone,
            phone = phone,
            email = email
        )
    }
}

internal fun UserProfile.toDocument(): UserDocument {
    return UserDocument(
        name = name.trim(),
        zone = zone.trim(),
        phone = phone.trim(),
        email = email.trim()
    )
}
