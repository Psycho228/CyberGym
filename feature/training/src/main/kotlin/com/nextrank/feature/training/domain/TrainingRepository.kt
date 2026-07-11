package com.nextrank.feature.training.domain

import com.nextrank.core.common.result.Result

data class TrainingExercise(
    val itemId: String,
    val exerciseId: String,
    val title: String,
    val description: String,
    val instructions: String,
    val resultType: String,
    val estimatedMinutes: Int,
    val baseXp: Int,
)

data class TrainingSession(
    val sessionId: String,
    val planTitle: String,
    val exercises: List<TrainingExercise>,
)

data class TrainingCompletion(
    val awardedXp: Int,
    val totalXp: Int,
    val level: Int,
    val streak: Int,
)

data class CatalogExercise(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val baseXp: Int,
)

interface TrainingRepository {
    suspend fun startOrResume(planId: String): Result<TrainingSession>
    suspend fun complete(
        sessionId: String,
        itemIds: List<String>,
        idempotencyKey: String,
    ): Result<TrainingCompletion>
    suspend fun loadAllExercises(): Result<List<CatalogExercise>>
}