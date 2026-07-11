package com.nextrank.feature.profile.domain

import com.nextrank.core.common.result.Result

data class ProfileData(
    val nickname: String,
    val currentRank: String?,
    val primaryGoal: String?,
    val dailyMinutes: Int,
    val level: Int,
    val totalXp: Long,
    val currentStreak: Int,
    val longestStreak: Int,
)

interface ProfileRepository {
    suspend fun loadProfile(): Result<ProfileData>
}
