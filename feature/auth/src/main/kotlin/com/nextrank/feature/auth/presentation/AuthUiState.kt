package com.nextrank.feature.auth.presentation

import androidx.compose.runtime.Immutable
import com.nextrank.core.common.result.Result

@Immutable
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    object Login : AuthEvent
    object Register : AuthEvent
    object Logout : AuthEvent
}

fun <T> Result<T>.toAuthUiState(errorMessage: String? = null): AuthUiState = when (this) {
    is com.nextrank.core.common.result.Result.Success -> AuthUiState(isLoggedIn = true)
    is com.nextrank.core.common.result.Result.Failure -> AuthUiState(errorMessage = errorMessage ?: "Произошла ошибка")
}
