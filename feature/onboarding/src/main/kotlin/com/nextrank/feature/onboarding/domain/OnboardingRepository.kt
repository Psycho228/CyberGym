package com.nextrank.feature.onboarding.domain

import com.nextrank.core.common.result.Result
import com.nextrank.feature.onboarding.presentation.OnboardingUiState

interface OnboardingRepository {
    suspend fun saveProfile(state: OnboardingUiState): Result<Unit>
}
