package com.nextrank.domain.model

import java.time.LocalDate

/**
 * Тип результата упражнения (соответствует exercise_result_type enum).
 */
enum class ExerciseResultType {
    TIMER,
    REPETITIONS,
    SCORE,
    CHECKLIST,
    EXTERNAL_TASK,
    SELF_RATING;

    companion object {
        fun fromString(value: String?): ExerciseResultType? = when (value) {
            "timer" -> TIMER
            "repetitions" -> REPETITIONS
            "score" -> SCORE
            "checklist" -> CHECKLIST
            "external_task" -> EXTERNAL_TASK
            "self_rating" -> SELF_RATING
            else -> null
        }
    }
}

/**
 * Категория упражнения.
 */
data class ExerciseCategory(
    val id: String,
    val slug: String,
    val name: String,
    val sortOrder: Int,
)

/**
 * Упражнение — единица контента тренировки.
 */
data class Exercise(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val instructions: String,
    val resultType: ExerciseResultType,
    val estimatedMinutes: Int,
    val baseXp: Int,
    val mediaPath: String?,
    val externalUri: String?,
    val config: Map<String, Any>,
    val isActive: Boolean,
    val categoryId: String,
    val categoryName: String,
)

/**
 * Результат выполнения упражнения.
 */
sealed interface ExerciseResult {
    data class TimerResult(
        val durationSeconds: Long,
    ) : ExerciseResult

    data class RepetitionsResult(
        val count: Int,
    ) : ExerciseResult

    data class ScoreResult(
        val score: Int,
        val maxScore: Int,
    ) : ExerciseResult

    data class ChecklistResult(
        val completedItems: List<String>,
        val totalItems: Int,
    ) : ExerciseResult

    data class ExternalTaskResult(
        val completed: Boolean,
        val notes: String?,
    ) : ExerciseResult

    data class SelfRatingResult(
        val rating: Int,
        val notes: String?,
    ) : ExerciseResult
}

/**
 * Элемент ежедневной программы.
 */
data class DailyPlanItem(
    val id: String,
    val exerciseId: String,
    val position: Int,
    val isRequired: Boolean,
    val configSnapshot: Map<String, Any>,
    val exercise: Exercise,
)

/**
 * Статус daily plan.
 */
enum class DailyPlanStatus {
    ASSIGNED,
    STARTED,
    COMPLETED,
    EXPIRED;

    companion object {
        fun fromString(value: String?): DailyPlanStatus = when (value) {
            "assigned" -> ASSIGNED
            "started" -> STARTED
            "completed" -> COMPLETED
            "expired" -> EXPIRED
            else -> ASSIGNED
        }
    }
}

/**
 * Ежедневная программа.
 */
data class DailyPlan(
    val id: String,
    val planDate: LocalDate,
    val title: String,
    val estimatedMinutes: Int,
    val status: DailyPlanStatus,
    val items: List<DailyPlanItem>,
)

/**
 * Статус тренировочной сессии.
 */
enum class SessionStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED;

    companion object {
        fun fromString(value: String?): SessionStatus = when (value) {
            "in_progress" -> IN_PROGRESS
            "completed" -> COMPLETED
            "abandoned" -> ABANDONED
            else -> IN_PROGRESS
        }
    }
}

/**
 * Тренировочная сессия.
 */
data class TrainingSession(
    val id: String,
    val dailyPlanId: String,
    val status: SessionStatus,
    val startedAt: Long, // epoch millis UTC
    val completedAt: Long?,
    val awardedXp: Int,
)

/**
 * Результат сессии для отправки на сервер.
 */
data class SessionCompletionPayload(
    val sessionId: String,
    val idempotencyKey: String,
    val clientCompletedAt: Long,
    val results: List<ExerciseResultPayload>,
)

data class ExerciseResultPayload(
    val dailyPlanItemId: String,
    val result: Map<String, Any>,
    val completed: Boolean,
)
