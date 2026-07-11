package com.nextrank.feature.training.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.nextrank.feature.training.R
import com.nextrank.feature.training.domain.CatalogExercise

@Composable
fun TrainingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainingCatalogViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.training_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                )
                OutlinedButton(
                    onClick = viewModel::loadExercises,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Повторить") }
            }
            uiState.exercises.isEmpty() -> {
                Text(
                    text = stringResource(R.string.training_no_exercises),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.exercises) { exercise ->
                        ExerciseCard(exercise)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Назад") }
    }
}

@Composable
private fun ExerciseCard(exercise: CatalogExercise) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = exercise.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${stringResource(R.string.training_minutes_format, exercise.estimatedMinutes)} • " +
                    stringResource(R.string.training_xp_format, exercise.baseXp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}