package com.example.proyect_plataformas.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private const val SESSION_TIMEOUT_MILLIS = 15_000L

internal class FirebaseSession(
    private val auth: FirebaseAuth
) {
    private val sessionMutex = Mutex()

    suspend fun requireUserId(): String {
        return withTimeout(SESSION_TIMEOUT_MILLIS) {
            sessionMutex.withLock {
                auth.currentUser?.uid
                    ?: requireNotNull(
                        auth.signInAnonymously().await().user?.uid
                    ) {
                        "Anonymous authentication returned no user"
                    }
            }
        }
    }
}