package com.nextrank.feature.onboarding.data

import com.nextrank.core.common.result.Result
import com.nextrank.core.common.error.toAppError
import com.nextrank.domain.model.Cs2Rank
import com.nextrank.domain.model.PlayerGoal
import com.nextrank.feature.onboarding.domain.OnboardingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Заглушка OnboardingRepository.
 *
 * TODO: Заменить на реализацию через Supabase SDK после подключения зависимостей.
 */
class SupabaseOnboardingRepository(
    private val supabaseClient: SupabaseClient,
) : OnboardingRepository {

    override suspend fun saveProfile(
        nickname: String,
        rank: Cs2Rank?,
        goal: PlayerGoal?,
        dailyMinutes: Int,
    ): Result<Unit> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: error("Пользователь не авторизован")

        supabaseClient.from("profiles").update(
            OnboardingProfileUpdate(
                nickname = nickname.trim(),
                currentRank = rank?.name?.lowercase(),
                primaryGoal = goal?.databaseValue(),
                dailyMinutes = dailyMinutes,
                onboardingCompleted = true,
            ),
        ) {
            filter {
                eq("id", userId)
            }
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
    @SerialName("daily_minutes")
    val dailyMinutes: Int,
    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean,
)

private fun PlayerGoal.databaseValue(): String = when (this) {
    PlayerGoal.RANK_UP -> "rank_up"
    PlayerGoal.AIM -> "aim"
    PlayerGoal.MOVEMENT -> "movement"
    PlayerGoal.SPRAY -> "spray"
    PlayerGoal.DISCIPLINE -> "discipline"
    PlayerGoal.TEAMPLAY -> "teamplay"
}
