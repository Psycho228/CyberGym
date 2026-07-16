@file:Suppress("FunctionNaming", "MagicNumber", "MaxLineLength", "TooManyFunctions")

package com.nextrank.feature.training.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.component.GamerAccentLime
import com.nextrank.core.designsystem.component.GamerAccentOrange
import com.nextrank.core.designsystem.component.GamerAccentPink
import com.nextrank.core.designsystem.component.GamerChip
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerPrimaryButton
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import com.nextrank.feature.training.R
import com.nextrank.feature.training.domain.CatalogExercise
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    onBack: () -> Unit,
    onStartTraining: (exerciseId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainingCatalogViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedExercise by remember { mutableStateOf<CatalogExercise?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    GamerScreen(modifier = modifier) {
        when {
            uiState.isLoading -> TrackLoading()
            uiState.errorMessage != null -> TrackError(
                message = uiState.errorMessage,
                onRetry = viewModel::loadExercises,
                onBack = onBack,
            )
            uiState.exercises.isEmpty() -> EmptyTrack(onBack)
            else -> TrackContent(
                exercises = uiState.exercises,
                onExerciseClick = { selectedExercise = it },
                onBack = onBack,
            )
        }
    }

    selectedExercise?.let { exercise ->
        ModalBottomSheet(
            onDismissRequest = { selectedExercise = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ExerciseDetailSheet(
                exercise = exercise,
                onStart = {
                    selectedExercise = null
                    onStartTraining(exercise.id)
                },
            )
        }
    }
}

@Composable
private fun TrackLoading() {
    GamerHeader(
        title = "Трек",
        subtitle = "Загружаем программу недели и ближайшие упражнения.",
    )
    CircularProgressIndicator()
}

@Composable
private fun TrackError(
    message: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    GamerHeader(
        title = "Трек недоступен",
        subtitle = "Не получилось загрузить программу. Обычно помогает повторная загрузка.",
    )
    GamerPanel(accent = GamerAccentOrange) {
        Text(
            text = message ?: "Не удалось загрузить трек",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        GamerSecondaryButton(text = "Повторить", onClick = onRetry)
        GamerSecondaryButton(text = "Назад", onClick = onBack)
    }
}

@Composable
private fun EmptyTrack(onBack: () -> Unit) {
    GamerHeader(
        title = "Трек",
        subtitle = "Программа ещё не сформирована. Заверши onboarding или обнови данные профиля.",
    )
    GamerPanel {
        Text(
            text = stringResource(R.string.training_no_exercises),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GamerSecondaryButton(text = "Назад", onClick = onBack)
    }
}

@Composable
private fun TrackContent(
    exercises: List<CatalogExercise>,
    onExerciseClick: (CatalogExercise) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GamerHeader(
                title = "Трек",
                subtitle = "Один понятный маршрут на неделю: что делать сегодня, что будет дальше и зачем это нужно.",
            )
        }
        item { TrackHero(exercises) }
        item { WeekRoadmap() }
        item { TodayFocus(exercises.first()) }
        item { SectionTitle("Ближайшие упражнения") }
        items(exercises) { exercise ->
            ExerciseCard(exercise = exercise, onClick = { onExerciseClick(exercise) })
        }
        item { TrackHint() }
        item { GamerSecondaryButton(text = "Назад", onClick = onBack) }
    }
}

@Composable
private fun TrackHero(exercises: List<CatalogExercise>) {
    val totalMinutes = exercises.sumOf { it.estimatedMinutes }.coerceAtLeast(20)

    GamerPanel(accent = GamerAccentLime) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "CS2 Foundation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Фокус недели — стабильная механика: меньше хаоса, больше повторяемых действий в матче.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GamerChip(text = "$totalMinutes мин")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "Неделя 1", accent = GamerAccentLime)
            GamerChip(text = "${exercises.size} задач")
            GamerChip(text = "Aim + Movement", accent = GamerAccentPink)
        }
    }
}

@Composable
private fun WeekRoadmap() {
    GamerPanel(accent = GamerAccentOrange) {
        Text(
            text = "План недели",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DayChip(label = "Пн", status = "готово", modifier = Modifier.weight(1f))
            DayChip(label = "Вт", status = "сегодня", accent = GamerAccentLime, modifier = Modifier.weight(1f))
            DayChip(label = "Ср", status = "план", modifier = Modifier.weight(1f))
            DayChip(label = "Чт", status = "план", modifier = Modifier.weight(1f))
        }
        Text(
            text = "Не нужно искать, что нажимать: начни с сегодняшнего фокуса, а остальные упражнения открывай по желанию.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DayChip(
    label: String,
    status: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GamerChip(text = label, accent = accent)
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TodayFocus(exercise: CatalogExercise) {
    GamerPanel(accent = GamerAccentPink) {
        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = exercise.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "${exercise.estimatedMinutes} мин")
            GamerChip(text = "${exercise.baseXp} XP", accent = GamerAccentLime)
            GamerChip(text = "открыть ниже")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Composable
private fun ExerciseCard(
    exercise: CatalogExercise,
    onClick: () -> Unit,
) {
    GamerPanel(
        modifier = Modifier.clickable(onClick = onClick),
        accent = GamerAccentOrange,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            GamerChip(text = "открыть", accent = GamerAccentLime)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "${exercise.estimatedMinutes} мин")
            GamerChip(text = "${exercise.baseXp} XP", accent = GamerAccentLime)
            GamerChip(text = exercise.resultType.ifBlank { "manual" })
        }
    }
}

@Composable
private fun TrackHint() {
    GamerPanel {
        Text(
            text = "Как пользоваться треком",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Список теперь скроллится одним полотном. Верхние блоки не зажимают упражнения, а карточка «Сегодня» сразу показывает главный фокус.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExerciseDetailSheet(
    exercise: CatalogExercise,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GamerPanel {
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
            if (exercise.instructions.isNotBlank()) {
                Text(
                    text = "Инструкция",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = exercise.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GamerChip(text = "${exercise.estimatedMinutes} мин")
                GamerChip(text = "${exercise.baseXp} XP", accent = GamerAccentLime)
            }
            GamerPrimaryButton(text = "Приступить", onClick = onStart)
        }
    }
}
