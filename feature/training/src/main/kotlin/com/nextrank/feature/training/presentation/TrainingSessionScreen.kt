@file:Suppress("FunctionNaming", "LongMethod", "MaxLineLength")

package com.nextrank.feature.training.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.component.GamerAccentLime
import com.nextrank.core.designsystem.component.GamerAccentOrange
import com.nextrank.core.designsystem.component.GamerChip
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerPrimaryButton
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import com.nextrank.feature.training.domain.WorkshopResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrainingSessionScreen(
    sessionId: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainingSessionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) { viewModel.load(sessionId) }
    LaunchedEffect(state.isComplete) { if (state.isComplete) onComplete() }

    if (state.isScanningResult) {
        WorkshopResultScanner(
            errorMessage = state.errorMessage,
            onTextDetected = viewModel::acceptRecognizedText,
            onClose = { viewModel.finishResultScan() },
            onCameraError = { viewModel.finishResultScan(it) },
            modifier = modifier,
        )
        return
    }

    GamerScreen(modifier = modifier) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.errorMessage != null && state.exercises.isEmpty() -> {
                GamerHeader(title = "Тренировка недоступна")
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                GamerSecondaryButton(text = "Назад", onClick = onBack)
            }
            state.scannedResult != null -> {
                GamerHeader(
                    eyebrow = "Результаты Workshop",
                    title = "Проверь показатели",
                    subtitle = "Данные считаны с карты. При необходимости исправь значения перед сохранением.",
                )
                WorkshopResultReview(
                    result = state.scannedResult!!,
                    state = state,
                    actions = WorkshopResultReviewActions(
                        onMetricChange = viewModel::updateMetric,
                        onConfirm = viewModel::confirmResults,
                        onRescan = viewModel::rescanResult,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
            state.exercises.isNotEmpty() -> {
                GamerHeader(
                    title = state.planTitle.ifBlank { "Тренировка" },
                    subtitle = "Пройди задания на карте CyberGym Workshop. После финиша наведи камеру на текст результатов.",
                )
                TrainingExerciseContent(
                    state = state,
                    onBack = onBack,
                    onCompleteCurrent = viewModel::completeCurrent,
                    onScanResult = viewModel::beginResultScan,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TrainingExerciseContent(
    state: TrainingSessionUiState,
    onBack: () -> Unit,
    onCompleteCurrent: () -> Unit,
    onScanResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exercise = state.exercises[state.currentIndex]
    val isLastExercise = state.currentIndex == state.exercises.lastIndex

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${state.currentIndex + 1} из ${state.exercises.size}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            GamerChip(text = "${exercise.baseXp} XP", accent = GamerAccentLime)
        }
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1f) / state.exercises.size },
            modifier = Modifier.fillMaxWidth(),
            color = GamerAccentLime,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        GamerPanel(accent = GamerAccentLime) {
            Text(
                text = exercise.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Задание на карте",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = exercise.instructions, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GamerChip(text = "${exercise.estimatedMinutes} мин")
                GamerChip(text = exercise.resultType.ifBlank { "Workshop" }, accent = GamerAccentOrange)
            }
        }
        if (isLastExercise) {
            GamerPanel(accent = GamerAccentOrange) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TextSnippet,
                        contentDescription = null,
                        tint = GamerAccentOrange,
                    )
                    Text(
                        text = "Финиш по результату карты",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "Заверши тренировку на карте и наведи камеру на весь текстовый блок результатов.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        GamerPrimaryButton(
            text = when {
                isLastExercise -> "Завершить тренировку"
                else -> "Следующее упражнение"
            },
            onClick = if (isLastExercise) onScanResult else onCompleteCurrent,
        )
        GamerSecondaryButton(text = "Выйти", onClick = onBack)
    }
}

@Composable
private fun WorkshopResultReview(
    result: WorkshopResult,
    state: TrainingSessionUiState,
    actions: WorkshopResultReviewActions,
    modifier: Modifier = Modifier,
) {
    val titlesBySlug = state.exercises.associate { it.slug to it.title }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GamerPanel(accent = GamerAccentLime) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Карта",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(result.mapName, fontWeight = FontWeight.Bold)
                }
                GamerChip(text = "Текст распознан", accent = GamerAccentLime)
            }
        }

        result.exercises.forEach { exerciseResult ->
            GamerPanel(accent = GamerAccentOrange) {
                Text(
                    text = titlesBySlug[exerciseResult.exerciseSlug] ?: exerciseResult.exerciseSlug,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                exerciseResult.metrics.forEach { (key, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = { actions.onMetricChange(exerciseResult.exerciseSlug, key, it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(metricLabel(key)) },
                        singleLine = true,
                    )
                }
            }
        }

        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        GamerPrimaryButton(
            text = if (state.isCompleting) "Сохраняем результаты…" else "Подтвердить и завершить",
            onClick = actions.onConfirm,
            enabled = !state.isCompleting,
        )
        GamerSecondaryButton(
            text = "Сканировать заново",
            onClick = actions.onRescan,
            enabled = !state.isCompleting,
        )
    }
}

private data class WorkshopResultReviewActions(
    val onMetricChange: (String, String, String) -> Unit,
    val onConfirm: () -> Unit,
    val onRescan: () -> Unit,
)

private fun metricLabel(key: String): String =
    when (key) {
        "attempts" -> "Попытки"
        "hits" -> "Попадания"
        "headshots" -> "Хедшоты"
        "accuracy" -> "Точность, %"
        "score" -> "Результат"
        "successful_stops" -> "Успешные остановки"
        "avg_stop_speed" -> "Средняя скорость при выстреле"
        "best_time_ms" -> "Лучшее время, мс"
        "average_time_ms" -> "Среднее время, мс"
        "duration_seconds" -> "Время, сек"
        else -> key.replace('_', ' ').replaceFirstChar(Char::uppercase)
    }
