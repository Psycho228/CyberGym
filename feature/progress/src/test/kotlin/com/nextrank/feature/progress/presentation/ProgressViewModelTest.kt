package com.nextrank.feature.progress.presentation

import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.progress.domain.AchievementInfo
import com.nextrank.feature.progress.domain.ProgressData
import com.nextrank.feature.progress.domain.ProgressRepository
import com.nextrank.feature.progress.domain.ProgressStats
import com.nextrank.feature.progress.domain.SessionHistoryItem
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressViewModelTest {

    private val repository: ProgressRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val progressData = ProgressData(
        stats = ProgressStats(
            level = 4,
            totalXp = 800,
            currentStreak = 3,
            longestStreak = 5,
            totalTrainings = 12,
        ),
        achievements = listOf(
            AchievementInfo("first_training", "Первый шаг", "Заверши первую тренировку", 50, true),
            AchievementInfo("ten_trainings", "Вошёл в ритм", "Заверши 10 тренировок", 150, false),
        ),
        recentSessions = listOf(
            SessionHistoryItem("session-1", 80, "2026-07-10T15:00:00Z"),
        ),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProgress success updates state`() = runTest(dispatcher) {
        coEvery { repository.loadProgress() } returns Result.Success(progressData)

        val viewModel = ProgressViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(4, state.level)
        assertEquals(800, state.totalXp)
        assertEquals(3, state.currentStreak)
        assertEquals(12, state.totalTrainings)
        assertEquals(2, state.achievements.size)
        assertEquals(1, state.recentSessions.size)
    }

    @Test
    fun `loadProgress failure shows error`() = runTest(dispatcher) {
        coEvery { repository.loadProgress() } returns Result.Failure(AppError.Network(null, null))

        val viewModel = ProgressViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.errorMessage != null)
    }
}