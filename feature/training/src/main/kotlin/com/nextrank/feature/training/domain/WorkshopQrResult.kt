package com.nextrank.feature.training.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.util.Base64

const val WORKSHOP_QR_PREFIX = "CYBERGYM1:"
private const val WORKSHOP_SOURCE = "cybergym_workshop"
private const val MAX_QR_LENGTH = 16_384
private const val MAX_RESULTS = 20
private const val MAX_METRICS_PER_EXERCISE = 30

data class WorkshopQrResult(
    val mapName: String,
    val runId: String,
    val completedAt: String?,
    val exercises: List<WorkshopExerciseResult>,
)

data class WorkshopExerciseResult(
    val exerciseSlug: String,
    val metrics: Map<String, String>,
)

object WorkshopQrParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun parse(rawValue: String, expectedExerciseSlugs: Set<String>): WorkshopQrResult {
        val raw = rawValue.trim()
        require(raw.isNotEmpty()) { "QR-код пуст." }
        require(raw.length <= MAX_QR_LENGTH) { "QR-код слишком большой." }

        val jsonText = if (raw.startsWith(WORKSHOP_QR_PREFIX)) {
            val encoded = raw.removePrefix(WORKSHOP_QR_PREFIX)
            runCatching {
                String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)
            }.getOrElse { throw IllegalArgumentException("Повреждённый QR-код CyberGym.") }
        } else {
            raw
        }

        val payload = runCatching { json.decodeFromString<WorkshopPayloadDto>(jsonText) }
            .getOrElse { throw IllegalArgumentException("Это не QR-код результата CyberGym.") }

        require(payload.version == 1) { "Версия QR-кода не поддерживается." }
        require(payload.source == WORKSHOP_SOURCE) { "QR-код создан не картой CyberGym." }
        require(payload.mapName.isNotBlank()) { "В QR-коде не указана карта." }
        require(payload.runId.isNotBlank()) { "В QR-коде нет идентификатора запуска." }
        require(payload.results.isNotEmpty()) { "В QR-коде нет результатов." }
        require(payload.results.size <= MAX_RESULTS) { "В QR-коде слишком много упражнений." }

        val duplicateSlugs = payload.results
            .groupingBy { it.exerciseSlug }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        require(duplicateSlugs.isEmpty()) { "В QR-коде повторяются упражнения." }

        val actualSlugs = payload.results.map { it.exerciseSlug }.toSet()
        val missing = expectedExerciseSlugs - actualSlugs
        val unexpected = actualSlugs - expectedExerciseSlugs
        require(missing.isEmpty()) {
            "QR-код не содержит результаты всей тренировки: ${missing.joinToString()}."
        }
        require(unexpected.isEmpty()) {
            "QR-код содержит другое упражнение: ${unexpected.joinToString()}."
        }

        return WorkshopQrResult(
            mapName = payload.mapName,
            runId = payload.runId,
            completedAt = payload.completedAt,
            exercises = payload.results.map { result ->
                require(result.exerciseSlug.isNotBlank()) { "Не указан код упражнения." }
                require(result.metrics.isNotEmpty()) { "Нет метрик для ${result.exerciseSlug}." }
                require(result.metrics.size <= MAX_METRICS_PER_EXERCISE) {
                    "Слишком много метрик для ${result.exerciseSlug}."
                }
                WorkshopExerciseResult(
                    exerciseSlug = result.exerciseSlug,
                    metrics = result.metrics.mapValues { (_, value) -> value.toEditableString() },
                )
            },
        )
    }

    private fun JsonPrimitive.toEditableString(): String =
        contentOrNull ?: throw IllegalArgumentException("Метрика не содержит значения.")
}

@Serializable
private data class WorkshopPayloadDto(
    @SerialName("v") val version: Int,
    val source: String,
    @SerialName("map") val mapName: String,
    @SerialName("run_id") val runId: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val results: List<WorkshopExerciseResultDto>,
)

@Serializable
private data class WorkshopExerciseResultDto(
    @SerialName("exercise") val exerciseSlug: String,
    val metrics: Map<String, JsonPrimitive>,
)
