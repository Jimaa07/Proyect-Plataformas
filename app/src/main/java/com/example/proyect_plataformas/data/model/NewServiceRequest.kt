package com.example.proyect_plataformas.data.model

import java.time.LocalDate

data class NewServiceRequest(
    val providerId: String,
    val description: String,
    val zone: String,
    val date: LocalDate,
    val timeSlot: TimeSlot
)