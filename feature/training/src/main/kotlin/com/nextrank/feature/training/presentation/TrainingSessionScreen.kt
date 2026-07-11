package com.nextrank.feature.training.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(state.planTitle.ifBlank { "Тренировка" }, style = MaterialTheme.typography.headlineMedium)
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            state.errorMessage != null && state.exercises.isEmpty() -> {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
            }
            state.exercises.isNotEmpty() -> {
                val exercise = state.exercises[state.currentIndex]
                Text("${state.currentIndex + 1} из ${state.exercises.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { (state.currentIndex + 1f) / state.exercises.size },
                    modifier = Modifier.fillMaxWidth(),
                )
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(exercise.title, style = MaterialTheme.typography.titleLarge)
                        Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
                        Text(exercise.instructions, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${exercise.estimatedMinutes} мин • ${exercise.baseXp} XP",
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = viewModel::completeCurrent,
                    enabled = !state.isCompleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (state.isCompleting) "Сохранение…"
                        else if (state.currentIndex == state.exercises.lastIndex) "Завершить тренировку"
                        else "Упражнение выполнено",
                    )
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Выйти") }
            }
        }
    }
}
