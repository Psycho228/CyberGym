package com.nextrank.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.analytics.Analytics
import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.onFailure
import com.nextrank.core.common.result.onSuccess
import com.nextrank.feature.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel авторизации.
 * Управляет состоянием UI и делегирует бизнес-логику в AuthRepository.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val analytics: Analytics,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun onLogin() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Заполните все поля") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess {
                    analytics.track(
                        com.nextrank.core.analytics.AnalyticsEvent.SimpleEvent("login_completed")
                    )
                    analytics.setUserId(it)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapError(error),
                        )
                    }
                }
        }
    }

    fun onRegister() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Заполните все поля") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Пароль должен быть не менее 6 символов") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.register(email, password)
                .onSuccess {
                    analytics.track(
                        com.nextrank.core.analytics.AnalyticsEvent.SimpleEvent("sign_up_completed")
                    )
                    analytics.setUserId(it)
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = mapError(error),
                        )
                    }
                }
        }
    }

    fun onLogout() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.logout()
            analytics.setUserId(null)
            _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
        }
    }

    private fun mapError(error: AppError): String = when (error) {
        is AppError.Auth -> error.detail ?: "Ошибка авторизации"
        is AppError.Network -> "Ошибка сети. Проверьте подключение"
        is AppError.Validation -> error.detail
        is AppError.Unknown -> error.message
        else -> "Произошла ошибка"
    }
}
