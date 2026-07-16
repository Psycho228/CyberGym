package com.nextrank.feature.profile.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.profile.domain.FaceitProfileStats
import com.nextrank.feature.profile.domain.FaceitStatsRepository
import com.nextrank.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProfileUiState(
    val isLoading: Boolean = true,
    val nickname: String = "",
    val currentRank: String? = null,
    val primaryGoal: String? = null,
    val dailyMinutes: Int = 0,
    val level: Int = 1,
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val faceit: FaceitProfileStats? = null,
    val favoriteMaps: List<String> = emptyList(),
    val weakSpots: List<String> = emptyList(),
    val trainingFrequencyDays: Int? = null,
    val isFaceitStatsRefreshing: Boolean = false,
    val faceitStatsError: String? = null,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val faceitStatsRepository: FaceitStatsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = profileRepository.loadProfile()) {
                is Result.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nickname = data.nickname,
                            currentRank = data.currentRank,
                            primaryGoal = data.primaryGoal,
                            dailyMinutes = data.dailyMinutes,
                            level = data.level,
                            totalXp = data.totalXp,
                            currentStreak = data.currentStreak,
                            longestStreak = data.longestStreak,
                            faceit = data.faceit,
                            favoriteMaps = data.favoriteMaps,
                            weakSpots = data.weakSpots,
                            trainingFrequencyDays = data.trainingFrequencyDays,
                            faceitStatsError = null,
                        )
                    }
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не удалось загрузить профиль")
                }
            }
        }
    }

    fun refreshFaceitStats() {
        val currentFaceit = _uiState.value.faceit
        val playerId = currentFaceit?.playerId

        if (playerId.isNullOrBlank() || _uiState.value.isFaceitStatsRefreshing) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFaceitStatsRefreshing = true,
                    faceitStatsError = null,
                )
            }

            when (val result = faceitStatsRepository.loadStats(playerId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isFaceitStatsRefreshing = false,
                        faceit = currentFaceit.mergeStats(result.data),
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isFaceitStatsRefreshing = false,
                        faceitStatsError = "Не удалось обновить FACEIT статистику",
                    )
                }
            }
        }
    }
}

private fun FaceitProfileStats.mergeStats(stats: FaceitProfileStats): FaceitProfileStats =
    copy(
        matches = stats.matches ?: matches,
        winRate = stats.winRate ?: winRate,
        averageKd = stats.averageKd ?: averageKd,
        headshots = stats.headshots ?: headshots,
    )
