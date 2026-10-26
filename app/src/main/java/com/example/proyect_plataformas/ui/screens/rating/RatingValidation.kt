package com.example.proyect_plataformas.ui.screens.rating

const val MINIMUM_REVIEW_LENGTH = 10

fun isRatingFormValid(
    stars: Int,
    comment: String
): Boolean {
    val hasValidStars = stars in 1..5
    val hasValidComment =
        comment.trim().length >= MINIMUM_REVIEW_LENGTH

    return hasValidStars && hasValidComment
}