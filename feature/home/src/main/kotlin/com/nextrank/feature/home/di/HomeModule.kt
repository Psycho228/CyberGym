package com.nextrank.feature.home.di

import com.nextrank.feature.home.data.SupabaseHomeRepository
import com.nextrank.feature.home.domain.HomeRepository
import com.nextrank.feature.home.presentation.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    single<HomeRepository> { SupabaseHomeRepository(get()) }
    viewModel { HomeViewModel(get()) }
}
