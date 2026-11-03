package com.uvg.localhands.data.repository

class RepositoryTimeoutException(
    message: String,
    cause: Throwable
) : Exception(message, cause)