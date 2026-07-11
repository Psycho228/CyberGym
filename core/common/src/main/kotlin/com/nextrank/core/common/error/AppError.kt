package com.nextrank.core.common.error

/**
 * Абстракция ошибок приложения.
 * Каждый тип описывает конкретный сценарий ошибки.
 */
sealed class AppError(
    message: String,
) : RuntimeException(message) {

    /** Ошибка сети */
    data class Network(
        val code: Int?,
        val detail: String?,
    ) : AppError("Network error: $code - $detail")

    /** Ошибка авторизации */
    data class Auth(
        val code: String?,
        val detail: String?,
    ) : AppError("Auth error: $code - $detail")

    /** Ошибка валидации данных */
    data class Validation(
        val field: String?,
        val detail: String,
    ) : AppError("Validation: $field - $detail")

    /** Ошибка сервера (не 2xx/4xx) */
    data class Server(
        val statusCode: Int,
        val detail: String?,
    ) : AppError("Server error: $statusCode - $detail")

    /** Ошибка отсутствия данных */
    object NotFound : AppError("Resource not found")

    /** Ошибка дублирования (уникальность) */
    object Duplicate : AppError("Resource already exists")

    /** Ошибка таймаута */
    object Timeout : AppError("Request timed out")

    /** Ошибка отмены запроса */
    object Cancelled : AppError("Request cancelled")

    /** Ошибка неизвестного происхождения */
    data class Unknown(
        override val message: String = "Unknown error occurred",
    ) : AppError(message)

    /** Ошибка отсутствия подключения к сети */
    object NoNetwork : AppError("No network connection")

    companion object {
        fun from(throwable: Throwable): AppError = when (throwable) {
            is AppError -> throwable
            is java.net.SocketTimeoutException -> Timeout
            is java.net.ConnectException -> NoNetwork
            is javax.net.ssl.SSLException -> Unknown("SSL error: ${throwable.message}")
            else -> Unknown(throwable.message ?: "Unknown error")
        }
    }
}

/**
 * Преобразует Throwable в AppError.
 */
fun Throwable.toAppError(): AppError = AppError.from(this)
