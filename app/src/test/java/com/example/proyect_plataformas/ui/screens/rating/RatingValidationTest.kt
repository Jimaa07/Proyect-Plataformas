package com.example.proyect_plataformas.ui.screens.rating

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingValidationTest {

    @Test
    fun formIsInvalid_whenNoStarsAreSelected() {
        val result = isRatingFormValid(
            stars = 0,
            comment = "Excelente trabajo realizado"
        )

        assertFalse(result)
    }

    @Test
    fun formIsInvalid_whenCommentIsTooShort() {
        val result = isRatingFormValid(
            stars = 5,
            comment = "Muy bien"
        )

        assertFalse(result)
    }

    @Test
    fun formIsInvalid_whenStarsAreGreaterThanFive() {
        val result = isRatingFormValid(
            stars = 6,
            comment = "Excelente trabajo realizado"
        )

        assertFalse(result)
    }

    @Test
    fun formIsInvalid_whenCommentContainsOnlySpaces() {
        val result = isRatingFormValid(
            stars = 4,
            comment = "             "
        )

        assertFalse(result)
    }

    @Test
    fun formIsValid_whenStarsAndCommentAreValid() {
        val result = isRatingFormValid(
            stars = 5,
            comment = "Trabajo rápido y profesional"
        )

        assertTrue(result)
    }

    @Test
    fun formIsValid_whenCommentHasExactlyMinimumLength() {
        val comment = "0123456789"

        val result = isRatingFormValid(
            stars = 3,
            comment = comment
        )

        assertTrue(result)
    }
}