package com.nextrank.feature.training.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.training.domain.CatalogExercise
import com.nextrank.feature.training.domain.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class TrainingCatalogUiState(
    val isLoading: Boolean = true,
    val exercises: List<CatalogExercise> = emptyList(),
    val errorMessage: String? = null,
)

class TrainingCatalogViewModel(
    private val repository: TrainingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingCatalogUiState())
    val uiState: StateFlow<TrainingCatalogUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.loadAllExercises()) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, exercises = result.data)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не удалось загрузить трек")
                }
            }
        }
    }
}
