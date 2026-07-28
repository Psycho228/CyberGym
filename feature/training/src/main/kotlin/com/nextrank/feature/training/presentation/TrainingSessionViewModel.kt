package com.nextrank.feature.training.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.training.domain.TrainingExercise
import com.nextrank.feature.training.domain.TrainingRepository
import com.nextrank.feature.training.domain.TrainingResultSubmission
import com.nextrank.feature.training.domain.WorkshopQrParser
import com.nextrank.feature.training.domain.WorkshopQrResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val ERROR_DETAILS_MAX_LENGTH = 220

@Immutable
data class TrainingSessionUiState(
    val isLoading: Boolean = true,
    val planTitle: String = "",
    val sessionId: String? = null,
    val exercises: List<TrainingExercise> = emptyList(),
    val currentIndex: Int = 0,
    val errorMessage: String? = null,
    val isScanningQr: Boolean = false,
    val scannedResult: WorkshopQrResult? = null,
    val isCompleting: Boolean = false,
    val isComplete: Boolean = false,
)

class TrainingSessionViewModel(private val repository: TrainingRepository) : ViewModel() {
    private val completionKey = UUID.randomUUID().toString()
    private val _uiState = MutableStateFlow(TrainingSessionUiState())
    val uiState = _uiState.asStateFlow()

    fun load(planId: String) {
        if (_uiState.value.sessionId != null) return
        viewModelScope.launch {
            val result = if (planId.startsWith("exercise-")) {
                repository.startExercise(planId.removePrefix("exercise-"))
            } else {
                repository.startOrResume(planId)
            }
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        planTitle = result.data.planTitle,
                        sessionId = result.data.sessionId,
                        exercises = result.data.exercises,
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Не удалось открыть тренировку. ${result.error.message.orEmpty()}".trim(),
                    )
                }
            }
        }
    }

    fun completeCurrent() {
        val state = _uiState.value
        if (state.currentIndex < state.exercises.lastIndex) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

    fun beginQrScan() {
        val state = _uiState.value
        if (state.isCompleting || state.isComplete) return
        _uiState.update { it.copy(isScanningQr = true, errorMessage = null) }
    }

    fun finishQrScan(message: String? = null) {
        _uiState.update {
            it.copy(
                isScanningQr = false,
                errorMessage = message?.let { reason ->
                    "Не удалось открыть QR-сканер. $reason"
                },
            )
        }
    }

    fun acceptQrCode(rawValue: String): Boolean {
        val expectedSlugs = _uiState.value.exercises.map { it.slug }.toSet()
        val parsedResult = runCatching { WorkshopQrParser.parse(rawValue, expectedSlugs) }
        return parsedResult.fold(
            onSuccess = { result ->
                _uiState.update {
                    it.copy(
                        isScanningQr = false,
                        scannedResult = result,
                        errorMessage = null,
                    )
                }
                true
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isScanningQr = true,
                        errorMessage = error.message ?: "QR-код результата не распознан.",
                    )
                }
                false
            }
        )
    }

    fun updateMetric(exerciseSlug: String, metricKey: String, value: String) {
        _uiState.update { state ->
            val result = state.scannedResult ?: return@update state
            state.copy(
                scannedResult = result.copy(
                    exercises = result.exercises.map { exercise ->
                        if (exercise.exerciseSlug == exerciseSlug) {
                            exercise.copy(metrics = exercise.metrics + (metricKey to value))
                        } else {
                            exercise
                        }
                    },
                ),
                errorMessage = null,
            )
        }
    }

    fun rescanQr() {
        _uiState.update { it.copy(scannedResult = null, errorMessage = null) }
    }

    fun confirmResults() {
        val state = _uiState.value
        val sessionId = state.sessionId
        val scannedResult = state.scannedResult
        val hasBlankMetrics = scannedResult?.let(::hasBlankMetrics) == true
        when {
            hasBlankMetrics -> {
                _uiState.update { it.copy(errorMessage = "Заполните все значения результатов.") }
            }
            sessionId == null || scannedResult == null -> Unit
            state.isCompleting -> Unit
            state.isComplete -> Unit
            else -> {
                val resultsBySlug = scannedResult.exercises.associateBy { it.exerciseSlug }
                val submissions = state.exercises.map { exercise ->
                    val result = requireNotNull(resultsBySlug[exercise.slug])
                    TrainingResultSubmission(
                        itemId = exercise.itemId,
                        exerciseSlug = exercise.slug,
                        mapName = scannedResult.mapName,
                        runId = scannedResult.runId,
                        completedAt = scannedResult.completedAt,
                        metrics = result.metrics,
                    )
                }
                _uiState.update { it.copy(isCompleting = true, errorMessage = null) }
                viewModelScope.launch {
                    when (val result = repository.complete(sessionId, submissions, completionKey)) {
                        is Result.Success -> _uiState.update { it.copy(isCompleting = false, isComplete = true) }
                        is Result.Failure -> _uiState.update {
                            it.copy(
                                isCompleting = false,
                                errorMessage = buildCompleteErrorMessage(result),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun hasBlankMetrics(result: WorkshopQrResult): Boolean =
        result.exercises.any { exercise -> exercise.metrics.values.any(String::isBlank) }

    private fun buildCompleteErrorMessage(result: Result.Failure): String =
        buildString {
            append("Не удалось завершить тренировку.")
            result.error.message
                ?.takeIf { message -> message.isNotBlank() }
                ?.let { message ->
                    append(" Причина: ")
                    append(message.take(ERROR_DETAILS_MAX_LENGTH))
                }
        }
}
