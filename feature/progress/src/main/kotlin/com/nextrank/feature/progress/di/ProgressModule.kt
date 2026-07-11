package com.nextrank.feature.progress.di

import com.nextrank.feature.progress.data.SupabaseProgressRepository
import com.nextrank.feature.progress.domain.ProgressRepository
import com.nextrank.feature.progress.presentation.ProgressViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val progressModule = module {
    single<ProgressRepository> { SupabaseProgressRepository(get()) }
    viewModel { ProgressViewModel(get()) }
}