@file:Suppress("FunctionNaming", "LongMethod", "MaxLineLength")

package com.nextrank.feature.training.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("LongMethod")
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

    GamerScreen(modifier = modifier) {
        GamerHeader(
            title = state.planTitle.ifBlank { "Тренировка" },
            subtitle = "Выполняй упражнения по очереди. Результат сохраняется после финального шага.",
        )

        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.errorMessage != null && state.exercises.isEmpty() -> {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                GamerSecondaryButton(text = "Назад", onClick = onBack)
            }
            state.exercises.isNotEmpty() -> TrainingExerciseContent(state, onBack, viewModel::completeCurrent)
        }
    }
}

@Composable
private fun TrainingExerciseContent(
    state: TrainingSessionUiState,
    onBack: () -> Unit,
    onCompleteCurrent: () -> Unit,
) {
    val exercise = state.exercises[state.currentIndex]

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
            text = "Инструкция",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = exercise.instructions,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "${exercise.estimatedMinutes} мин")
            GamerChip(text = exercise.resultType.ifBlank { "manual" }, accent = GamerAccentOrange)
        }
    }
    GamerPanel(accent = GamerAccentOrange) {
        Text(
            text = "Критерий выполнения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Отметь упражнение выполненным только если реально сделал подходы. CyberGym не подтверждает результат автоматически.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    GamerPrimaryButton(
        text = when {
            state.isCompleting -> "Сохранение..."
            state.currentIndex == state.exercises.lastIndex -> "Завершить тренировку"
            else -> "Упражнение выполнено"
        },
        onClick = onCompleteCurrent,
        enabled = !state.isCompleting,
    )
    GamerSecondaryButton(text = "Выйти", onClick = onBack)
}
