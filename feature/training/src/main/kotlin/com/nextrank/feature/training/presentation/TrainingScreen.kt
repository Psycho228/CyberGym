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
        GamerHeader(
            title = stringResource(R.string.training_title),
            subtitle = "Выбирай короткие упражнения под aim, движение, дисциплину и стабильность.",
        )

        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            uiState.errorMessage != null -> {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                GamerSecondaryButton(text = "Повторить", onClick = viewModel::loadExercises)
            }
            uiState.exercises.isEmpty() -> {
                GamerPanel {
                    Text(
                        text = stringResource(R.string.training_no_exercises),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(uiState.exercises) { exercise ->
                        ExerciseCard(exercise = exercise, onClick = { selectedExercise = exercise })
                    }
                }
            }
        }

        GamerSecondaryButton(text = "Назад", onClick = onBack)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            GamerChip(text = "${exercise.baseXp} XP", accent = GamerAccentLime)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "${exercise.estimatedMinutes} мин")
            GamerChip(text = "CS2")
        }
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
