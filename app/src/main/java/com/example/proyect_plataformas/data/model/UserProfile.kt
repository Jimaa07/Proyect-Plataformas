package com.example.proyect_plataformas.data.model

/** Vecino que usa la app (mock mientras no haya inicio de sesión). */
data class UserProfile(
    val name: String,
    val zone: String,
    val phone: String,
    val email: String
)
