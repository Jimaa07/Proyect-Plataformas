package com.example.proyect_plataformas.ui.screens.profile

// Enlaces de contacto del prestador, sin Android para poder probarlos con JUnit.

private const val GUATEMALA_CODE = "502"
private const val LOCAL_NUMBER_LENGTH = 8

/**
 * "+502 5555-0101" -> "50255550101". Si el número viene sin código de país
 * (8 dígitos), se agrega el de Guatemala. Devuelve null si no hay número.
 */
fun internationalPhoneDigits(phone: String): String? {
    val digits = phone.filter { char -> char.isDigit() }

    return when {
        digits.isEmpty() -> null
        digits.length == LOCAL_NUMBER_LENGTH -> GUATEMALA_CODE + digits
        else -> digits
    }
}

/** Abre el marcador con el número escrito; llamar lo decide la persona. */
fun dialUri(phone: String): String? {
    return internationalPhoneDigits(phone)?.let { digits -> "tel:+$digits" }
}

/** Enlace oficial de WhatsApp: abre la app si está instalada o el navegador si no. */
fun whatsAppUrl(phone: String): String? {
    return internationalPhoneDigits(phone)?.let { digits -> "https://wa.me/$digits" }
}
