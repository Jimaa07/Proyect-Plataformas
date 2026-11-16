package com.uvg.localhands.data.model

/** Vecino que usa la app; se guarda en Firestore en users/{uid}. */
data class UserProfile(
    val name: String,
    val zone: String,
    val phone: String,
    val email: String
)
