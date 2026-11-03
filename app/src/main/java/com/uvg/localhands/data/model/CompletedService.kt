package com.uvg.localhands.data.model

data class CompletedService(
    val id: String,
    val providerId: String,
    val providerName: String,
    val serviceDescription: String,
    val completedDate: String
)