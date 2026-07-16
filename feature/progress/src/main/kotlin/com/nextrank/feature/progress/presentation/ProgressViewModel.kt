package com.nextrank.feature.progress.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.progress.domain.AchievementInfo
import com.nextrank.feature.progress.domain.ProgressRepository
import com.nextrank.feature.progress.domain.SessionHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProgressUiState(
    val isLoading: Boolean = true,
    val level: Int = 1,
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalTrainings: Int = 0,
    val achievements: List<AchievementInfo> = emptyList(),
    val recentSessions: List<SessionHistoryItem> = emptyList(),
    val errorMessage: String? = null,
)

class ProgressViewModel(
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = progressRepository.loadProgress()) {
                is Result.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            level = data.stats.level,
                            totalXp = data.stats.totalXp,
                            currentStreak = data.stats.currentStreak,
                            longestStreak = data.stats.longestStreak,
                            totalTrainings = data.stats.totalTrainings,
                            achievements = data.achievements,
                            recentSessions = data.recentSessions,
                        )
                    }
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не удалось загрузить прогресс")
                }
            }
        }
    }
}
