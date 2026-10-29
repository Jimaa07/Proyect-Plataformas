package com.example.proyect_plataformas.data.model

import java.time.LocalDate

enum class TimeSlot(
    val label: String,
    val hours: String
) {
    MORNING(
        label = "Mañana",
        hours = "8:00 a 12:00"
    ),
    AFTERNOON(
        label = "Tarde",
        hours = "13:00 a 17:00"
    )
}

/**
 * SENT: el vecino envió la solicitud y espera confirmación.
 * TO_RATE: el trabajo terminó y falta calificarlo.
 * RATED: el vecino ya dejó su reseña.
 */
enum class RequestStatus(
    val label: String
) {
    SENT("Enviada"),
    TO_RATE("Por calificar"),
    RATED("Calificado")
}

data class ServiceRequest(
    val id: String,
    val providerId: String,
    val providerName: String,
    val category: ServiceCategory,
    val description: String,
    val zone: String,
    val date: LocalDate,
    val timeSlot: TimeSlot,
    val status: RequestStatus,
    val rating: Int? = null,
    val reviewComment: String? = null,
    // true mientras la solicitud solo está guardada en el dispositivo
    // y todavía no llega al servidor (por ejemplo, en modo avión).
    val isPendingSync: Boolean = false
)
