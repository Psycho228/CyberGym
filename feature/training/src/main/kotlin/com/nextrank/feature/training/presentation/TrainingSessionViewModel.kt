package com.nextrank.feature.training.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.training.domain.TrainingExercise
import com.nextrank.feature.training.domain.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@Immutable
data class TrainingSessionUiState(
    val isLoading: Boolean = true,
    val planTitle: String = "",
    val sessionId: String? = null,
    val exercises: List<TrainingExercise> = emptyList(),
    val currentIndex: Int = 0,
    val errorMessage: String? = null,
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
            when (val result = repository.startOrResume(planId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, planTitle = result.data.planTitle,
                        sessionId = result.data.sessionId, exercises = result.data.exercises)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не удалось открыть тренировку")
                }
            }
        }
    }

    fun completeCurrent() {
        val state = _uiState.value
        if (state.currentIndex < state.exercises.lastIndex) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
            return
        }
        val sessionId = state.sessionId ?: return
        if (state.isCompleting || state.isComplete) return
        _uiState.update { it.copy(isCompleting = true, errorMessage = null) }
        viewModelScope.launch {
            when (repository.complete(sessionId, state.exercises.map { it.itemId }, completionKey)) {
                is Result.Success -> _uiState.update { it.copy(isCompleting = false, isComplete = true) }
                is Result.Failure -> _uiState.update { it.copy(
                    isCompleting = false,
                    errorMessage = "Не удалось сохранить тренировку. Попробуйте ещё раз.",
                ) }
            }
        }
    }
}
