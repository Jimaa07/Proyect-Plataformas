package com.example.proyect_plataformas.data.repository

class RepositoryTimeoutException(
    message: String,
    cause: Throwable
) : Exception(message, cause)