package com.example.proyect_plataformas.ui.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Funciones de texto sin Compose: se pueden probar con JUnit normal.

private val SPANISH: Locale = Locale.forLanguageTag("es-GT")

private val LONG_DATE: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", SPANISH)

private val DAY_AND_DATE: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", SPANISH)

/** "3 de octubre de 2026" (mismo formato que las reseñas del mock). */
fun formatLongDate(date: LocalDate): String {
    return date.format(LONG_DATE)
}

/** "Lunes 20 de octubre": para mostrar la fecha elegida en un pedido. */
fun formatDayAndDate(date: LocalDate): String {
    return date.format(DAY_AND_DATE)
        .replaceFirstChar { char -> char.titlecase(SPANISH) }
}

/** "Carlos Méndez" -> "CM". */
fun initialsOf(name: String): String {
    return name
        .trim()
        .split(" ")
        .filter { word -> word.isNotBlank() }
        .take(2)
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar() }
        .joinToString("")
}

/** 4 -> "★★★★☆". */
fun starsText(rating: Int): String {
    val stars = rating.coerceIn(0, 5)
    return "★".repeat(stars) + "☆".repeat(5 - stars)
}
