package com.nextrank.core.common.result

import com.nextrank.core.common.error.AppError

/** Универсальный результат операции. */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()
}

/** Проверяет, успешен ли результат. */
val <T> Result<T>.isSuccess: Boolean
    get() = this is Result.Success

/** Проверяет, является ли результат ошибкой. */
val <T> Result<T>.isFailure: Boolean
    get() = this is Result.Failure

/** Возвращает данные, если Success, иначе null. */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Failure -> null
}

/** Возвращает ошибку, если Failure, иначе null. */
fun <T> Result<T>.exceptionOrNull(): AppError? = when (this) {
    is Result.Success -> null
    is Result.Failure -> error
}

/** Преобразует Success значение функцией map. */
fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> Result.Failure(error)
}

/** Преобразует ошибку функцией mapError. */
fun <T> Result<T>.mapError(transform: (AppError) -> AppError): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Failure(transform(error))
}

/** Выполняет действие при Success. */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> = when (this) {
    is Result.Success -> { action(data); this }
    is Result.Failure -> this
}

/** Выполняет действие при Failure. */
inline fun <T> Result<T>.onFailure(action: (AppError) -> Unit): Result<T> = when (this) {
    is Result.Success -> this
    is Result.Failure -> { action(error); this }
}
