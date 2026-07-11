package com.nextrank.feature.home.presentation

import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.home.domain.HomeRepository
import com.nextrank.feature.home.domain.HomeSnapshot
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest {

    private val repository: HomeRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val snapshot = HomeSnapshot(
        nickname = "TestPlayer",
        level = 3,
        totalXp = 500,
        streak = 5,
        planId = "plan-123",
        exerciseCount = 3,
        estimatedMinutes = 18,
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
    fun `loadHomeData success updates state with data`() = runTest(dispatcher) {
        coEvery { repository.loadHome() } returns Result.Success(snapshot)

        val viewModel = HomeViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("TestPlayer", state.nickname)
        assertEquals(3, state.level)
        assertEquals(500, state.totalXp)
        assertEquals(5, state.streak)
        assertEquals("plan-123", state.planId)
        assertEquals(3, state.exerciseCount)
        assertEquals(18, state.estimatedMinutes)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadHomeData failure shows error message`() = runTest(dispatcher) {
        coEvery { repository.loadHome() } returns Result.Failure(AppError.Network(null, null))

        val viewModel = HomeViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.errorMessage != null)
    }
}