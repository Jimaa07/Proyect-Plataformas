package com.example.proyect_plataformas.data.repository

class RatingConfirmationPendingException(
    val requestId: String,
    cause: Throwable
) : Exception(
    "Rating for request $requestId has not been confirmed",
    cause
)