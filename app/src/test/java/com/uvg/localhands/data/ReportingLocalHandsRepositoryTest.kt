package com.uvg.localhands.data

import com.uvg.localhands.data.mock.MockLocalHandsData
import com.uvg.localhands.data.model.NewServiceRequest
import com.uvg.localhands.data.model.TimeSlot
import com.uvg.localhands.data.repository.ErrorReporter
import com.uvg.localhands.data.repository.ReportingLocalHandsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

private class RecordingErrorReporter : ErrorReporter {
    val recorded = mutableListOf<Pair<String, Throwable>>()

    override fun recordNonFatal(operation: String, error: Throwable) {
        recorded += operation to error
    }
}

class ReportingLocalHandsRepositoryTest {

    private val fake = FakeLocalHandsRepository()
    private val reporter = RecordingErrorReporter()
    private val repository = ReportingLocalHandsRepository(fake, reporter)

    private val request = NewServiceRequest(
        providerId = MockLocalHandsData.providers.first().id,
        description = "Fuga en el lavamanos",
        zone = MockLocalHandsData.providers.first().zone,
        date = LocalDate.of(2026, 11, 20),
        timeSlot = TimeSlot.values().first()
    )

    @Test
    fun failedWrite_isRecordedAndStillReachesTheCaller() = runTest {
        val error = IOException("sin conexión")
        fake.createRequestError = error

        try {
            repository.createRequest(request)
            fail("createRequest debía fallar")
        } catch (thrown: IOException) {
            assertSame(error, thrown)
        }

        assertEquals(listOf("createRequest" to error), reporter.recorded)
    }

    @Test
    fun failedListener_isRecordedWithItsOperation() = runTest {
        val error = IllegalStateException("permiso denegado")
        fake.providersSource.value = FakeSource.Fails(error)

        try {
            repository.observeProvidersSnapshot().first()
            fail("observeProvidersSnapshot debía fallar")
        } catch (thrown: IllegalStateException) {
            assertSame(error, thrown)
        }

        assertEquals(listOf("observeProvidersSnapshot" to error), reporter.recorded)
    }

    @Test
    fun successfulOperations_recordNothing() = runTest {
        repository.createRequest(request)
        repository.observeProvidersSnapshot().first()

        assertTrue(reporter.recorded.isEmpty())
    }

    @Test
    fun cancellation_isNotRecorded() = runTest {
        fake.createRequestError = CancellationException("se salió de la pantalla")

        try {
            repository.createRequest(request)
        } catch (expected: CancellationException) {
        }

        assertTrue(reporter.recorded.isEmpty())
    }
}
