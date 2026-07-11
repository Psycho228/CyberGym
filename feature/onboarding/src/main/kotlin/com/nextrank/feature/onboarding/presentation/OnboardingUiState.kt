package com.nextrank.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import com.nextrank.domain.model.Cs2Rank
import com.nextrank.domain.model.PlayerGoal

@Immutable
data class OnboardingUiState(
    val nickname: String = "",
    val selectedRank: Cs2Rank? = null,
    val selectedGoals: Set<PlayerGoal> = emptySet(),
    val dailyMinutes: Int = 15,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false,
)
