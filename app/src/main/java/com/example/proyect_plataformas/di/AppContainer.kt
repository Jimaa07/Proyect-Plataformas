package com.example.proyect_plataformas.di

import com.example.proyect_plataformas.data.repository.FirestoreLocalHandsRepository
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class AppContainer {

    val repository: LocalHandsRepository by lazy {
        FirestoreLocalHandsRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance()
        )
    }
}