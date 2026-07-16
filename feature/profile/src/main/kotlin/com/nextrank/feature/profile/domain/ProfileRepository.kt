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
    val faceit: FaceitProfileStats? = null,
    val favoriteMaps: List<String> = emptyList(),
    val weakSpots: List<String> = emptyList(),
    val trainingFrequencyDays: Int? = null,
)

data class FaceitProfileStats(
    val playerId: String?,
    val nickname: String?,
    val avatar: String?,
    val country: String?,
    val faceitUrl: String?,
    val skillLevel: Int?,
    val faceitElo: Int?,
    val gamePlayerId: String?,
    val matches: Int? = null,
    val winRate: String? = null,
    val averageKd: String? = null,
    val headshots: String? = null,
)

interface ProfileRepository {
    suspend fun loadProfile(): Result<ProfileData>
    suspend fun saveFaceitStats(stats: FaceitProfileStats): Result<Unit>
}

interface FaceitStatsRepository {
    suspend fun loadStats(playerId: String): Result<FaceitProfileStats>
}
