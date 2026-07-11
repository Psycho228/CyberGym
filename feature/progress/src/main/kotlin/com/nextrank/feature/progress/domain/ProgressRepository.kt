package com.nextrank.feature.progress.domain

import com.nextrank.core.common.result.Result

data class ProgressStats(
    val level: Int,
    val totalXp: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalTrainings: Int,
)

data class AchievementInfo(
    val slug: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isUnlocked: Boolean,
)

data class SessionHistoryItem(
    val sessionId: String,
    val awardedXp: Int,
    val completedAt: String?,
)

data class ProgressData(
    val stats: ProgressStats,
    val achievements: List<AchievementInfo>,
    val recentSessions: List<SessionHistoryItem>,
)

interface ProgressRepository {
    suspend fun loadProgress(): Result<ProgressData>
}
