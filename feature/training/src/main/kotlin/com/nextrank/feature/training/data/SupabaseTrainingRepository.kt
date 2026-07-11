package com.nextrank.feature.training.data

import android.util.Log
import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.training.domain.CatalogExercise
import com.nextrank.feature.training.domain.TrainingCompletion
import com.nextrank.feature.training.domain.TrainingExercise
import com.nextrank.feature.training.domain.TrainingRepository
import com.nextrank.feature.training.domain.TrainingSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class SupabaseTrainingRepository(private val client: SupabaseClient) : TrainingRepository {

    override suspend fun startExercise(exerciseId: String): Result<TrainingSession> = runCatching {
        val rows = client.postgrest.rpc(
            "start_or_resume_practice_exercise",
            buildJsonObject { put("p_exercise_id", exerciseId) },
        ).decodeList<TrainingRowDto>()
        require(rows.isNotEmpty()) { "Exercise has no data" }
        rows.toTrainingSession(sessionIdPrefix = "practice-")
    }.fold({ Result.Success(it) }, { throwable ->
        Log.e(TAG, "Failed to start practice exercise: exerciseId=$exerciseId", throwable)
        Result.Failure(throwable.toAppError())
    })

    override suspend fun startOrResume(planId: String): Result<TrainingSession> = runCatching {
        val rows = client.postgrest.rpc(
            "start_or_resume_training",
            buildJsonObject { put("p_plan_id", planId) },
        ).decodeList<TrainingRowDto>()
        require(rows.isNotEmpty()) { "Training plan has no exercises" }
        rows.toTrainingSession()
    }.fold({ Result.Success(it) }, { throwable ->
        Log.e(TAG, "Failed to start training plan: planId=$planId", throwable)
        Result.Failure(throwable.toAppError())
    })

    override suspend fun complete(
        sessionId: String,
        itemIds: List<String>,
        idempotencyKey: String,
    ): Result<TrainingCompletion> = runCatching {
        val row = if (sessionId.startsWith(PRACTICE_SESSION_PREFIX)) {
            client.postgrest.rpc(
                "complete_practice_session",
                buildJsonObject {
                    put("p_session_id", sessionId.removePrefix(PRACTICE_SESSION_PREFIX))
                    put("p_idempotency_key", idempotencyKey)
                    put("p_client_completed_at", Instant.now().toString())
                },
            ).decodeSingle<TrainingCompletionDto>()
        } else {
            client.postgrest.rpc(
                "complete_training_session",
                buildJsonObject {
                    put("p_session_id", sessionId)
                    put("p_idempotency_key", idempotencyKey)
                    put("p_client_completed_at", Instant.now().toString())
                    put(
                        "p_results",
                        buildJsonArray {
                            itemIds.forEach { itemId ->
                                add(
                                    buildJsonObject {
                                        put("item_id", itemId)
                                        put("result", buildJsonObject {})
                                    },
                                )
                            }
                        },
                    )
                },
            ).decodeSingle<TrainingCompletionDto>()
        }
        TrainingCompletion(row.awardedXp, row.totalXp.toInt(), row.level, row.streak)
    }.fold({ Result.Success(it) }, { throwable ->
        Log.e(TAG, "Failed to complete training: sessionId=$sessionId itemIds=$itemIds", throwable)
        Result.Failure(throwable.toAppError())
    })

    override suspend fun loadAllExercises(): Result<List<CatalogExercise>> = runCatching {
        client.from("exercises")
            .select {
                filter { eq("is_active", true) }
                order("title", Order.ASCENDING)
            }
            .decodeList<ExerciseCatalogDto>()
            .map {
                CatalogExercise(
                    id = it.id,
                    slug = it.slug,
                    title = it.title,
                    description = it.description,
                    instructions = it.instructions,
                    resultType = it.resultType,
                    estimatedMinutes = it.estimatedMinutes,
                    baseXp = it.baseXp,
                )
            }
    }.fold({ Result.Success(it) }, { throwable ->
        Log.e(TAG, "Failed to load catalog exercises", throwable)
        Result.Failure(throwable.toAppError())
    })

    private fun List<TrainingRowDto>.toTrainingSession(sessionIdPrefix: String = ""): TrainingSession =
        TrainingSession(
            sessionId = "$sessionIdPrefix${first().sessionId}",
            planTitle = first().planTitle,
            exercises = map { row ->
                TrainingExercise(
                    itemId = row.itemId,
                    exerciseId = row.exerciseId,
                    title = row.exerciseTitle,
                    description = row.exerciseDescription,
                    instructions = row.instructions,
                    resultType = row.resultType,
                    estimatedMinutes = row.estimatedMinutes,
                    baseXp = row.baseXp,
                )
            },
        )

    private companion object {
        const val PRACTICE_SESSION_PREFIX = "practice-"
        const val TAG = "CyberGymTraining"
    }
}

@Serializable
private data class TrainingCompletionDto(
    @SerialName("awarded_xp") val awardedXp: Int,
    @SerialName("total_xp") val totalXp: Long,
    val level: Int,
    val streak: Int,
)

@Serializable
private data class TrainingRowDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("plan_title") val planTitle: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("exercise_title") val exerciseTitle: String,
    @SerialName("exercise_description") val exerciseDescription: String,
    val instructions: String,
    @SerialName("result_type") val resultType: String,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("base_xp") val baseXp: Int,
)

@Serializable
private data class ExerciseCatalogDto(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val instructions: String,
    @SerialName("result_type") val resultType: String,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("base_xp") val baseXp: Int,
)
