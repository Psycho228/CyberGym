package com.nextrank.feature.training.di

import com.nextrank.feature.training.data.SupabaseTrainingRepository
import com.nextrank.feature.training.domain.TrainingRepository
import com.nextrank.feature.training.presentation.TrainingCatalogViewModel
import com.nextrank.feature.training.presentation.TrainingSessionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val trainingModule = module {
    single<TrainingRepository> { SupabaseTrainingRepository(get()) }
    viewModel { TrainingSessionViewModel(get()) }
    viewModel { TrainingCatalogViewModel(get()) }
}