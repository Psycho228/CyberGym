plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.nextrank.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.core)
    // Supabase SDK — требуется настройка репозитория (maven { url = "..." })
    api(libs.supabase.client)
    api(libs.supabase.auth)
    api(libs.supabase.postgrest)
    implementation(libs.ktor.client.okhttp)
    implementation(project(":core:common"))
}
