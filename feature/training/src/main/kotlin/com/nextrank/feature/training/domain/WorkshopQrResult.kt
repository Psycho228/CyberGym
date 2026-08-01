package com.nextrank.feature.training.domain

private const val MAX_TEXT_LENGTH = 8_192
private const val MAX_RESULTS = 20
private const val MAX_METRICS_PER_EXERCISE = 30
private const val MIN_RUN_ID_LENGTH = 4
private const val MIN_MAP_NAME_LENGTH = 2
private const val MAX_FIELD_LENGTH = 120
private const val MIN_EXERCISE_TOKENS = 5

data class WorkshopResult(
    val mapName: String,
    val runId: String,
    val completedAt: String? = null,
    val exercises: List<WorkshopExerciseResult>,
)

data class WorkshopExerciseResult(
    val exerciseSlug: String,
    val metrics: Map<String, String>,
)

/**
 * Parses a deliberately OCR-friendly text block rendered by the Workshop map.
 * Delimiters (:, = and |) are optional because OCR can lose them.
 */
object WorkshopTextParser {
    fun parse(recognizedText: String, expectedExerciseSlugs: Set<String>): WorkshopResult {
        val lines = recognizedText
            .lineSequence()
            .map(::normalizeLine)
            .filter(String::isNotBlank)
            .toList()

        require(recognizedText.isNotBlank()) { "Текст на экране не найден." }
        require(recognizedText.length <= MAX_TEXT_LENGTH) { "Распознанный текст слишком большой." }
        require(lines.any { it.contains("CYBERGYM") && it.contains("V1") }) {
            "Наведи рамку на заголовок CYBERGYM RESULT V1."
        }
        require(lines.any { it == "END" || it.startsWith("END ") }) {
            "В рамку не попал весь блок результатов."
        }

        val runId = fieldValue(lines, "RUN")
        require(runId.length in MIN_RUN_ID_LENGTH..MAX_FIELD_LENGTH) {
            "Не удалось распознать RUN ID."
        }
        val mapName = fieldValue(lines, "MAP")
        require(mapName.length in MIN_MAP_NAME_LENGTH..MAX_FIELD_LENGTH) {
            "Не удалось распознать название карты."
        }

        val exercises = lines
            .filter { it.startsWith("EX ") }
            .map(::parseExercise)

        require(exercises.isNotEmpty()) { "Не найдены строки EX с результатами." }
        require(exercises.size <= MAX_RESULTS) { "Слишком много упражнений в результате." }

        val duplicateSlugs = exercises
            .groupingBy { it.exerciseSlug }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        require(duplicateSlugs.isEmpty()) { "Результаты упражнений повторяются." }

        val actualSlugs = exercises.map { it.exerciseSlug }.toSet()
        val missing = expectedExerciseSlugs - actualSlugs
        val unexpected = actualSlugs - expectedExerciseSlugs
        require(missing.isEmpty()) {
            "Не распознаны результаты всей тренировки: ${missing.joinToString()}."
        }
        require(unexpected.isEmpty()) {
            "На экране результат другого упражнения: ${unexpected.joinToString()}."
        }

        return WorkshopResult(
            mapName = mapName.lowercase(),
            runId = runId,
            exercises = exercises,
        )
    }

    fun looksComplete(recognizedText: String): Boolean {
        val normalized = recognizedText.uppercase()
        return "CYBERGYM" in normalized &&
            "V1" in normalized &&
            normalized.lineSequence().any(::isEndLine)
    }

    private fun fieldValue(lines: List<String>, field: String): String =
        lines.firstOrNull { it.startsWith("$field ") }
            ?.removePrefix("$field ")
            ?.substringBefore(' ')
            ?.trim()
            .orEmpty()

    private fun parseExercise(line: String): WorkshopExerciseResult {
        val tokens = line.split(' ').filter(String::isNotBlank)
        require(tokens.size >= MIN_EXERCISE_TOKENS) {
            "Строка упражнения распознана не полностью."
        }

        val exerciseSlug = exerciseSlug(tokens[1])
        val metricTokens = tokens.drop(2)
        require(metricTokens.size % 2 == 0) { "Не удалось разделить названия и значения метрик." }

        val metrics = metricTokens
            .chunked(2)
            .associate { pair ->
                metricKey(pair[0]) to normalizeMetricValue(pair[1])
            }
        require(metrics.isNotEmpty()) { "Нет метрик для $exerciseSlug." }
        require(metrics.size <= MAX_METRICS_PER_EXERCISE) { "Слишком много метрик для $exerciseSlug." }

        return WorkshopExerciseResult(exerciseSlug = exerciseSlug, metrics = metrics)
    }

    private fun exerciseSlug(value: String): String =
        when (value.uppercase()) {
            "WARMUP", "FLICKS", "WARMUP_FLICKS" -> "warmup_flicks"
            "AIM", "AIM50", "HEADSHOTS", "AIM_HEADSHOTS" -> "aim_headshots"
            "SPRAY", "SPRAY5", "AK_SPRAY" -> "ak_spray"
            "STRAFE", "STRAFE50", "COUNTER_STRAFE" -> "counter_strafe"
            else -> value.lowercase()
        }

    private fun metricKey(value: String): String =
        when (value.uppercase()) {
            "ATT", "ATTEMPTS" -> "attempts"
            "HIT", "HITS" -> "hits"
            "HS", "HEADSHOTS" -> "headshots"
            "ACC", "ACCURACY" -> "accuracy"
            "STOPS", "SUCCESSFUL_STOPS" -> "successful_stops"
            "SPEED", "AVG_SPEED", "AVG_STOP_SPEED" -> "avg_stop_speed"
            "TIME", "DURATION", "DURATION_SECONDS" -> "duration_seconds"
            "BEST", "BEST_TIME", "BEST_TIME_MS" -> "best_time_ms"
            "AVG_TIME", "AVERAGE_TIME_MS" -> "average_time_ms"
            "SCORE" -> "score"
            else -> value.lowercase()
        }

    private fun normalizeMetricValue(value: String): String {
        val normalized = value
            .removeSuffix("%")
            .replace(',', '.')
            .trim()
        require(normalized.isNotEmpty()) { "Распознано пустое значение метрики." }
        return normalized
    }

    private fun normalizeLine(value: String): String =
        value
            .uppercase()
            .replace(Regex("[:=|]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun isEndLine(value: String): Boolean {
        val line = normalizeLine(value)
        return line == "END" || line.startsWith("END ")
    }
}
