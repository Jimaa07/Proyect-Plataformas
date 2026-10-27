package com.example.proyect_plataformas.data.model

import java.time.LocalDate

/** Datos ya validados de una solicitud nueva que el ViewModel le pasa al Repository. */
data class RequestDraft(
    val description: String,
    val zone: String,
    val date: LocalDate,
    val timeSlot: TimeSlot
)
