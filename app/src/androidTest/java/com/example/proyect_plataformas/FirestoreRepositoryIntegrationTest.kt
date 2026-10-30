package com.example.proyect_plataformas

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyect_plataformas.data.model.NewServiceRequest
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import com.example.proyect_plataformas.data.repository.RepositoryTimeoutException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TEST_STEP_TIMEOUT_MILLIS = 30_000L

@RunWith(AndroidJUnit4::class)
class FirestoreRepositoryIntegrationTest {

    @Test
    fun createsRequestForExistingProvider() = runBlocking {
        val repository = getRepository()

        val provider = waitForStep(
            description = "Consultar el prestador provider-001"
        ) {
            requireNotNull(
                repository.observeProvider("provider-001")
                    .first { it != null }
            )
        }

        assertEquals("provider-001", provider.id)
        assertTrue(provider.isAvailable)

        val zone = provider.coverageZones.firstOrNull()
            ?: provider.zone

        val description =
            "Solicitud de prueba: reparar una fuga en la cocina."

        val requestId = repository.createRequest(
            NewServiceRequest(
                providerId = provider.id,
                description = description,
                zone = zone,
                date = LocalDate.now().plusDays(1),
                timeSlot = TimeSlot.MORNING
            )
        )

        assertTrue(requestId.isNotBlank())

        val savedRequest = waitForStep(
            description = "Encontrar la solicitud creada: $requestId"
        ) {
            repository.observeRequests()
                .first { requests ->
                    requests.any { it.id == requestId }
                }
                .first { it.id == requestId }
        }

        assertEquals(provider.id, savedRequest.providerId)
        assertEquals(description, savedRequest.description)
        assertEquals(zone, savedRequest.zone)
        assertEquals(RequestStatus.SENT, savedRequest.status)
    }

    @Test
    fun ratesCompletedRequestAndUpdatesProvider() = runBlocking {
        val repository = getRepository()

        val provider = waitForStep(
            description = "Consultar el prestador"
        ) {
            requireNotNull(
                repository.observeProvider("provider-001")
                    .first { it != null }
            )
        }

        val zone = provider.coverageZones.firstOrNull()
            ?: provider.zone

        val requestId = repository.createRequest(
            NewServiceRequest(
                providerId = provider.id,
                description =
                    "Prueba integrada de calificación: reparar fuga.",
                zone = zone,
                date = LocalDate.now().plusDays(1),
                timeSlot = TimeSlot.MORNING
            )
        )

        // Durante esta espera, marcar esta solicitud como TO_RATE
        // desde Firebase Console, sin volver a ejecutar el test.
        withTimeout(180_000L) {
            repository.observeRequests()
                .first { requests ->
                    requests.any {
                        it.id == requestId &&
                                it.status == RequestStatus.TO_RATE
                    }
                }
        }

        val comment = "Servicio de prueba verificado: $requestId"

        repository.submitRating(
            requestId = requestId,
            stars = 5,
            comment = comment,
            reviewerName = "Usuario de prueba"
        )

        val ratedRequest = waitForStep(
            description = "Observar la solicitud en RATED"
        ) {
            repository.observeRequests()
                .first { requests ->
                    requests.any {
                        it.id == requestId &&
                                it.status == RequestStatus.RATED
                    }
                }
                .first { it.id == requestId }
        }

        assertEquals(5, ratedRequest.rating)
        assertEquals(comment, ratedRequest.reviewComment)

        val updatedProvider = waitForStep(
            description = "Observar la reseña en el prestador"
        ) {
            requireNotNull(
                repository.observeProvider(provider.id)
                    .first { currentProvider ->
                        currentProvider?.reviews?.any {
                            it.comment == comment && it.rating == 5
                        } == true
                    }
            )
        }

        assertTrue(
            updatedProvider.reviews.any {
                it.comment == comment && it.rating == 5
            }
        )
    }

    private fun getRepository(): LocalHandsRepository {
        val application = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .applicationContext as LocalHandsApplication

        return application.appContainer.repository
    }

    private suspend fun <T> waitForStep(
        description: String,
        diagnostics: () -> String = { "" },
        action: suspend () -> T
    ): T {
        return try {
            withTimeout(TEST_STEP_TIMEOUT_MILLIS) {
                action()
            }
        } catch (exception: TimeoutCancellationException) {
            val userId = FirebaseAuth.getInstance()
                .currentUser
                ?.uid

            throw AssertionError(
                "Se agotó la espera en: $description.\n" +
                        "UID actual: $userId\n" +
                        diagnostics(),
                exception
            )
        }
    }

    @Test
    fun readsCachedDataAndRejectsRequestWhileOffline() = runBlocking {
        val repository = getRepository()
        val firestore = FirebaseFirestore.getInstance()

        // Primero carga los datos desde el servidor y establece la sesión.
        val onlineProviders = waitForStep(
            description = "Cargar prestadores desde el servidor"
        ) {
            repository.observeProvidersSnapshot()
                .first { snapshot ->
                    !snapshot.isFromCache &&
                            snapshot.value.any {
                                it.id == "provider-001"
                            }
                }
        }

        val provider = onlineProviders.value.first {
            it.id == "provider-001"
        }

        assertTrue(provider.isAvailable)

        val onlineRequests = waitForStep(
            description = "Cargar solicitudes desde el servidor"
        ) {
            repository.observeRequestsSnapshot()
                .first { !it.isFromCache }
        }

        try {
            waitForStep(
                description = "Desactivar la conexión de Firestore"
            ) {
                firestore.disableNetwork().await()
            }

            val cachedProviders = waitForStep(
                description = "Leer prestadores desde caché"
            ) {
                repository.observeProvidersSnapshot()
                    .first { snapshot ->
                        snapshot.isFromCache &&
                                snapshot.value.any {
                                    it.id == provider.id
                                }
                    }
            }

            assertTrue(cachedProviders.isFromCache)

            val cachedRequests = waitForStep(
                description = "Leer solicitudes desde caché"
            ) {
                repository.observeRequestsSnapshot()
                    .first { it.isFromCache }
            }

            assertEquals(
                onlineRequests.value.map { it.id }.sorted(),
                cachedRequests.value.map { it.id }.sorted()
            )

            val zone = provider.coverageZones.firstOrNull()
                ?: provider.zone

            val writeFailure = try {
                withTimeout(20_000L) {
                    repository.createRequest(
                        NewServiceRequest(
                            providerId = provider.id,
                            description =
                                "Prueba sin conexión: reparar fuga de agua.",
                            zone = zone,
                            date = LocalDate.now().plusDays(1),
                            timeSlot = TimeSlot.MORNING
                        )
                    )
                }

                null
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                exception
            }

            val isExpectedFailure =
                writeFailure is RepositoryTimeoutException ||
                        (
                                writeFailure is FirebaseFirestoreException &&
                                        writeFailure.code ==
                                        FirebaseFirestoreException.Code.UNAVAILABLE
                                )

            assertTrue(
                "Se esperaba un error de conexión o tiempo de espera. " +
                        "Resultado: $writeFailure",
                isExpectedFailure
            )
        } finally {
            // Restaura la conexión incluso si alguna comprobación falla.
            withContext(NonCancellable) {
                withTimeout(15_000L) {
                    firestore.enableNetwork().await()
                }
            }
        }
    }
}