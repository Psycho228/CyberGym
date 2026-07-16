package com.nextrank.feature.onboarding.di

import com.nextrank.feature.onboarding.data.FaceitApiRepository
import com.nextrank.feature.onboarding.data.SupabaseOnboardingRepository
import com.nextrank.feature.onboarding.domain.FaceitRepository
import com.nextrank.feature.onboarding.domain.OnboardingRepository
import com.nextrank.feature.onboarding.presentation.OnboardingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    single<OnboardingRepository> { SupabaseOnboardingRepository(get()) }
    single<FaceitRepository> { FaceitApiRepository(get()) }
    viewModel { OnboardingViewModel(get(), get()) }
}
