package com.nextrank.feature.home.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val level: Int = 1,
    val totalXp: Long = 0,
    val streak: Int = 0,
    val exerciseCount: Int = 0,
    val estimatedMinutes: Int = 0,
    val planId: String? = null,
    val errorMessage: String? = null,
)
