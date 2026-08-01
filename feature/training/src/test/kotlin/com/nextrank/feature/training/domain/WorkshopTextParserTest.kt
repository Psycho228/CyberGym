package com.nextrank.feature.training.domain

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WorkshopTextParserTest {

    private val resultText =
        """
        CYBERGYM RESULT V1
        RUN RUN-42
        MAP CYBERGYM_TRAINING_HUB
        EX STRAFE50 ATTEMPTS 50 STOPS 43 ACCURACY 86.5
        END
        """.trimIndent()

    @Test
    fun `parses workshop result text`() {
        val result = WorkshopTextParser.parse(resultText, setOf("counter_strafe"))

        assertEquals("cybergym_training_hub", result.mapName)
        assertEquals("RUN-42", result.runId)
        assertEquals("50", result.exercises.single().metrics["attempts"])
        assertEquals("86.5", result.exercises.single().metrics["accuracy"])
    }

    @Test
    fun `accepts OCR friendly delimiters and decimal comma`() {
        val result = WorkshopTextParser.parse(
            """
            CYBERGYM | RESULT | V1
            RUN: RUN-99
            MAP = CYBERGYM_TRAINING_HUB
            EX: AIM50 | ATT 70 | HITS 51 | ACC 72,9
            END
            """.trimIndent(),
            setOf("aim_headshots"),
        )

        assertEquals("72.9", result.exercises.single().metrics["accuracy"])
        assertTrue(WorkshopTextParser.looksComplete(resultText))
    }

    @Test
    fun `rejects result for another exercise`() {
        assertFailsWith<IllegalArgumentException> {
            WorkshopTextParser.parse(resultText, setOf("aim_headshots"))
        }
    }

    @Test
    fun `rejects cropped result without end marker`() {
        assertFailsWith<IllegalArgumentException> {
            WorkshopTextParser.parse(resultText.removeSuffix("\nEND"), setOf("counter_strafe"))
        }
    }

    @Test
    fun `parses all sample training shapes`() {
        val samples = listOf(
            Sample(
                exercises = setOf("warmup_flicks", "aim_headshots", "counter_strafe"),
                text = """
                    CYBERGYM RESULT V1
                    RUN DEMO-DAILY-001
                    MAP CYBERGYM_TRAINING_HUB
                    EX WARMUP ATT 40 HITS 31 ACC 77.5 SCORE 74
                    EX AIM50 ATT 70 HITS 51 HS 46 ACC 72.9 SCORE 78
                    EX STRAFE50 ATT 50 STOPS 42 HITS 39 SPEED 19.4 SCORE 83
                    END
                """.trimIndent(),
            ),
            Sample(setOf("warmup_flicks"), sampleLine("WARMUP ATT 40 HITS 34 ACC 85 SCORE 82")),
            Sample(setOf("aim_headshots"), sampleLine("AIM50 ATT 70 HITS 55 HS 48 ACC 78.6 SCORE 84")),
            Sample(setOf("ak_spray"), sampleLine("SPRAY5 ATT 150 HITS 119 ACC 79.3 SCORE 81")),
            Sample(
                setOf("counter_strafe"),
                sampleLine("STRAFE50 ATT 50 STOPS 44 HITS 41 SPEED 18.7 SCORE 87"),
            ),
        )

        samples.forEach { sample ->
            val result = WorkshopTextParser.parse(sample.text, sample.exercises)
            assertEquals(sample.exercises, result.exercises.map { it.exerciseSlug }.toSet())
        }
    }

    private fun sampleLine(exerciseResult: String): String =
        """
        CYBERGYM RESULT V1
        RUN DEMO-SINGLE-001
        MAP CYBERGYM_TRAINING_HUB
        EX $exerciseResult
        END
        """.trimIndent()

    private data class Sample(
        val exercises: Set<String>,
        val text: String,
    )
}
