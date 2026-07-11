package com.nextrank.core.network.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

fun createSupabaseClient(supabaseUrl: String, anonKey: String): SupabaseClient {
    require(supabaseUrl.startsWith("http")) { "Supabase URL is not configured" }
    require(anonKey.isNotBlank() && anonKey != "placeholder-key") {
        "Supabase anon key is not configured"
    }

    return createSupabaseClient(
        supabaseUrl = supabaseUrl.trimEnd('/'),
        supabaseKey = anonKey,
    ) {
        install(Auth)
        install(Postgrest)
    }
}
