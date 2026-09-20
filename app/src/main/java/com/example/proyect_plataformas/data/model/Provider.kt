package com.example.proyect_plataformas.data.model

data class Provider(
    val id: String,
    val name: String,
    val specialty: String,
    val zone: String,
    val experienceYears: Int,
    val rating: Double,
    val reviewCount: Int,
    val isAvailable: Boolean
)