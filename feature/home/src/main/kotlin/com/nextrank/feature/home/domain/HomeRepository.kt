package com.nextrank.feature.home.domain

import com.nextrank.core.common.result.Result

data class HomeSnapshot(
    val nickname: String,
    val level: Int,
    val totalXp: Long,
    val streak: Int,
    val planId: String,
    val exerciseCount: Int,
    val estimatedMinutes: Int,
)

interface HomeRepository {
    suspend fun loadHome(): Result<HomeSnapshot>
}
