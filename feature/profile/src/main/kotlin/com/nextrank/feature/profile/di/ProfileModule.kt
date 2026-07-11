package com.nextrank.feature.profile.di

import com.nextrank.feature.profile.data.SupabaseProfileRepository
import com.nextrank.feature.profile.domain.ProfileRepository
import com.nextrank.feature.profile.presentation.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { SupabaseProfileRepository(get()) }
    viewModel { ProfileViewModel(get()) }
}