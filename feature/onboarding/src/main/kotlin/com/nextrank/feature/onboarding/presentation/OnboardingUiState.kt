@file:Suppress("MagicNumber")

package com.nextrank.feature.onboarding.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingUiState(
    val nickname: String = "",
    val goal: OnboardingGoal? = null,
    val premierRating: String = "",
    val faceitLevel: FaceitLevel = FaceitLevel.NOT_PLAYING,
    val modes: Set<PlayMode> = emptySet(),
    val favoriteMaps: Set<Cs2Map> = emptySet(),
    val trainingDuration: TrainingDuration = TrainingDuration.MINUTES_30,
    val trainingFrequency: TrainingFrequency = TrainingFrequency.FOUR_DAYS,
    val weakSpots: Set<WeakSpot> = emptySet(),
    val tools: Set<TrainingTool> = emptySet(),
    val connectFaceit: Boolean? = null,
    val faceitNickname: String = "",
    val faceitStatus: FaceitConnectStatus = FaceitConnectStatus.IDLE,
    val faceitPlayer: ConnectedFaceitPlayer? = null,
    val faceitError: String? = null,
    val selfScores: Map<SelfScoreCategory, Int> = SelfScoreCategory.entries.associateWith { DEFAULT_SELF_SCORE },
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false,
)

private const val DEFAULT_SELF_SCORE = 5

enum class OnboardingGoal(val label: String) {
    PREMIER("Поднять Premier"),
    FACEIT("Поднять FACEIT"),
    CONSISTENCY("Стать стабильнее"),
    FRIENDS("Лучше играть с друзьями"),
}

enum class FaceitLevel(val label: String, val value: String?) {
    NOT_PLAYING("Не играю", null),
    LEVEL_1_2("1–2", "1-2"),
    LEVEL_3_4("3–4", "3-4"),
    LEVEL_5_6("5–6", "5-6"),
    LEVEL_7_8("7–8", "7-8"),
    LEVEL_9_10("9–10", "9-10"),
}

enum class PlayMode(val label: String) {
    PREMIER("Premier"),
    FACEIT("FACEIT"),
    MATCHMAKING("Matchmaking"),
}

enum class Cs2Map(val label: String) {
    MIRAGE("Mirage"),
    DUST_2("Dust II"),
    INFERNO("Inferno"),
    ANCIENT("Ancient"),
    NUKE("Nuke"),
    TRAIN("Train"),
    ANUBIS("Anubis"),
    OVERPASS("Overpass"),
}

enum class TrainingDuration(val label: String, val minutes: Int) {
    MINUTES_20("20 минут", 20),
    MINUTES_30("30 минут", 30),
    MINUTES_40("40 минут", 40),
}

enum class TrainingFrequency(val label: String, val daysPerWeek: Int) {
    THREE_DAYS("3 дня в неделю", 3),
    FOUR_DAYS("4 дня", 4),
    FIVE_DAYS("5 дней", 5),
    EVERY_DAY("Каждый день", 7),
}

enum class WeakSpot(val label: String) {
    AIM("Aim"),
    SPRAY("Spray"),
    MOVEMENT("Movement"),
    COUNTER_STRAFE("Counter-strafe"),
    UTILITY("Utility"),
    POSITIONING("Positioning"),
    CROSSHAIR("Crosshair Placement"),
    PEEK("Peek"),
    GAME_SENSE("Game Sense"),
    UNKNOWN("Не знаю"),
}

enum class TrainingTool(val label: String) {
    AIM_BOTZ("Aim Botz"),
    AIMLABS("Aimlabs"),
    WORKSHOP("Workshop карты"),
    REFRAG("Refrag"),
    YPRAC("Yprac"),
    YOUTUBE("YouTube"),
    NOTHING("Ничего"),
}

enum class SelfScoreCategory(val label: String) {
    AIM("Aim"),
    MOVEMENT("Movement"),
    UTILITY("Utility"),
    POSITIONING("Positioning"),
    CROSSHAIR("Crosshair Placement"),
    GAME_SENSE("Game Sense"),
}

enum class FaceitConnectStatus {
    IDLE,
    CONNECTING,
    CONNECTED,
    SKIPPED,
    ERROR,
}

data class ConnectedFaceitPlayer(
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
