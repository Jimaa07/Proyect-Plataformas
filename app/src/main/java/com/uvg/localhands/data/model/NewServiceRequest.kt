package com.uvg.localhands.data.model

import java.time.LocalDate

data class NewServiceRequest(
    val providerId: String,
    val description: String,
    val zone: String,
    val date: LocalDate,
    val timeSlot: TimeSlot
)