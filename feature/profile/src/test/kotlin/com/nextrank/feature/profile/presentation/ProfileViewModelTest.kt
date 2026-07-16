package com.nextrank.feature.profile.presentation

import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.profile.domain.FaceitProfileStats
import com.nextrank.feature.profile.domain.FaceitStatsRepository
import com.nextrank.feature.profile.domain.ProfileData
import com.nextrank.feature.profile.domain.ProfileRepository
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

class ProfileViewModelTest {

    private val repository: ProfileRepository = mockk()
    private val faceitStatsRepository: FaceitStatsRepository = mockk(relaxed = true)
    private val dispatcher = UnconfinedTestDispatcher()
    private val profileData = ProfileData(
        nickname = "ProGamer",
        currentRank = "gold_nova_i",
        primaryGoal = "aim",
        dailyMinutes = 20,
        level = 5,
        totalXp = 1200,
        currentStreak = 7,
        longestStreak = 10,
        faceit = FaceitProfileStats(
            playerId = "faceit-player-id",
            nickname = "ProGamer",
            avatar = null,
            country = "RU",
            faceitUrl = null,
            skillLevel = 8,
            faceitElo = 2100,
            gamePlayerId = "steam-id",
        ),
        favoriteMaps = listOf("MIRAGE", "ANCIENT"),
        weakSpots = listOf("AIM"),
        trainingFrequencyDays = 4,
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
    fun `loadProfile success updates state`() = runTest(dispatcher) {
        coEvery { repository.loadProfile() } returns Result.Success(profileData)

        val viewModel = ProfileViewModel(repository, faceitStatsRepository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("ProGamer", state.nickname)
        assertEquals(5, state.level)
        assertEquals(1200, state.totalXp)
        assertEquals(7, state.currentStreak)
        assertEquals(10, state.longestStreak)
        assertEquals(2100, state.faceit?.faceitElo)
        assertEquals(listOf("MIRAGE", "ANCIENT"), state.favoriteMaps)
        assertEquals(4, state.trainingFrequencyDays)
    }

    @Test
    fun `loadProfile failure shows error`() = runTest(dispatcher) {
        coEvery { repository.loadProfile() } returns Result.Failure(AppError.Network(null, null))

        val viewModel = ProfileViewModel(repository, faceitStatsRepository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.errorMessage != null)
    }
}
