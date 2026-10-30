package com.example.proyect_plataformas.data.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un borrador por prestador. Room guarda la fecha ISO y el nombre del horario. */
@Entity(tableName = "request_drafts")
data class RequestDraftEntity(
    @PrimaryKey val providerId: String,
    val description: String,
    val zone: String?,
    val date: String?,
    val timeSlot: String?
)
