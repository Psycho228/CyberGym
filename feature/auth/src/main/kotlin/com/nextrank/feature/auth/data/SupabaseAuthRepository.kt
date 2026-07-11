package com.nextrank.feature.auth.data

import com.nextrank.core.common.error.AppError
import com.nextrank.core.common.error.toAppError
import com.nextrank.core.common.result.Result
import com.nextrank.feature.auth.domain.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class SupabaseAuthRepository(
    private val supabaseClient: SupabaseClient,
) : AuthRepository {

    override suspend fun register(email: String, password: String): Result<String> = runCatching {
        val authResult = supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val userId = authResult?.id ?: supabaseClient.auth.currentUserOrNull()?.id
            ?: throw AppError.Auth(null, "Подтвердите email, чтобы завершить регистрацию")
        Result.Success(userId)
    }.getOrElse { Result.Failure(it.toAppError()) }

    override suspend fun login(email: String, password: String): Result<String> = runCatching {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw AppError.Auth(null, "Не удалось получить пользователя")
        Result.Success(userId)
    }.getOrElse { Result.Failure(it.toAppError()) }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
        Result.Success(Unit)
    }.getOrElse { Result.Failure(it.toAppError()) }

    override suspend fun isSessionActive(): Result<Boolean> =
        Result.Success(supabaseClient.auth.currentSessionOrNull() != null)
}
