package com.nextrank.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Профиль пользователя.
 * Агрегированные данные из profiles.
 */
data class UserProfile(
    val id: String,
    val nickname: String,
    val timezone: String,
    val currentRank: String?,
    val primaryGoal: PlayerGoal?,
    val dailyMinutes: Int,
    val reminderTime: LocalTime?,
    val onboardingCompleted: Boolean,
    val totalXp: Long,
    val level: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastTrainingLocalDate: LocalDate?,
)

/**
 * Ранги CS2.
 */
enum class Cs2Rank {
    UNRANKED,
    GOLD_NOVA_I,
    GOLD_NOVA_II,
    GOLD_NOVA_III,
    GOLD_NOVA_MASTER,
    MASTER_GOLD_NOVA_I,
    MASTER_GOLD_NOVA_II,
    MASTER_GOLD_NOVA_III,
    MASTER_GOLD_NOVA_MASTER,
    MASTER_SUPERNOVA,
    MASTER_SUPERNOVA_II,
    MASTER_SUPERNOVA_MASTER,
    LEETONE,
    LEETWO,
    LEEETHREE,
    LEEFOUR,
    LEEFIVE;

    companion object {
        val defaultOptions: List<Cs2Rank> = listOf(
            UNRANKED,
            GOLD_NOVA_I,
            GOLD_NOVA_III,
            GOLD_NOVA_MASTER,
            MASTER_GOLD_NOVA_I,
            MASTER_SUPERNOVA,
            LEETONE,
        )

        /** Поиск ранга по отображаемому названию. */
        fun fromDisplay(display: String): Cs2Rank? = values().find { it.displayName == display }
    }

    /** Отображаемое название ранга. */
    val displayName: String
        get() = when (this) {
            UNRANKED -> "Без ранга"
            GOLD_NOVA_I -> "Gold Nova I"
            GOLD_NOVA_II -> "Gold Nova II"
            GOLD_NOVA_III -> "Gold Nova III"
            GOLD_NOVA_MASTER -> "Gold Nova Master"
            MASTER_GOLD_NOVA_I -> "Master GN I"
            MASTER_GOLD_NOVA_II -> "Master GN II"
            MASTER_GOLD_NOVA_III -> "Master GN III"
            MASTER_GOLD_NOVA_MASTER -> "Master GN Master"
            MASTER_SUPERNOVA -> "Master Supernova"
            MASTER_SUPERNOVA_II -> "Master SN II"
            MASTER_SUPERNOVA_MASTER -> "Master SN Master"
            LEETONE -> "LEET1"
            LEETWO -> "LEET2"
            LEEETHREE -> "LEET3"
            LEEFOUR -> "LEET4"
            LEEFIVE -> "LEET5"
        }
}
