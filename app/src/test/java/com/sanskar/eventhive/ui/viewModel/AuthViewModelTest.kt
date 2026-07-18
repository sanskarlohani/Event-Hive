package com.sanskar.eventhive.ui.viewModel

import app.cash.turbine.test
import com.sanskar.eventhive.MainDispatcherRule
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.repository.Inteface.AuthRepository
import com.sanskar.eventhive.presentation.permission.PermissionHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = kotlinx.coroutines.test.StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var authRepository: AuthRepository
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        authRepository = mockk(relaxed = true)
        permissionHelper = mockk(relaxed = true)
        viewModel = AuthViewModel(authRepository, permissionHelper)
    }

    @Test
    fun `login success updates state and refreshes permissions`() = runTest(testDispatcher) {
        // Arrange
        val email = "test@example.com"
        val password = "password"
        val uid = "user123"
        coEvery { authRepository.login(email, password) } returns Resource.Success(Unit)
        coEvery { authRepository.getCurrentUser()?.uid } returns uid

        // Act & Assert
        viewModel.loginState.test {
            assertEquals(Resource.Idle, awaitItem())
            viewModel.login(email, password)
            
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(Unit), awaitItem())
        }

        coVerify(exactly = 1) { permissionHelper.refresh(uid) }
    }

    @Test
    fun `login failure updates state and does not refresh permissions`() = runTest(testDispatcher) {
        // Arrange
        val email = "test@example.com"
        val password = "password"
        val error = Exception("Login failed")
        coEvery { authRepository.login(email, password) } returns Resource.Error(error)

        // Act & Assert
        viewModel.loginState.test {
            assertEquals(Resource.Idle, awaitItem())
            viewModel.login(email, password)
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Error(error), awaitItem())
        }

        coVerify(exactly = 0) { permissionHelper.refresh(any()) }
    }

    @Test
    fun `signOut success updates state and refreshes permissions with empty uid`() = runTest(testDispatcher) {
        // Arrange
        coEvery { authRepository.signOut() } returns Resource.Success(Unit)

        // Act & Assert
        viewModel.signoutState.test {
            assertEquals(Resource.Idle, awaitItem())
            viewModel.signOut()
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(Unit), awaitItem())
        }

        coVerify(exactly = 1) { permissionHelper.refresh("") }
    }
}
