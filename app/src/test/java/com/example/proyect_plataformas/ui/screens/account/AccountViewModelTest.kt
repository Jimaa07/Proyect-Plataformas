package com.example.proyect_plataformas.ui.screens.account

import com.example.proyect_plataformas.data.FakeLocalHandsRepository
import com.example.proyect_plataformas.data.FakeSource
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.data.ready
import com.example.proyect_plataformas.ui.state.ContentStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val repository = FakeLocalHandsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.newViewModel(): AccountViewModel {
        val viewModel = AccountViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel
    }

    private fun AccountViewModel.fillValidProfile() {
        updateName("Lucía Pérez")
        updateZone("Zona 12")
        updatePhone("5555-0101")
        updateEmail("lucia@correo.com")
    }

    @Test
    fun savedProfile_isShown_withoutForm() = runTest {
        val state = newViewModel().uiState.value

        assertEquals(MockLocalHandsData.currentUser, state.profile)
        assertNull(state.form)
        assertEquals(ContentStatus.Ready(), state.profileStatus)
        assertEquals(1, state.pendingRatings)
    }

    @Test
    fun withoutProfile_theFormOpensToCreateIt() = runTest {
        repository.profileSource.value = ready(null)

        val state = newViewModel().uiState.value

        assertNull(state.profile)
        assertEquals(ProfileFormState(), state.form)
    }

    @Test
    fun invalidForm_showsErrors_andIsNotSaved() = runTest {
        repository.profileSource.value = ready(null)
        val viewModel = newViewModel()
        viewModel.updateName("Al")

        viewModel.saveProfile()

        val state = viewModel.uiState.value
        assertTrue(state.form?.showErrors == true)
        assertEquals("El nombre debe tener al menos 3 letras.", state.formErrors.name)
        assertTrue(repository.savedProfiles.isEmpty())
    }

    @Test
    fun validForm_savesTrimmedProfile_andClosesTheForm() = runTest {
        repository.profileSource.value = ready(null)
        val viewModel = newViewModel()
        viewModel.fillValidProfile()
        viewModel.updateName("  Lucía Pérez  ")

        viewModel.saveProfile()

        val expected = UserProfile(
            name = "Lucía Pérez",
            zone = "Zona 12",
            phone = "5555-0101",
            email = "lucia@correo.com"
        )
        assertEquals(listOf(expected), repository.savedProfiles)
        assertEquals(expected, viewModel.uiState.value.profile)
        assertNull(viewModel.uiState.value.form)
    }

    @Test
    fun doubleTapWhileSaving_savesOnce() = runTest {
        repository.profileSource.value = ready(null)
        val gate = CompletableDeferred<Unit>()
        repository.writeGate = gate
        val viewModel = newViewModel()
        viewModel.fillValidProfile()

        viewModel.saveProfile()
        viewModel.saveProfile()

        assertTrue(viewModel.uiState.value.form?.isSaving == true)
        assertEquals(1, repository.savedProfiles.size)

        gate.complete(Unit)

        assertNull(viewModel.uiState.value.form)
    }

    @Test
    fun failedSave_keepsTheForm_andShowsAnError() = runTest {
        repository.profileSource.value = ready(null)
        repository.saveProfileError = IOException("Permiso denegado")
        val viewModel = newViewModel()
        viewModel.fillValidProfile()

        viewModel.saveProfile()

        val form = viewModel.uiState.value.form
        assertEquals(AccountViewModel.SAVE_ERROR, form?.saveError)
        assertEquals("Lucía Pérez", form?.name)
    }

    @Test
    fun editing_startsWithTheSavedData_andCancelCloses() = runTest {
        val viewModel = newViewModel()

        viewModel.startEditing()
        assertEquals(MockLocalHandsData.currentUser.name, viewModel.uiState.value.form?.name)

        viewModel.cancelEditing()
        assertNull(viewModel.uiState.value.form)
    }

    @Test
    fun profileFailure_showsError_andRetryRecovers() = runTest {
        repository.profileSource.value = FakeSource.Fails(IOException("Sin respuesta"))
        val viewModel = newViewModel()

        assertTrue(viewModel.uiState.value.profileStatus is ContentStatus.Error)

        repository.profileSource.value = ready(MockLocalHandsData.currentUser)
        viewModel.retry()

        assertEquals(MockLocalHandsData.currentUser, viewModel.uiState.value.profile)
    }
}
