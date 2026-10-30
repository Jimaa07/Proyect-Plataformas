package com.example.proyect_plataformas.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase

// Primera versión, sin migraciones todavía. No se elimina la base al abrirla.
@Database(entities = [RequestDraftEntity::class], version = 1, exportSchema = false)
abstract class LocalHandsDatabase : RoomDatabase() {

    abstract fun requestDraftDao(): RequestDraftDao
}
