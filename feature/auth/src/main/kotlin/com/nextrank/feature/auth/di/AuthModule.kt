package com.nextrank.feature.auth.di

import com.nextrank.feature.auth.data.SupabaseAuthRepository
import com.nextrank.feature.auth.domain.AuthRepository
import com.nextrank.feature.auth.presentation.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    viewModel { AuthViewModel(get(), get()) }
}
