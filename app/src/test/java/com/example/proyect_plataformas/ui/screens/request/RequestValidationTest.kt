package com.example.proyect_plataformas.ui.screens.request

import com.example.proyect_plataformas.data.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RequestValidationTest {

    private val today = LocalDate.of(2026, 10, 15)
    private val carlosZones = listOf("Zona 10", "Zona 14", "Zona 15", "Zona 16")

    // ---------- Descripción ----------

    @Test
    fun description_empty_asksWhatFails() {
        assertEquals(
            "Describe el problema: qué falla y en qué parte de la casa.",
            validateDescription("   ")
        )
    }

    @Test
    fun description_tooShort_saysHowManyCharactersAreMissing() {
        // "Se fue la luz" tiene 13 caracteres: faltan 7 para llegar a 20.
        assertEquals(
            "Agrega más detalle para el profesional (faltan 7 caracteres).",
            validateDescription("Se fue la luz")
        )
    }

    @Test
    fun description_longButOneWord_asksForWords() {
        assertEquals(
            "Explica el problema con al menos 3 palabras.",
            validateDescription("aaaaaaaaaaaaaaaaaaaaaaaaa")
        )
    }

    @Test
    fun description_complete_isValid() {
        assertNull(validateDescription("El tomacorriente de la cocina no funciona"))
    }

    // ---------- Zona ----------

    @Test
    fun zone_notChosen_asksToChoose() {
        assertEquals(
            "Elige la zona donde se hará el trabajo.",
            validateZone(null, carlosZones, "Carlos Méndez")
        )
    }

    @Test
    fun zone_outsideCoverage_namesProviderAndHisZones() {
        assertEquals(
            "Carlos Méndez no trabaja en Mixco. Cubre: Zona 10, Zona 14, Zona 15, Zona 16.",
            validateZone("Mixco", carlosZones, "Carlos Méndez")
        )
    }

    @Test
    fun zone_insideCoverage_ignoresCase() {
        assertNull(validateZone("zona 10", carlosZones, "Carlos Méndez"))
    }

    // ---------- Fecha ----------

    @Test
    fun date_notChosen_pointsToCalendar() {
        assertEquals("Elige una fecha en el calendario.", validateDate(null, today))
    }

    @Test
    fun date_inThePast_isRejected() {
        assertEquals(
            "Esa fecha ya pasó. Elige una a partir de hoy.",
            validateDate(today.minusDays(1), today)
        )
    }

    @Test
    fun date_todayAndLastAllowedDay_areValid() {
        assertNull(validateDate(today, today))
        assertNull(validateDate(today.plusDays(MAX_DAYS_AHEAD), today))
    }

    @Test
    fun date_tooFarAhead_isRejected() {
        assertEquals(
            "Solo se puede agendar hasta 60 días adelante.",
            validateDate(today.plusDays(MAX_DAYS_AHEAD + 1), today)
        )
    }

    // ---------- Horario ----------

    @Test
    fun timeSlot_requiredUntilChosen() {
        assertTrue(validateTimeSlot(null) != null)
        assertNull(validateTimeSlot(TimeSlot.MORNING))
    }

    // ---------- Formulario completo ----------

    @Test
    fun form_empty_listsOneMessagePerField() {
        val errors = validateRequestForm(
            description = "",
            zone = null,
            date = null,
            timeSlot = null,
            coverageZones = carlosZones,
            providerName = "Carlos Méndez",
            today = today
        )

        assertFalse(errors.isValid)
        assertEquals(4, errors.pendingMessages.size)
    }

    @Test
    fun form_complete_isValid() {
        val errors = validateRequestForm(
            description = "El tomacorriente de la cocina no funciona",
            zone = "Zona 10",
            date = today.plusDays(3),
            timeSlot = TimeSlot.AFTERNOON,
            coverageZones = carlosZones,
            providerName = "Carlos Méndez",
            today = today
        )

        assertTrue(errors.isValid)
        assertTrue(errors.pendingMessages.isEmpty())
    }

    // ---------- Conversión del DatePicker ----------

    @Test
    fun datePickerMillis_roundTripKeepsTheSameDay() {
        val date = LocalDate.of(2026, 10, 20)

        assertEquals(date, utcMillisToLocalDate(localDateToUtcMillis(date)))
    }

    @Test
    fun calendar_onlyAllowsDatesFromTodayOn() {
        assertFalse(isSelectableRequestDate(localDateToUtcMillis(today.minusDays(1)), today))
        assertTrue(isSelectableRequestDate(localDateToUtcMillis(today), today))
    }
}
