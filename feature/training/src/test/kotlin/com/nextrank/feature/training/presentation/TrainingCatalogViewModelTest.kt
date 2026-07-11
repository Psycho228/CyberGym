package com.nextrank.feature.training.presentation

import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.training.domain.CatalogExercise
import com.nextrank.feature.training.domain.TrainingRepository
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

class TrainingCatalogViewModelTest {

    private val repository: TrainingRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val exercises = listOf(
        CatalogExercise(
            id = "1",
            slug = "aim_headshots",
            title = "Только хедшоты",
            description = "Тренировка точности",
            estimatedMinutes = 8,
            baseXp = 50,
        ),
        CatalogExercise(
            id = "2",
            slug = "ak_spray",
            title = "Контроль AK-47",
            description = "Закрепление spray pattern",
            estimatedMinutes = 6,
            baseXp = 40,
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
    fun `loadExercises success updates state with exercises`() = runTest(dispatcher) {
        coEvery { repository.loadAllExercises() } returns Result.Success(exercises)

        val viewModel = TrainingCatalogViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(2, state.exercises.size)
        assertEquals("Только хедшоты", state.exercises[0].title)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadExercises failure shows error`() = runTest(dispatcher) {
        coEvery { repository.loadAllExercises() } returns Result.Failure(AppError.Network(null, null))

        val viewModel = TrainingCatalogViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.errorMessage != null)
    }
}