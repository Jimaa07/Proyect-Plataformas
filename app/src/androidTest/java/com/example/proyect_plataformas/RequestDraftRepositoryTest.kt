package com.example.proyect_plataformas

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.repository.FirestoreLocalHandsRepository
import com.example.proyect_plataformas.data.repository.LocalHandsDatabase
import com.example.proyect_plataformas.data.repository.LocalHandsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Usa un archivo real de Room y solo métodos locales del Repository. No necesita red. */
@RunWith(AndroidJUnit4::class)
class RequestDraftRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "request-drafts-test.db"
    private lateinit var database: LocalHandsDatabase
    private lateinit var repository: LocalHandsRepository

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
        openDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    private fun openDatabase() {
        database = Room.databaseBuilder(
            context,
            LocalHandsDatabase::class.java,
            databaseName
        ).build()
        repository = FirestoreLocalHandsRepository(
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance(),
            draftDao = database.requestDraftDao()
        )
    }

    @Test
    fun draftPersistsAfterDatabaseAndRepositoryAreRecreated() = runBlocking {
        val draft = RequestDraft(
            providerId = "provider-001",
            description = "  Revisar conexión eléctrica de la cocina.  ",
            zone = "Zona 10",
            date = LocalDate.of(2026, 11, 15),
            timeSlot = TimeSlot.AFTERNOON
        )
        repository.saveRequestDraft(draft)

        database.close()
        openDatabase()

        assertEquals(draft, repository.getRequestDraft(draft.providerId))
    }

    @Test
    fun incompleteDraftPreservesNullFields() = runBlocking {
        val draft = RequestDraft(providerId = "provider-001", description = "Fuga")

        repository.saveRequestDraft(draft)

        assertEquals(draft, repository.getRequestDraft(draft.providerId))
    }

    @Test
    fun savingAgainReplacesOnlyThatProvidersDraft() = runBlocking {
        val first = RequestDraft("provider-001", description = "Primera versión")
        val other = RequestDraft("provider-002", description = "Otro prestador")
        repository.saveRequestDraft(first)
        repository.saveRequestDraft(other)
        val edited = first.copy(description = "Segunda versión", timeSlot = TimeSlot.MORNING)

        repository.saveRequestDraft(edited)

        assertEquals(edited, repository.getRequestDraft(first.providerId))
        assertEquals(other, repository.getRequestDraft(other.providerId))
    }

    @Test
    fun deletingOneDraftPreservesTheOther() = runBlocking {
        val other = RequestDraft("provider-002", description = "No se elimina")
        repository.saveRequestDraft(RequestDraft("provider-001"))
        repository.saveRequestDraft(other)

        repository.deleteRequestDraft("provider-001")
        repository.deleteRequestDraft("provider-001")

        assertNull(repository.getRequestDraft("provider-001"))
        assertEquals(other, repository.getRequestDraft(other.providerId))
    }

    @Test
    fun missingDraftReturnsNull() = runBlocking {
        assertNull(repository.getRequestDraft("no-existe"))
    }
}
