package com.nextrank.feature.onboarding.domain

import com.nextrank.core.common.result.Result

interface FaceitRepository {
    suspend fun findPlayer(nickname: String): Result<FaceitPlayer>
}

data class FaceitPlayer(
    val playerId: String,
    val nickname: String,
    val avatar: String?,
    val country: String?,
    val faceitUrl: String?,
    val game: String,
    val gamePlayerId: String?,
    val skillLevel: Int?,
    val faceitElo: Int?,
)
