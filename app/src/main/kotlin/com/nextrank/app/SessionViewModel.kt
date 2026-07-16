package com.nextrank.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SessionViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionUiState())
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    fun logout(onSuccess: () -> Unit) {
        if (_state.value.isLoggingOut) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoggingOut = true,
                    logoutError = null,
                )
            }

            when (authRepository.logout()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoggingOut = false,
                            logoutError = null,
                        )
                    }
                    onSuccess()
                }
                is Result.Failure -> {
                    _state.update {
                        it.copy(
                            isLoggingOut = false,
                            logoutError = "Не удалось выйти из аккаунта. Попробуйте ещё раз.",
                        )
                    }
                }
            }
        }
    }

    fun consumeLogoutError() {
        _state.update { it.copy(logoutError = null) }
    }
}

internal data class SessionUiState(
    val isLoggingOut: Boolean = false,
    val logoutError: String? = null,
)
