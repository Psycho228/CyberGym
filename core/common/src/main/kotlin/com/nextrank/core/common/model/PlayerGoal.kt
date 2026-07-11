package com.nextrank.domain.model

/**
 * Цели пользователя (соответствует player_goal enum в БД).
 */
enum class PlayerGoal {
    RANK_UP,
    AIM,
    MOVEMENT,
    SPRAY,
    DISCIPLINE,
    TEAMPLAY;

    companion object {
        fun fromString(value: String?): PlayerGoal? = when (value) {
            "rank_up" -> RANK_UP
            "aim" -> AIM
            "movement" -> MOVEMENT
            "spray" -> SPRAY
            "discipline" -> DISCIPLINE
            "teamplay" -> TEAMPLAY
            else -> null
        }

        fun PlayerGoal.toStringValue(): String = when (this) {
            RANK_UP -> "rank_up"
            AIM -> "aim"
            MOVEMENT -> "movement"
            SPRAY -> "spray"
            DISCIPLINE -> "discipline"
            TEAMPLAY -> "teamplay"
        }
    }
}
