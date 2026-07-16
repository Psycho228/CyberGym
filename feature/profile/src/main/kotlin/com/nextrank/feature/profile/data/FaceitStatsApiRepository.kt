package com.nextrank.feature.profile.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.core.network.faceit.FaceitConfig
import com.nextrank.feature.profile.domain.FaceitProfileStats
import com.nextrank.feature.profile.domain.FaceitStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

private const val FACEIT_BASE_URL = "https://open.faceit.com/data/v4"
private const val CONNECT_TIMEOUT_MILLIS = 10_000
private const val READ_TIMEOUT_MILLIS = 10_000
private const val CS2_GAME_ID = "cs2"

class FaceitStatsApiRepository(
    private val config: FaceitConfig,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : FaceitStatsRepository {

    override suspend fun loadStats(playerId: String): Result<FaceitProfileStats> = runCatching {
        require(config.apiKey.isNotBlank()) { "FACEIT API key is not configured" }
        require(playerId.isNotBlank()) { "FACEIT player id is empty" }

        val response = get("$FACEIT_BASE_URL/players/$playerId/stats/$CS2_GAME_ID")
        val dto = json.decodeFromString<FaceitStatsDto>(response)
        dto.toDomain()
    }.fold({ Result.Success(it) }, { Result.Failure(it.toAppError()) })

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
        }

        try {
            val code = connection.responseCode
            val body = if (code in HTTP_SUCCESS_RANGE) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            check(code in HTTP_SUCCESS_RANGE) { "FACEIT stats request failed: HTTP $code $body" }
            body
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        val HTTP_SUCCESS_RANGE = 200..299
    }
}

@Serializable
private data class FaceitStatsDto(
    @SerialName("player_id") val playerId: String? = null,
    @SerialName("game_id") val gameId: String? = null,
    val lifetime: JsonObject = JsonObject(emptyMap()),
)

private fun FaceitStatsDto.toDomain(): FaceitProfileStats =
    FaceitProfileStats(
        playerId = playerId,
        nickname = null,
        avatar = null,
        country = null,
        faceitUrl = null,
        skillLevel = null,
        faceitElo = null,
        gamePlayerId = null,
        matches = lifetime.intMetric("Matches", "Total Matches", "Matches Played"),
        winRate = lifetime.stringMetric("Win Rate %", "Win Rate", "Winrate", "Winrate %").asPercent(),
        averageKd = lifetime.stringMetric("Average K/D Ratio", "K/D Ratio", "Average K/D", "K/D"),
        headshots = lifetime.stringMetric("Average Headshots %", "Headshots %", "Total Headshots %").asPercent(),
    )

private fun JsonObject.stringMetric(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key -> get(key).metricString() }

private fun JsonObject.intMetric(vararg keys: String): Int? =
    keys.firstNotNullOfOrNull { key -> get(key).metricInt() }

private fun JsonElement?.metricString(): String? =
    this?.jsonPrimitive
        ?.contentOrNull
        ?.trim()
        ?.takeIf(String::isNotBlank)

private fun JsonElement?.metricInt(): Int? =
    this?.jsonPrimitive?.intOrNull
        ?: metricString()?.toDoubleOrNull()?.toInt()

private fun String?.asPercent(): String? =
    this?.let { value ->
        if (value.endsWith("%")) value else "$value%"
    }
