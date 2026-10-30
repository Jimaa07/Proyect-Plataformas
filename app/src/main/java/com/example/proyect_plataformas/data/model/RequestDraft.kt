package com.example.proyect_plataformas.data.model

import java.time.LocalDate

/** Solicitud sin enviar; puede conservar campos que todavía faltan por llenar. */
data class RequestDraft(
    val providerId: String,
    val description: String = "",
    val zone: String? = null,
    val date: LocalDate? = null,
    val timeSlot: TimeSlot? = null
)
