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
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseProgressRepository(
    private val supabaseClient: SupabaseClient,
) : ProgressRepository {

    override suspend fun loadProgress(): Result<ProgressData> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Пользователь не авторизован")

        val profile = supabaseClient.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<ProfileStatsDto>()

        val achievements = supabaseClient.from("achievements")
            .select { filter { eq("is_active", true) } }
            .decodeList<AchievementDto>()

        val unlocked = supabaseClient.from("user_achievements")
            .select { filter { eq("user_id", userId) } }
            .decodeList<UserAchievementDto>()
        val unlockedIds = unlocked.map { it.achievementId }.toSet()

        val sessions = supabaseClient.from("training_sessions")
            .select {
                filter { eq("user_id", userId); eq("status", "completed") }
                order("completed_at", Order.DESCENDING)
                limit(10)
            }
            .decodeList<SessionDto>()

        ProgressData(
            stats = ProgressStats(
                level = profile.level,
                totalXp = profile.totalXp,
                currentStreak = profile.currentStreak,
                longestStreak = profile.longestStreak,
                totalTrainings = sessions.size,
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
            recentSessions = sessions.map {
                SessionHistoryItem(
                    sessionId = it.id,
                    awardedXp = it.awardedXp,
                    completedAt = it.completedAt,
                )
            },
        )
    }.fold({ Result.Success(it) }, { Result.Failure(it.toAppError()) })
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
private data class SessionDto(
    val id: String,
    @SerialName("awarded_xp") val awardedXp: Int,
    @SerialName("completed_at") val completedAt: String?,
)
