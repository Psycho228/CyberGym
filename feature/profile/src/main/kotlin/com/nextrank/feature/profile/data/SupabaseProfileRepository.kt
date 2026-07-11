package com.nextrank.feature.profile.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.profile.domain.ProfileData
import com.nextrank.feature.profile.domain.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseProfileRepository(
    private val supabaseClient: SupabaseClient,
) : ProfileRepository {

    override suspend fun loadProfile(): Result<ProfileData> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Пользователь не авторизован")

        val dto = supabaseClient.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<ProfileDto>()

        ProfileData(
            nickname = dto.nickname.ifBlank { "Игрок" },
            currentRank = dto.currentRank,
            primaryGoal = dto.primaryGoal,
            dailyMinutes = dto.dailyMinutes,
            level = dto.level,
            totalXp = dto.totalXp,
            currentStreak = dto.currentStreak,
            longestStreak = dto.longestStreak,
        )
    }.fold({ Result.Success(it) }, { Result.Failure(it.toAppError()) })
}

@Serializable
private data class ProfileDto(
    val nickname: String,
    @SerialName("current_rank") val currentRank: String?,
    @SerialName("primary_goal") val primaryGoal: String?,
    @SerialName("daily_minutes") val dailyMinutes: Int,
    val level: Int,
    @SerialName("total_xp") val totalXp: Long,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("longest_streak") val longestStreak: Int,
)
