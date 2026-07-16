package com.nextrank.feature.home.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.home.domain.HomeRepository
import com.nextrank.feature.home.domain.HomeSnapshot
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.LocalDate

class SupabaseHomeRepository(
    private val supabaseClient: SupabaseClient,
) : HomeRepository {

    override suspend fun loadHome(): Result<HomeSnapshot> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Пользователь не авторизован")

        val profile = supabaseClient.from("profiles")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingle<ProfileDto>()

        val plan = loadDailyPlanOrNull()

        HomeSnapshot(
            nickname = profile.displayNickname(),
            level = profile.level,
            totalXp = profile.totalXp,
            streak = profile.currentStreak,
            planId = plan?.planId,
            exerciseCount = plan?.exerciseCount?.toInt() ?: 0,
            estimatedMinutes = plan?.estimatedMinutes ?: 0,
        )
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Failure(it.toAppError()) },
    )

    private suspend fun loadDailyPlanOrNull(): DailyPlanDto? =
        runCatching {
            supabaseClient.postgrest
                .rpc(
                    function = "get_or_create_daily_plan",
                    parameters = buildJsonObject {
                        put("target_date", LocalDate.now().toString())
                    },
                )
                .decodeSingle<DailyPlanDto>()
        }.getOrNull()
}

@Serializable
private data class ProfileDto(
    val nickname: String = "",
    val level: Int,
    @SerialName("total_xp") val totalXp: Long,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("onboarding_answers") val onboardingAnswers: JsonObject? = null,
)

@Serializable
private data class DailyPlanDto(
    @SerialName("plan_id") val planId: String,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("exercise_count") val exerciseCount: Long,
)

private fun ProfileDto.displayNickname(): String =
    nickname.trim()
        .ifBlank { onboardingAnswers.stringValue("nickname") }
        .ifBlank { onboardingAnswers.stringValue("faceit_nickname") }
        .ifBlank { onboardingAnswers.nestedStringValue("faceit_player", "nickname") }
        .ifBlank { "Игрок" }

private fun JsonObject?.stringValue(key: String): String =
    this?.get(key)
        ?.jsonPrimitive
        ?.contentOrNull
        ?.trim()
        .orEmpty()

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
