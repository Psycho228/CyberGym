package com.nextrank.feature.progress.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.progress.domain.AchievementInfo
import com.nextrank.feature.progress.domain.ProgressData
import com.nextrank.feature.progress.domain.ProgressRepository
import com.nextrank.feature.progress.domain.ProgressStats
import com.nextrank.feature.progress.domain.SessionHistoryItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseProgressRepository(
    private val supabaseClient: SupabaseClient,
) : ProgressRepository {

    override suspend fun loadProgress(): Result<ProgressData> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Пользователь не авторизован")

        val profile = loadProfile(userId)
        val achievements = loadAchievements()
        val unlockedIds = loadUnlockedAchievementIds(userId)
        val totals = loadProgressTotals()
        val sessions = loadCompletedSessions("training_sessions", userId)
        val practiceSessions = loadCompletedSessions("practice_sessions", userId)
        val recentSessions = (sessions + practiceSessions)
            .sortedByDescending { it.completedAt.orEmpty() }
            .take(10)

        ProgressData(
            stats = ProgressStats(
                level = profile.level,
                totalXp = profile.totalXp,
                currentStreak = profile.currentStreak,
                longestStreak = profile.longestStreak,
                totalTrainings = totals.totalTrainings,
            ),
            achievements = achievements.map {
                AchievementInfo(
                    slug = it.slug,
                    title = it.title,
                    description = it.description,
                    xpReward = it.xpReward,
                    isUnlocked = it.id in unlockedIds,
                )
            },
            recentSessions = recentSessions.map {
                SessionHistoryItem(
                    sessionId = it.id,
                    awardedXp = it.awardedXp,
                    completedAt = it.completedAt,
                )
            },
        )
    }.fold({ Result.Success(it) }, { Result.Failure(it.toAppError()) })

    private suspend fun loadProfile(userId: String): ProfileStatsDto =
        supabaseClient.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle()

    private suspend fun loadAchievements(): List<AchievementDto> =
        supabaseClient.from("achievements")
            .select { filter { eq("is_active", true) } }
            .decodeList()

    private suspend fun loadUnlockedAchievementIds(userId: String): Set<String> =
        supabaseClient.from("user_achievements")
            .select { filter { eq("user_id", userId) } }
            .decodeList<UserAchievementDto>()
            .map { it.achievementId }
            .toSet()

    private suspend fun loadProgressTotals(): ProgressTotalsDto =
        supabaseClient.postgrest
            .rpc("get_progress_totals")
            .decodeSingle()

    private suspend fun loadCompletedSessions(
        table: String,
        userId: String,
    ): List<SessionDto> =
        supabaseClient.from(table)
            .select {
                filter {
                    eq("user_id", userId)
                    eq("status", "completed")
                }
                order("completed_at", Order.DESCENDING)
                limit(10)
            }
            .decodeList()
}

@Serializable
private data class ProfileStatsDto(
    val level: Int,
    @SerialName("total_xp") val totalXp: Long,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("longest_streak") val longestStreak: Int,
)

@Serializable
private data class AchievementDto(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    @SerialName("xp_reward") val xpReward: Int,
)

@Serializable
private data class UserAchievementDto(
    @SerialName("achievement_id") val achievementId: String,
)

@Serializable
private data class ProgressTotalsDto(
    @SerialName("total_trainings") val totalTrainings: Int,
    @SerialName("completed_training_sessions") val completedTrainingSessions: Int,
    @SerialName("completed_practice_sessions") val completedPracticeSessions: Int,
    @SerialName("total_awarded_xp") val totalAwardedXp: Long,
)

@Serializable
private data class SessionDto(
    val id: String,
    @SerialName("awarded_xp") val awardedXp: Int,
    @SerialName("completed_at") val completedAt: String?,
)
