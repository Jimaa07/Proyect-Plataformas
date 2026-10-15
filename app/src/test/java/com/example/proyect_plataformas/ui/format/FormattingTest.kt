package com.example.proyect_plataformas.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormattingTest {

    @Test
    fun longDate_isInSpanish() {
        assertEquals("3 de octubre de 2026", formatLongDate(LocalDate.of(2026, 10, 3)))
    }

    @Test
    fun dayAndDate_startsWithCapitalDay() {
        assertEquals("Sábado 3 de octubre", formatDayAndDate(LocalDate.of(2026, 10, 3)))
    }

    @Test
    fun initials_takeFirstTwoWords() {
        assertEquals("CM", initialsOf("Carlos Méndez"))
        assertEquals("A", initialsOf("  Ana  "))
    }

    @Test
    fun stars_areClampedBetweenZeroAndFive() {
        assertEquals("★★★★☆", starsText(4))
        assertEquals("★★★★★", starsText(9))
        assertEquals("☆☆☆☆☆", starsText(-1))
    }
}
