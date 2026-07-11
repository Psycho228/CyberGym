package com.nextrank.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nextrank.core.designsystem.theme.NextRankTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val analytics: com.nextrank.core.analytics.Analytics by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NextRankTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        analytics.track(
            com.nextrank.core.analytics.AnalyticsEvent.SimpleEvent("app_opened")
        )
    }
}