package com.nextrank.feature.profile.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.profile.domain.FaceitProfileStats
import com.nextrank.feature.profile.domain.ProfileData
import com.nextrank.feature.profile.domain.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
            nickname = dto.displayNickname(),
            currentRank = dto.currentRank,
            primaryGoal = dto.primaryGoal,
            dailyMinutes = dto.dailyMinutes,
            level = dto.level,
            totalXp = dto.totalXp,
            currentStreak = dto.currentStreak,
            longestStreak = dto.longestStreak,
            faceit = dto.onboardingAnswers.toFaceitStats(),
            favoriteMaps = dto.onboardingAnswers.stringList("favorite_maps"),
            weakSpots = dto.onboardingAnswers.stringList("weak_spots"),
            trainingFrequencyDays = dto.onboardingAnswers.intValue("training_frequency_days"),
        )
    }.fold({ Result.Success(it) }, { Result.Failure(it.toAppError()) })
}

@Serializable
private data class ProfileDto(
    val nickname: String = "",
    @SerialName("current_rank") val currentRank: String?,
    @SerialName("primary_goal") val primaryGoal: String?,
    @SerialName("daily_minutes") val dailyMinutes: Int,
    val level: Int,
    @SerialName("total_xp") val totalXp: Long,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("longest_streak") val longestStreak: Int,
    @SerialName("onboarding_answers") val onboardingAnswers: JsonObject? = null,
)

private fun ProfileDto.displayNickname(): String =
    nickname.trim()
        .ifBlank { onboardingAnswers.stringValue("nickname") }
        .ifBlank { onboardingAnswers.stringValue("faceit_nickname") }
        .ifBlank { onboardingAnswers.nestedStringValue("faceit_player", "nickname") }
        .ifBlank { "Игрок" }

private fun JsonObject?.toFaceitStats(): FaceitProfileStats? {
    val player = this?.get("faceit_player")?.jsonObject
    val playerId = player.stringValue("player_id")
    val nickname = stringValue("faceit_nickname")
        .ifBlank { player.stringValue("nickname") }
    val skillLevel = intValue("faceit_skill_level")
        ?: player.intValue("skill_level")
    val elo = intValue("faceit_elo")
        ?: player.intValue("faceit_elo")

    val hasFaceitData = listOf(playerId, nickname).any(String::isNotBlank) ||
        listOf(skillLevel, elo).any { value -> value != null }

    if (!hasFaceitData) return null

    return FaceitProfileStats(
        playerId = playerId.ifBlank { null },
        nickname = nickname.ifBlank { null },
        avatar = player.stringValue("avatar").ifBlank { null },
        country = player.stringValue("country").ifBlank { null },
        faceitUrl = player.stringValue("faceit_url").ifBlank { null },
        skillLevel = skillLevel,
        faceitElo = elo,
        gamePlayerId = player.stringValue("game_player_id").ifBlank { null },
    )
}

private fun JsonObject?.stringValue(key: String): String =
    this?.get(key)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.trim()
        .orEmpty()

private fun JsonObject?.intValue(key: String): Int? =
    this?.get(key)
        ?.jsonPrimitive
        ?.intOrNull

private fun JsonObject?.nestedStringValue(
    objectKey: String,
    valueKey: String,
): String =
    this?.get(objectKey)
        ?.jsonObject
        ?.get(valueKey)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.trim()
        .orEmpty()

private fun JsonObject?.stringList(key: String): List<String> =
    this?.get(key)
        ?.jsonArray
        ?.mapNotNull { item -> item.jsonPrimitive.contentOrNull?.trim()?.takeIf(String::isNotBlank) }
        .orEmpty()
