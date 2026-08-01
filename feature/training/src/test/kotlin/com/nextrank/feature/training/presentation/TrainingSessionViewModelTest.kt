package com.nextrank.feature.training.presentation

import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.training.domain.TrainingCompletion
import com.nextrank.feature.training.domain.TrainingExercise
import com.nextrank.feature.training.domain.TrainingRepository
import com.nextrank.feature.training.domain.TrainingResultSubmission
import com.nextrank.feature.training.domain.TrainingSession
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrainingSessionViewModelTest {

    private val repository: TrainingRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val session = TrainingSession(
        sessionId = "session-1",
        planTitle = "Ежедневная база",
        exercises = listOf(
            TrainingExercise(
                "item-1",
                "ex-1",
                "warmup_flicks",
                "Флики",
                "Разминка",
                "Делай флики",
                "timer",
                5,
                30,
            ),
            TrainingExercise(
                "item-2",
                "ex-2",
                "aim_headshots",
                "Хедшоты",
                "Aim",
                "50 хедшотов",
                "repetitions",
                8,
                50,
            ),
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
    fun `load success populates exercises and sessionId`() = runTest(dispatcher) {
        coEvery { repository.startOrResume("plan-1") } returns Result.Success(session)

        val viewModel = TrainingSessionViewModel(repository)
        viewModel.load("plan-1")

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("session-1", state.sessionId)
        assertEquals("Ежедневная база", state.planTitle)
        assertEquals(2, state.exercises.size)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `completeCurrent advances index on non-last exercise`() = runTest(dispatcher) {
        coEvery { repository.startOrResume("plan-1") } returns Result.Success(session)

        val viewModel = TrainingSessionViewModel(repository)
        viewModel.load("plan-1")
        viewModel.completeCurrent()

        assertEquals(1, viewModel.uiState.value.currentIndex)
    }

    @Test
    fun `recognized result on last exercise completes session`() = runTest(dispatcher) {
        coEvery { repository.startOrResume("plan-1") } returns Result.Success(session)
        coEvery {
            repository.complete("session-1", any<List<TrainingResultSubmission>>(), any())
        } returns Result.Success(TrainingCompletion(80, 580, 3, 1))

        val viewModel = TrainingSessionViewModel(repository)
        viewModel.load("plan-1")
        viewModel.completeCurrent()
        viewModel.acceptRecognizedText(
            """
            CYBERGYM RESULT V1
            RUN RUN-TEST
            MAP CYBERGYM_TRAINING_HUB
            EX WARMUP ATTEMPTS 40 HITS 30
            EX AIM50 ATTEMPTS 70 HITS 50 ACCURACY 82.5
            END
            """.trimIndent(),
        )
        viewModel.confirmResults()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(false, state.isCompleting)
    }

    @Test
    fun `load failure shows error`() = runTest(dispatcher) {
        coEvery { repository.startOrResume("plan-1") } returns Result.Failure(AppError.Network(null, null))

        val viewModel = TrainingSessionViewModel(repository)
        viewModel.load("plan-1")

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.errorMessage != null)
    }

    @Test
    fun `invalid text keeps scanner open`() = runTest(dispatcher) {
        coEvery { repository.startOrResume("plan-1") } returns Result.Success(session)

        val viewModel = TrainingSessionViewModel(repository)
        viewModel.load("plan-1")
        viewModel.beginResultScan()

        val accepted = viewModel.acceptRecognizedText("NOT A CYBERGYM RESULT")

        assertFalse(accepted)
        assertTrue(viewModel.uiState.value.isScanningResult)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }
}
