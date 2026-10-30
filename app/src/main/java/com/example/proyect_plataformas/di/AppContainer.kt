package com.example.proyect_plataformas.di

import android.content.Context
import androidx.room.Room
import com.example.proyect_plataformas.data.repository.FirestoreLocalHandsRepository
import com.example.proyect_plataformas.data.repository.LocalHandsDatabase
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    // El contenedor de Application conserva una sola instancia durante el proceso.
    private val database: LocalHandsDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            LocalHandsDatabase::class.java,
            "localhands.db"
        ).build()
    }

    val repository: LocalHandsRepository by lazy {
        FirestoreLocalHandsRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            draftDao = database.requestDraftDao()
        )
    }
}
