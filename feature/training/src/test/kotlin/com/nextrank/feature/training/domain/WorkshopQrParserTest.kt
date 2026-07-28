package com.nextrank.feature.training.domain

import org.junit.Test
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkshopQrParserTest {

    private val json =
        """
        {
          "v": 1,
          "source": "cybergym_workshop",
          "map": "cybergym_training_hub",
          "run_id": "run-42",
          "completed_at": "2026-07-28T12:00:00Z",
          "results": [
            {
              "exercise": "counter_strafe",
              "metrics": {
                "attempts": 50,
                "successful_stops": 43,
                "accuracy": 86.5
              }
            }
          ]
        }
        """.trimIndent()

    @Test
    fun `parses raw workshop JSON`() {
        val result = WorkshopQrParser.parse(json, setOf("counter_strafe"))

        assertEquals("cybergym_training_hub", result.mapName)
        assertEquals("run-42", result.runId)
        assertEquals("50", result.exercises.single().metrics["attempts"])
        assertEquals("86.5", result.exercises.single().metrics["accuracy"])
    }

    @Test
    fun `parses base64url CyberGym payload`() {
        val encoded = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toByteArray(Charsets.UTF_8))

        val result = WorkshopQrParser.parse(
            "$WORKSHOP_QR_PREFIX$encoded",
            setOf("counter_strafe"),
        )

        assertEquals("counter_strafe", result.exercises.single().exerciseSlug)
    }

    @Test
    fun `rejects result for another exercise`() {
        assertFailsWith<IllegalArgumentException> {
            WorkshopQrParser.parse(json, setOf("aim_headshots"))
        }
    }
}
