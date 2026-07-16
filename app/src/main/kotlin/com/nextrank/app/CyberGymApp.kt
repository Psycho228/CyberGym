package com.nextrank.app

import android.app.Application
import com.nextrank.app.di.SupabaseConfig
import com.nextrank.app.di.initKoin
import com.nextrank.core.network.faceit.FaceitConfig

class CyberGymApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val supabaseConfig = SupabaseConfig(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY,
        )
        val faceitConfig = FaceitConfig(
            apiKey = BuildConfig.FACEIT_API_KEY,
        )

        initKoin(this, supabaseConfig, faceitConfig)
    }
}
