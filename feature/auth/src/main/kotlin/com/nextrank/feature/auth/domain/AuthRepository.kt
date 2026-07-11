package com.nextrank.feature.auth.domain

import com.nextrank.core.common.result.Result

/**
 * Repository авторизации.
 * Скрывает детали Supabase Auth за интерфейсом.
 */
interface AuthRepository {
    /**
     * Регистрация по email/password.
     * Возвращает userId при успехе.
     */
    suspend fun register(email: String, password: String): Result<String>

    /**
     * Вход по email/password.
     * Возвращает userId при успехе.
     */
    suspend fun login(email: String, password: String): Result<String>

    /**
     * Выход.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Проверка активной сессии.
     */
    suspend fun isSessionActive(): Result<Boolean>
}
