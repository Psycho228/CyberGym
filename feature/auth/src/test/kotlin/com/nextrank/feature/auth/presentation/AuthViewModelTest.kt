package com.nextrank.feature.auth.presentation

import app.cash.turbine.test
import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.Result
import com.nextrank.core.analytics.Analytics
import com.nextrank.core.analytics.AnalyticsNoOp
import com.nextrank.feature.auth.domain.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthViewModelTest {

    private val repository: AuthRepository = mockk()
    private val analytics: Analytics = AnalyticsNoOp()
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onLogin with blank fields shows validation error`() = runTest(dispatcher) {
        val viewModel = AuthViewModel(repository, analytics)
        viewModel.onLogin()

        assertEquals("Заполните все поля", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onRegister with short password shows validation error`() = runTest(dispatcher) {
        val viewModel = AuthViewModel(repository, analytics)
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("123")
        viewModel.onRegister()

        assertEquals("Пароль должен быть не менее 6 символов", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onLogin success sets isLoggedIn`() = runTest(dispatcher) {
        coEvery { repository.login("test@test.com", "password123") } returns Result.Success("user-1")

        val viewModel = AuthViewModel(repository, analytics)
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLogin()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onLogin failure shows error`() = runTest(dispatcher) {
        coEvery { repository.login(any(), any()) } returns Result.Failure(AppError.Auth(null, "Invalid credentials"))

        val viewModel = AuthViewModel(repository, analytics)
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password123")
        viewModel.onLogin()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Invalid credentials", viewModel.uiState.value.errorMessage)
    }
}