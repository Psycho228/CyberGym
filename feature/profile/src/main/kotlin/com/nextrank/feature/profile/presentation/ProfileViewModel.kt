package com.nextrank.feature.profile.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.profile.domain.ProfileData
import com.nextrank.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProfileUiState(
    val isLoading: Boolean = true,
    val nickname: String = "",
    val currentRank: String? = null,
    val primaryGoal: String? = null,
    val dailyMinutes: Int = 0,
    val level: Int = 1,
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.loadProfile()) {
                is Result.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nickname = data.nickname,
                            currentRank = data.currentRank,
                            primaryGoal = data.primaryGoal,
                            dailyMinutes = data.dailyMinutes,
                            level = data.level,
                            totalXp = data.totalXp,
                            currentStreak = data.currentStreak,
                            longestStreak = data.longestStreak,
                        )
                    }
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не удалось загрузить профиль")
                }
            }
        }
    }
}
