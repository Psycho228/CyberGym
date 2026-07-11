package com.nextrank.core.navigation

/**
 * Навигационные маршруты приложения.
 */
sealed interface Route {
    object Splash : Route
    object Login : Route
    object Register : Route
    object Onboarding : Route
    object Home : Route
    object Training : Route
    object TrainingSession : Route {
        fun createRoute(sessionId: String): String = "training_session/$sessionId"
    }
    object Progress : Route
    object Profile : Route
}

/**
 * Граф навигации.
 */
object NavGraph {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TRAINING = "training"
    const val TRAINING_SESSION = "training_session/{sessionId}"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"
}
