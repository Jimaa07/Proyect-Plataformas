package com.example.proyect_plataformas.ui.screens.request

import androidx.lifecycle.SavedStateHandle
import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.FakeSource
import com.example.proyect_plataformas.data.model.RequestDraft
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.ready
import com.example.proyect_plataformas.data.repository.RequestConfirmationPendingException
import com.example.proyect_plataformas.navigation.Route
import com.example.proyect_plataformas.ui.state.DetailState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RequestServiceViewModelTest {

    private val today = LocalDate.of(2026, 10, 15)
    private val repository = FakeLocalHandsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.newViewModel(providerId: String = "carlos-mendez"): RequestServiceViewModel {
        val viewModel = RequestServiceViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf(Route.RequestService.ARG_PROVIDER_ID to providerId)),
            clock = { today }
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    private fun RequestServiceViewModel.fillValidRequest() {
        updateDescription("El tomacorriente de la cocina no funciona")
        selectZone("Zona 10")
        selectDate(today.plusDays(2))
        selectTimeSlot(TimeSlot.MORNING)
    }

    @Test
    fun loadsTheProviderFromTheArgument() = runTest {
        val viewModel = newViewModel()

        val providerState = viewModel.uiState.value.providerState as DetailState.Ready
        assertEquals("carlos-mendez", providerState.data.id)
    }

    @Test
    fun unknownProvider_isNotFound_andCannotSubmit() = runTest {
        val viewModel = newViewModel(providerId = "no-existe")
        viewModel.fillValidRequest()

        viewModel.submit()

        assertEquals(DetailState.NotFound, viewModel.uiState.value.providerState)
        assertTrue(repository.createdRequests.isEmpty())
    }

    @Test
    fun incompleteForm_isNotSent() = runTest {
        val viewModel = newViewModel()
        viewModel.updateDescription("Se fue la luz")

        viewModel.submit()

        assertTrue(repository.createdRequests.isEmpty())
        assertFalse(viewModel.uiState.value.form.submitted)
        assertEquals(
            "Agrega más detalle para el profesional (faltan 7 caracteres).",
            viewModel.uiState.value.formErrors.description
        )
    }

    @Test
    fun zoneOutsideCoverage_showsSpecificError() = runTest {
        val viewModel = newViewModel()

        viewModel.selectZone("Mixco")

        assertEquals(
            "Carlos Méndez no trabaja en Mixco. Cubre: Zona 10, Zona 14, Zona 15, Zona 16.",
            viewModel.uiState.value.formErrors.zone
        )
    }

    @Test
    fun validForm_isSentOnceWithTrimmedText() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()
        viewModel.updateDescription("  El tomacorriente de la cocina no funciona  ")

        viewModel.submit()
        viewModel.submit()

        assertEquals(1, repository.createdRequests.size)
        assertEquals("carlos-mendez", repository.createdRequests.single().providerId)
        assertEquals("El tomacorriente de la cocina no funciona", repository.createdRequests.single().description)
        assertTrue(viewModel.uiState.value.form.submitted)
        assertFalse(viewModel.uiState.value.form.isSubmitting)
    }

    @Test
    fun doubleTapWhileSending_sendsOnce_andFormCannotChange() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.submit()
        viewModel.submit()
        viewModel.updateDescription("Otro texto distinto para la solicitud")

        assertTrue(viewModel.uiState.value.form.isSubmitting)
        assertEquals(1, repository.createdRequests.size)
        assertEquals("El tomacorriente de la cocina no funciona", viewModel.uiState.value.form.description)

        gate.complete(Unit)

        assertTrue(viewModel.uiState.value.form.submitted)
        assertFalse(viewModel.uiState.value.form.isSubmitting)
    }

    @Test
    fun failedSubmit_keepsTheForm_andShowsAnError() = runTest {
        repository.createRequestError = IOException("Sin conexión")
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.submit()

        val form = viewModel.uiState.value.form
        assertEquals(RequestServiceViewModel.SUBMIT_ERROR, form.submitError)
        assertFalse(form.submitted)
        assertFalse(form.isSubmitting)
        assertEquals("El tomacorriente de la cocina no funciona", form.description)

        repository.createRequestError = null
        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.submitted)
        assertEquals(null, viewModel.uiState.value.form.submitError)
        assertEquals(2, repository.createdRequests.size)
    }

    @Test
    fun offlineWithoutCache_showsError_notInfiniteLoading() = runTest {
        repository.providersSource.value = ready(emptyList(), isFromCache = true)

        val viewModel = newViewModel()

        assertTrue(viewModel.uiState.value.providerState is DetailState.Error)
    }

    @Test
    fun queuedWithoutConfirmation_countsAsSent() = runTest {
        repository.createRequestError = RequestConfirmationPendingException("req-9", IllegalStateException("Tiempo agotado"))
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.submitted)
        assertEquals(null, viewModel.uiState.value.form.submitError)
    }

    @Test
    fun unavailableProvider_isNotSent() = runTest {
        val viewModel = newViewModel(providerId = "roberto-garcia")
        viewModel.fillValidRequest()

        viewModel.submit()

        assertTrue(repository.createdRequests.isEmpty())
        assertEquals(RequestServiceViewModel.UNAVAILABLE_ERROR, viewModel.uiState.value.form.submitError)
        assertFalse(viewModel.uiState.value.form.submitted)
    }

    @Test
    fun sentRequest_appearsFirstInTheRequests() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.submit()

        val requests = repository.observeRequests().first()
        assertEquals("carlos-mendez", requests.first().providerId)
        assertEquals(RequestStatus.SENT, requests.first().status)
        assertEquals(2, requests.size)
    }

    @Test
    fun savedDraft_isRestoredInAnotherViewModel_withCachedProvider() = runTest {
        val first = newViewModel()
        first.fillValidRequest()
        first.saveDraft()
        repository.providersSource.value = ready(
            (repository.providersSource.value as FakeSource.Ready).snapshot.value,
            isFromCache = true
        )

        val reopened = newViewModel()
        val form = reopened.uiState.value.form

        assertEquals("El tomacorriente de la cocina no funciona", form.description)
        assertEquals("Zona 10", form.zone)
        assertEquals(today.plusDays(2), form.date)
        assertEquals(TimeSlot.MORNING, form.timeSlot)
        assertEquals(RequestServiceViewModel.DRAFT_RESTORED, form.draftMessage)
        assertFalse(form.isLoadingDraft)
        assertTrue(repository.createdRequests.isEmpty())
    }

    @Test
    fun incompleteDraft_isSavedWithoutSending_andOtherProviderStartsEmpty() = runTest {
        val viewModel = newViewModel()
        viewModel.updateDescription("  Falta revisar  ")

        viewModel.saveDraft()

        val draft = repository.drafts.getValue("carlos-mendez")
        assertEquals("  Falta revisar  ", draft.description)
        assertNull(draft.date)
        assertNull(draft.zone)
        assertNull(draft.timeSlot)
        assertTrue(repository.createdRequests.isEmpty())
        assertEquals(RequestServiceViewModel.DRAFT_SAVED, viewModel.uiState.value.form.draftMessage)
        assertEquals("", newViewModel("roberto-garcia").uiState.value.form.description)
    }

    @Test
    fun failedDraftSave_keepsTheFields_andCanBeRetried() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()
        repository.saveDraftError = IOException("Disco no disponible")

        viewModel.saveDraft()

        assertEquals(RequestServiceViewModel.DRAFT_SAVE_ERROR, viewModel.uiState.value.form.draftError)
        assertFalse(viewModel.uiState.value.form.isSavingDraft)
        assertTrue(repository.drafts.isEmpty())
        assertEquals("El tomacorriente de la cocina no funciona", viewModel.uiState.value.form.description)

        repository.saveDraftError = null
        viewModel.saveDraft()

        assertNull(viewModel.uiState.value.form.draftError)
        assertEquals(RequestServiceViewModel.DRAFT_SAVED, viewModel.uiState.value.form.draftMessage)
    }

    @Test
    fun failedDraftLoad_finishesLoading_andDoesNotCrash() = runTest {
        repository.loadDraftError = IOException("No se puede abrir la base")

        val viewModel = newViewModel()

        assertFalse(viewModel.uiState.value.form.isLoadingDraft)
        assertEquals(RequestServiceViewModel.DRAFT_LOAD_ERROR, viewModel.uiState.value.form.draftError)
        viewModel.updateDescription("Todavía puedo completar el formulario")
        assertEquals("Todavía puedo completar el formulario", viewModel.uiState.value.form.description)
    }

    @Test
    fun slowDraftLoad_cannotBeOverwrittenByEditingOrSaving() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.loadDraftGate = gate
        repository.drafts["carlos-mendez"] = RequestDraft("carlos-mendez", description = "Texto guardado")
        val viewModel = newViewModel()

        viewModel.updateDescription("Texto antes de terminar la lectura")
        viewModel.saveDraft()
        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.isLoadingDraft)
        assertEquals("", viewModel.uiState.value.form.description)
        assertTrue(repository.createdRequests.isEmpty())
        gate.complete(Unit)
        assertEquals("Texto guardado", viewModel.uiState.value.form.description)
    }

    @Test
    fun draftSave_isNotConfirmedBeforeTheWriteFinishes_andBlocksSubmit() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.saveDraftGate = gate
        val viewModel = newViewModel()
        viewModel.fillValidRequest()

        viewModel.saveDraft()
        viewModel.submit()
        viewModel.updateDescription("Cambio durante el guardado")

        assertTrue(viewModel.uiState.value.form.isSavingDraft)
        assertNull(viewModel.uiState.value.form.draftMessage)
        assertTrue(repository.drafts.isEmpty())
        assertTrue(repository.createdRequests.isEmpty())
        gate.complete(Unit)
        assertFalse(viewModel.uiState.value.form.isSavingDraft)
        assertEquals(RequestServiceViewModel.DRAFT_SAVED, viewModel.uiState.value.form.draftMessage)
        assertEquals("El tomacorriente de la cocina no funciona", repository.drafts.getValue("carlos-mendez").description)
    }

    @Test
    fun failedSubmit_keepsTheSavedDraft_butSuccessfulRetryDeletesIt() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()
        viewModel.saveDraft()
        repository.createRequestError = IOException("Sin conexión")

        viewModel.submit()
        assertTrue(repository.drafts.containsKey("carlos-mendez"))

        repository.createRequestError = null
        viewModel.submit()
        assertFalse(repository.drafts.containsKey("carlos-mendez"))
        assertTrue(viewModel.uiState.value.form.submitted)
    }

    @Test
    fun queuedRequest_deletesTheDraftSoItIsNotSubmittedTwice() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()
        viewModel.saveDraft()
        repository.createRequestError = RequestConfirmationPendingException("req-9", IOException())

        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.submitted)
        assertFalse(repository.drafts.containsKey("carlos-mendez"))
    }

    @Test
    fun failedDraftCleanup_stillCountsAsSent_andShowsAWarning() = runTest {
        val viewModel = newViewModel()
        viewModel.fillValidRequest()
        viewModel.saveDraft()
        repository.deleteDraftError = IOException("No se puede eliminar")

        viewModel.submit()
        viewModel.submit()

        assertTrue(viewModel.uiState.value.form.submitted)
        assertNull(viewModel.uiState.value.form.submitError)
        assertEquals(RequestServiceViewModel.DRAFT_DELETE_ERROR, viewModel.uiState.value.form.draftError)
        assertEquals(1, repository.createdRequests.size)
    }

    @Test
    fun savedDateThatHasPassed_isRestoredButCannotBeSubmitted() = runTest {
        repository.drafts["carlos-mendez"] = RequestDraft(
            providerId = "carlos-mendez",
            description = "El tomacorriente de la cocina no funciona",
            zone = "Zona 10",
            date = today.minusDays(1),
            timeSlot = TimeSlot.MORNING
        )
        val viewModel = newViewModel()

        viewModel.submit()

        assertEquals(today.minusDays(1), viewModel.uiState.value.form.date)
        assertEquals("Esa fecha ya pasó. Elige una a partir de hoy.", viewModel.uiState.value.formErrors.date)
        assertTrue(repository.createdRequests.isEmpty())
    }
}
