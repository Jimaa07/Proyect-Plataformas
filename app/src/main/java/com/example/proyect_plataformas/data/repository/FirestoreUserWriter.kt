package com.example.proyect_plataformas.data.repository

import com.example.proyect_plataformas.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val PROFILE_SAVE_TIMEOUT_MILLIS = 8_000L

internal class FirestoreUserWriter(
    private val firestore: FirebaseFirestore
) {
    /**
     * Guarda el perfil en users/{uid}. Sin conexión, Firestore deja el cambio
     * en cola y la espera no termina: después del tiempo límite se devuelve
     * igual, porque el perfil ya quedó en el teléfono y se enviará solo.
     * Un rechazo de las reglas sí se lanza como excepción.
     */
    suspend fun saveUser(
        userId: String,
        profile: UserProfile
    ) {
        require(userId.isNotBlank()) {
            "User ID cannot be blank"
        }
        require(profile.name.isNotBlank()) {
            "Name cannot be blank"
        }

        withTimeoutOrNull(PROFILE_SAVE_TIMEOUT_MILLIS) {
            firestore.collection("users")
                .document(userId)
                .set(profile.toDocument())
                .await()
        }
    }
}
