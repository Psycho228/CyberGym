package com.nextrank.app.di

import android.content.Context
import com.nextrank.app.SessionViewModel
import com.nextrank.app.StartupViewModel
import com.nextrank.core.analytics.Analytics
import com.nextrank.core.analytics.AnalyticsNoOp
import com.nextrank.core.common.time.Clock
import com.nextrank.core.common.time.SystemClock
import com.nextrank.core.network.supabase.createSupabaseClient
import com.nextrank.feature.auth.di.authModule
import com.nextrank.feature.home.di.homeModule
import com.nextrank.feature.onboarding.di.onboardingModule
import com.nextrank.feature.profile.di.profileModule
import com.nextrank.feature.progress.di.progressModule
import com.nextrank.feature.training.di.trainingModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

data class SupabaseConfig(
    val url: String,
    val anonKey: String,
)

val appModule = module {
    single<Clock> { SystemClock() }
    single<Analytics> { AnalyticsNoOp() }
    viewModel { SessionViewModel(get()) }
    viewModel { StartupViewModel(get(), get()) }
}

fun initKoin(
    context: Context,
    supabaseConfig: SupabaseConfig,
) {
    startKoin {
        androidLogger()
        androidContext(context)
        modules(
            appModule,
            networkModule(supabaseConfig),
            authModule,
            onboardingModule,
            homeModule,
            trainingModule,
            progressModule,
            profileModule,
        )
    }
}

fun networkModule(config: SupabaseConfig) = module {
    single { createSupabaseClient(config.url, config.anonKey) }
}
