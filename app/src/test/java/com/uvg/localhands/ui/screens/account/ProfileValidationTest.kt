package com.uvg.localhands.ui.screens.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidationTest {

    @Test
    fun nameAndZone_areRequired() {
        val errors = validateProfileForm(name = "  ", zone = "", phone = "", email = "")

        assertEquals("Escribe tu nombre.", errors.name)
        assertEquals("Escribe tu zona, por ejemplo Zona 10.", errors.zone)
        assertFalse(errors.isValid)
    }

    @Test
    fun phoneAndEmail_areOptional() {
        val errors = validateProfileForm(name = "Lucía Pérez", zone = "Zona 12", phone = "", email = "")

        assertTrue(errors.isValid)
    }

    @Test
    fun name_needsThreeLettersAndNoDigits() {
        assertEquals("El nombre debe tener al menos 3 letras.", validateProfileName("Al"))
        assertEquals("El nombre no puede tener números.", validateProfileName("Ana 2"))
        assertNull(validateProfileName("  Ana  "))
    }

    @Test
    fun phone_acceptsLocalOrWithCountryCode() {
        assertNull(validateProfilePhone("5555-0101"))
        assertNull(validateProfilePhone("+502 5555 0101"))
        assertEquals(
            "Escribe un teléfono de 8 dígitos, por ejemplo 5555-0101.",
            validateProfilePhone("555-01")
        )
        assertEquals(
            "Escribe un teléfono de 8 dígitos, por ejemplo 5555-0101.",
            validateProfilePhone("5555-01OI")
        )
    }

    @Test
    fun email_needsUserAndDomain() {
        assertNull(validateProfileEmail("lucia@correo.com"))
        assertEquals(
            "Escribe un correo válido, por ejemplo nombre@correo.com.",
            validateProfileEmail("lucia@correo")
        )
    }
}
