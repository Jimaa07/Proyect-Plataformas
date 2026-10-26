package com.example.proyect_plataformas.ui.screens.request

import com.example.proyect_plataformas.data.model.TimeSlot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// Reglas del formulario de solicitud, separadas de Compose para poder
// probarlas con JUnit (igual que RatingValidation.kt).

const val MIN_PROBLEM_LENGTH = 20
const val MIN_PROBLEM_WORDS = 3
const val MAX_DAYS_AHEAD = 60L

/** Datos ya validados que la pantalla le entrega al ViewModel. */
data class RequestDraft(
    val description: String,
    val zone: String,
    val date: LocalDate,
    val timeSlot: TimeSlot
)

/** Un mensaje por campo; null significa que ese campo está bien. */
data class RequestFormErrors(
    val description: String?,
    val zone: String?,
    val date: String?,
    val timeSlot: String?
) {
    val isValid: Boolean
        get() = description == null &&
                zone == null &&
                date == null &&
                timeSlot == null

    val pendingMessages: List<String>
        get() = listOfNotNull(description, zone, date, timeSlot)
}

fun validateDescription(description: String): String? {
    val text = description.trim()

    val wordCount = text
        .split(Regex("\\s+"))
        .count { word -> word.any { char -> char.isLetter() } }

    return when {
        text.isEmpty() ->
            "Describe el problema: qué falla y en qué parte de la casa."

        text.length < MIN_PROBLEM_LENGTH -> {
            val missing = MIN_PROBLEM_LENGTH - text.length
            val unit = if (missing == 1) "carácter" else "caracteres"
            "Agrega más detalle para el profesional (faltan $missing $unit)."
        }

        wordCount < MIN_PROBLEM_WORDS ->
            "Explica el problema con al menos $MIN_PROBLEM_WORDS palabras."

        else -> null
    }
}

fun validateZone(
    zone: String?,
    coverageZones: List<String>,
    providerName: String
): String? {
    if (zone.isNullOrBlank()) {
        return "Elige la zona donde se hará el trabajo."
    }

    val chosenZone = zone.trim()

    val isCovered = coverageZones.isEmpty() ||
            coverageZones.any { coveredZone ->
                coveredZone.equals(chosenZone, ignoreCase = true)
            }

    return if (isCovered) {
        null
    } else {
        "$providerName no trabaja en $chosenZone. " +
                "Cubre: ${coverageZones.joinToString(", ")}."
    }
}

fun validateDate(
    date: LocalDate?,
    today: LocalDate
): String? {
    return when {
        date == null ->
            "Elige una fecha en el calendario."

        date.isBefore(today) ->
            "Esa fecha ya pasó. Elige una a partir de hoy."

        date.isAfter(today.plusDays(MAX_DAYS_AHEAD)) ->
            "Solo se puede agendar hasta $MAX_DAYS_AHEAD días adelante."

        else -> null
    }
}

fun validateTimeSlot(timeSlot: TimeSlot?): String? {
    return if (timeSlot == null) {
        "Elige si prefieres la visita en la mañana o en la tarde."
    } else {
        null
    }
}

fun validateRequestForm(
    description: String,
    zone: String?,
    date: LocalDate?,
    timeSlot: TimeSlot?,
    coverageZones: List<String>,
    providerName: String,
    today: LocalDate
): RequestFormErrors {
    return RequestFormErrors(
        description = validateDescription(description),
        zone = validateZone(zone, coverageZones, providerName),
        date = validateDate(date, today),
        timeSlot = validateTimeSlot(timeSlot)
    )
}

// El DatePicker de Material 3 trabaja con milisegundos en UTC
// (medianoche del día elegido). Estas funciones hacen la conversión.

fun utcMillisToLocalDate(utcMillis: Long): LocalDate {
    return Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}

fun localDateToUtcMillis(date: LocalDate): Long {
    return date.atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

/** Regla del calendario: desde hoy y hasta MAX_DAYS_AHEAD días después. */
fun isSelectableRequestDate(
    utcMillis: Long,
    today: LocalDate
): Boolean {
    return validateDate(utcMillisToLocalDate(utcMillis), today) == null
}
