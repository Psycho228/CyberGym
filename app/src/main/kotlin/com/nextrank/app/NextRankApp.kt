package com.nextrank.app

import android.app.Application
import com.nextrank.app.di.SupabaseConfig
import com.nextrank.app.di.initKoin

class NextRankApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val supabaseConfig = SupabaseConfig(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY,
        )

        initKoin(this, supabaseConfig)
    }
}
