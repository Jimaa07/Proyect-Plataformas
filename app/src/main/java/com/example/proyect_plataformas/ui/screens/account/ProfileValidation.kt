package com.example.proyect_plataformas.ui.screens.account

// Reglas del formulario de perfil, sin Compose para poder probarlas con JUnit.
// Coinciden con los límites de las reglas de Firestore para users/{uid}.

const val MIN_NAME_LETTERS = 3
const val MAX_NAME_LENGTH = 60
const val MAX_ZONE_LENGTH = 40
const val MAX_EMAIL_LENGTH = 80

private const val LOCAL_PHONE_DIGITS = 8
private const val GUATEMALA_CODE = "502"
private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/** Un mensaje por campo; null significa que ese campo está bien. */
data class ProfileFormErrors(
    val name: String? = null,
    val zone: String? = null,
    val phone: String? = null,
    val email: String? = null
) {
    val isValid: Boolean
        get() = name == null &&
                zone == null &&
                phone == null &&
                email == null
}

fun validateProfileName(name: String): String? {
    val trimmed = name.trim()

    return when {
        trimmed.isEmpty() -> "Escribe tu nombre."
        trimmed.any { char -> char.isDigit() } -> "El nombre no puede tener números."
        trimmed.count { char -> char.isLetter() } < MIN_NAME_LETTERS ->
            "El nombre debe tener al menos $MIN_NAME_LETTERS letras."
        trimmed.length > MAX_NAME_LENGTH ->
            "El nombre puede tener hasta $MAX_NAME_LENGTH caracteres."
        else -> null
    }
}

fun validateProfileZone(zone: String): String? {
    val trimmed = zone.trim()

    return when {
        trimmed.length < 2 -> "Escribe tu zona, por ejemplo Zona 10."
        trimmed.length > MAX_ZONE_LENGTH ->
            "La zona puede tener hasta $MAX_ZONE_LENGTH caracteres."
        else -> null
    }
}

/** Opcional. Acepta 8 dígitos, con o sin +502, espacios y guiones. */
fun validateProfilePhone(phone: String): String? {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return null

    val hasOnlyPhoneCharacters = trimmed.all { char ->
        char.isDigit() || char == ' ' || char == '-' || char == '+'
    }
    val digits = trimmed.filter { char -> char.isDigit() }
    val isLocal = digits.length == LOCAL_PHONE_DIGITS
    val isWithCode = digits.length == GUATEMALA_CODE.length + LOCAL_PHONE_DIGITS &&
            digits.startsWith(GUATEMALA_CODE)

    return if (hasOnlyPhoneCharacters && (isLocal || isWithCode)) {
        null
    } else {
        "Escribe un teléfono de 8 dígitos, por ejemplo 5555-0101."
    }
}

/** Opcional. Solo revisa la forma nombre@dominio.algo. */
fun validateProfileEmail(email: String): String? {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return null

    return if (trimmed.length <= MAX_EMAIL_LENGTH && EMAIL_PATTERN.matches(trimmed)) {
        null
    } else {
        "Escribe un correo válido, por ejemplo nombre@correo.com."
    }
}

fun validateProfileForm(
    name: String,
    zone: String,
    phone: String,
    email: String
): ProfileFormErrors {
    return ProfileFormErrors(
        name = validateProfileName(name),
        zone = validateProfileZone(zone),
        phone = validateProfilePhone(phone),
        email = validateProfileEmail(email)
    )
}
