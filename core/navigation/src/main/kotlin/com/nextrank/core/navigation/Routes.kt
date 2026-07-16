package com.nextrank.core.navigation

sealed interface Route {
    data object Splash : Route
    data object Login : Route
    data object Register : Route
    data object Onboarding : Route
    data object Home : Route
    data object Training : Route
    data object TrainingSession : Route {
        fun createRoute(sessionId: String): String = "training_session/$sessionId"
        fun createExerciseRoute(exerciseId: String): String = "training_session/exercise-$exerciseId"
    }
    data object Progress : Route
    data object Profile : Route
}

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
