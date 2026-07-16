package com.nextrank.feature.onboarding.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.onboarding.domain.OnboardingRepository
import com.nextrank.feature.onboarding.presentation.OnboardingGoal
import com.nextrank.feature.onboarding.presentation.OnboardingUiState
import com.nextrank.feature.onboarding.presentation.WeakSpot
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseOnboardingRepository(
    private val supabaseClient: SupabaseClient,
) : OnboardingRepository {

    override suspend fun saveProfile(state: OnboardingUiState): Result<Unit> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Пользователь не авторизован")

        supabaseClient.from("profiles").update(
            OnboardingProfileUpdate(
                nickname = state.nickname.trim(),
                currentRank = state.faceitLevel.value
                    ?: state.faceitPlayer?.skillLevel?.toString()
                    ?: state.premierRating.takeIf(String::isNotBlank),
                primaryGoal = state.goal?.databaseValue(),
                primaryGoals = state.weakSpots.map(WeakSpot::databaseValue),
                dailyMinutes = state.trainingDuration.minutes,
                onboardingAnswers = state.toAnswersJson(),
                onboardingCompleted = true,
            ),
        ) {
            filter { eq("id", userId) }
        }

        Result.Success(Unit)
    }.getOrElse { Result.Failure(it.toAppError()) }
}

@Serializable
private data class OnboardingProfileUpdate(
    val nickname: String,
    @SerialName("current_rank")
    val currentRank: String?,
    @SerialName("primary_goal")
    val primaryGoal: String?,
    @SerialName("primary_goals")
    val primaryGoals: List<String>,
    @SerialName("daily_minutes")
    val dailyMinutes: Int,
    @SerialName("onboarding_answers")
    val onboardingAnswers: JsonObject,
    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean,
)

private fun OnboardingUiState.toAnswersJson(): JsonObject = buildJsonObject {
    put("nickname", nickname.trim())
    put("goal", goal?.name)
    put("premier_rating", premierRating)
    put("faceit_level", faceitLevel.name)
    put("faceit_skill_level", faceitPlayer?.skillLevel)
    put("faceit_elo", faceitPlayer?.faceitElo)
    put("faceit_nickname", faceitNickname)
    put("training_duration_minutes", trainingDuration.minutes)
    put("training_frequency_days", trainingFrequency.daysPerWeek)
    put("connect_faceit", connectFaceit)
    faceitPlayer?.let { player ->
        put("faceit_player", buildJsonObject {
            put("player_id", player.playerId)
            put("nickname", player.nickname)
            put("avatar", player.avatar)
            put("country", player.country)
            put("faceit_url", player.faceitUrl)
            put("game", player.game)
            put("game_player_id", player.gamePlayerId)
            put("skill_level", player.skillLevel)
            put("faceit_elo", player.faceitElo)
        })
    }
    put("modes", modes.mapToJsonArray { it.name })
    put("favorite_maps", favoriteMaps.mapToJsonArray { it.name })
    put("weak_spots", weakSpots.mapToJsonArray { it.name })
    put("tools", tools.mapToJsonArray { it.name })
    put("self_scores", buildJsonObject {
        selfScores.forEach { (category, score) -> put(category.name, score) }
    })
}

private fun <T> Iterable<T>.mapToJsonArray(transform: (T) -> String) = buildJsonArray {
    forEach { add(JsonPrimitive(transform(it))) }
}

private fun OnboardingGoal.databaseValue(): String = when (this) {
    OnboardingGoal.PREMIER -> "rank_up"
    OnboardingGoal.FACEIT -> "rank_up"
    OnboardingGoal.CONSISTENCY -> "discipline"
    OnboardingGoal.FRIENDS -> "teamplay"
}

private fun WeakSpot.databaseValue(): String = when (this) {
    WeakSpot.AIM -> "aim"
    WeakSpot.SPRAY -> "spray"
    WeakSpot.MOVEMENT -> "movement"
    WeakSpot.COUNTER_STRAFE -> "movement"
    WeakSpot.UTILITY -> "teamplay"
    WeakSpot.POSITIONING -> "discipline"
    WeakSpot.CROSSHAIR -> "aim"
    WeakSpot.PEEK -> "aim"
    WeakSpot.GAME_SENSE -> "discipline"
    WeakSpot.UNKNOWN -> "discipline"
}
