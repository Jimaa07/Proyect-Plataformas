package com.example.proyect_plataformas.data.repository

class RequestConfirmationPendingException(
    val requestId: String,
    cause: Throwable
) : Exception(
    "Request $requestId has not been confirmed by the server",
    cause
)