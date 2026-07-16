package com.nextrank.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.auth.domain.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class StartupViewModel(
    private val authRepository: AuthRepository,
    private val supabaseClient: SupabaseClient,
) : ViewModel() {

    private val _state = MutableStateFlow<StartupState>(StartupState.Checking)
    val state: StateFlow<StartupState> = _state.asStateFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            _state.value = when (val sessionResult = authRepository.isSessionActive()) {
                is Result.Failure -> StartupState.Unauthenticated
                is Result.Success -> {
                    if (!sessionResult.data) {
                        StartupState.Unauthenticated
                    } else {
                        resolveAuthenticatedDestination()
                    }
                }
            }
        }
    }

    private suspend fun resolveAuthenticatedDestination(): StartupState {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: return StartupState.Unauthenticated

        val profile = runCatching {
            supabaseClient.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingle<StartupProfileDto>()
        }.getOrNull()

        return if (profile?.onboardingCompleted == true) {
            StartupState.Ready(NavTarget.Home)
        } else {
            StartupState.Ready(NavTarget.Onboarding)
        }
    }
}

internal sealed interface StartupState {
    data object Checking : StartupState
    data object Unauthenticated : StartupState
    data class Ready(val target: NavTarget) : StartupState
}

internal enum class NavTarget {
    Home,
    Onboarding,
}

@Serializable
private data class StartupProfileDto(
    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean = false,
)
