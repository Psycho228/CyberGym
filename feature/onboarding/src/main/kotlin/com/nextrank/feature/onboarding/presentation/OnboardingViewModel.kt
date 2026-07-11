package com.nextrank.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.domain.model.Cs2Rank
import com.nextrank.domain.model.PlayerGoal
import com.nextrank.feature.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val nickname: String get() = _uiState.value.nickname
    val selectedRank: Cs2Rank? get() = _uiState.value.selectedRank
    val selectedGoal: PlayerGoal? get() = _uiState.value.selectedGoal

    fun onNicknameChange(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun onRankSelect(rank: Cs2Rank?) {
        _uiState.update { it.copy(selectedRank = rank) }
    }

    fun onGoalSelect(goal: PlayerGoal?) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun onComplete() {
        val state = _uiState.value
        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = onboardingRepository.saveProfile(
                nickname = state.nickname,
                rank = state.selectedRank,
                goal = state.selectedGoal,
                dailyMinutes = state.dailyMinutes,
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, isComplete = true)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.error.message ?: "Не удалось сохранить профиль",
                    )
                }
            }
        }
    }
}
