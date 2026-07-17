package com.nextrank.feature.onboarding.data

import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.onboarding.domain.FaceitPlayer
import com.nextrank.feature.onboarding.domain.FaceitRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

private const val CONNECT_TIMEOUT_MILLIS = 10_000
private const val READ_TIMEOUT_MILLIS = 10_000

class FaceitApiRepository(
    private val supabaseClient: SupabaseClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : FaceitRepository {

    override suspend fun findPlayer(nickname: String): Result<FaceitPlayer> = runCatching {
        val encodedNickname = URLEncoder.encode(nickname.trim(), Charsets.UTF_8.name())
        val response = get("${supabaseClient.functionsBaseUrl()}/faceit-player?nickname=$encodedNickname")
        val dto = json.decodeFromString<FaceitPlayerDto>(response)
        dto.toDomain()
    }.fold({ Result.Success(it) }, { Result.Failure(it.toAppError()) })

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            setRequestProperty("Accept", "application/json")
            setRequestProperty(
                "Authorization",
                "Bearer ${supabaseClient.auth.currentAccessTokenOrNull() ?: error("No active Supabase session")}",
            )
        }

        try {
            val code = connection.responseCode
            val body = if (code in HTTP_SUCCESS_RANGE) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            check(code in HTTP_SUCCESS_RANGE) { "FACEIT request failed: HTTP $code $body" }
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
private data class FaceitPlayerDto(
    @SerialName("player_id") val playerId: String,
    val nickname: String,
    val avatar: String? = null,
    val country: String? = null,
    @SerialName("faceit_url") val faceitUrl: String? = null,
    val games: Map<String, FaceitGameDetailDto> = emptyMap(),
)

@Serializable
private data class FaceitGameDetailDto(
    @SerialName("faceit_elo") val faceitElo: Int? = null,
    @SerialName("game_player_id") val gamePlayerId: String? = null,
    @SerialName("skill_level") val skillLevel: Int? = null,
)

private fun FaceitPlayerDto.toDomain(): FaceitPlayer {
    val game = games["cs2"] ?: games["csgo"]
    val gameKey = when {
        games.containsKey("cs2") -> "cs2"
        games.containsKey("csgo") -> "csgo"
        else -> "unknown"
    }

    return FaceitPlayer(
        playerId = playerId,
        nickname = nickname,
        avatar = avatar,
        country = country,
        faceitUrl = faceitUrl,
        game = gameKey,
        gamePlayerId = game?.gamePlayerId,
        skillLevel = game?.skillLevel,
        faceitElo = game?.faceitElo,
    )
}

private fun SupabaseClient.functionsBaseUrl(): String {
    val baseUrl = supabaseUrl.trimEnd('/')
    val normalizedBaseUrl = if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
        baseUrl
    } else {
        "https://$baseUrl"
    }
    return "$normalizedBaseUrl/functions/v1"
}
