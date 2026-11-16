package com.uvg.localhands.di

import android.content.Context
import androidx.room.Room
import com.uvg.localhands.data.repository.CrashlyticsErrorReporter
import com.uvg.localhands.data.repository.FirestoreLocalHandsRepository
import com.uvg.localhands.data.repository.LocalHandsDatabase
import com.uvg.localhands.data.repository.LocalHandsRepository
import com.uvg.localhands.data.repository.ReportingLocalHandsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

    // Cada error del Repository se registra en Crashlytics como no fatal.
    val repository: LocalHandsRepository by lazy {
        ReportingLocalHandsRepository(
            delegate = FirestoreLocalHandsRepository(
                firestore = FirebaseFirestore.getInstance(),
                auth = FirebaseAuth.getInstance(),
                draftDao = database.requestDraftDao()
            ),
            reporter = CrashlyticsErrorReporter(FirebaseCrashlytics.getInstance())
        )
    }
}
