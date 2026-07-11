package com.nextrank.feature.onboarding.domain

import com.nextrank.core.common.result.Result
import com.nextrank.domain.model.Cs2Rank
import com.nextrank.domain.model.PlayerGoal

interface OnboardingRepository {
    suspend fun saveProfile(
        nickname: String,
        rank: Cs2Rank?,
        goal: PlayerGoal?,
        dailyMinutes: Int,
    ): Result<Unit>
}
