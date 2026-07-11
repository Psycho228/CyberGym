package com.nextrank.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextrank.core.designsystem.theme.HudLine
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nextrank.core.navigation.NavGraph
import com.nextrank.core.navigation.Route
import com.nextrank.feature.auth.presentation.LoginScreen
import com.nextrank.feature.auth.presentation.RegisterScreen
import com.nextrank.feature.home.presentation.HomeScreen
import com.nextrank.feature.onboarding.presentation.OnboardingScreen
import com.nextrank.feature.progress.presentation.ProgressScreen
import com.nextrank.feature.profile.presentation.ProfileScreen
import com.nextrank.feature.training.presentation.TrainingScreen
import com.nextrank.feature.training.presentation.TrainingSessionScreen
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(NavGraph.HOME, "Главная", Icons.Default.Home),
    BottomNavItem(NavGraph.TRAINING, "Упражнения", Icons.Default.FitnessCenter),
    BottomNavItem(NavGraph.PROGRESS, "Прогресс", Icons.Default.BarChart),
    BottomNavItem(NavGraph.PROFILE, "Профиль", Icons.Default.Person),
)

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavGraph.SPLASH,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        NavGraph.HOME,
        NavGraph.TRAINING,
        NavGraph.PROGRESS,
        NavGraph.PROFILE,
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = HudLine,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            composable(NavGraph.SPLASH) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(NavGraph.LOGIN) {
                        popUpTo(NavGraph.SPLASH) { inclusive = true }
                    }
                }
            }

            composable(NavGraph.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavGraph.HOME) {
                            popUpTo(NavGraph.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavGraph.REGISTER)
                    },
                )
            }

            composable(NavGraph.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(NavGraph.ONBOARDING) {
                            popUpTo(NavGraph.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                )
            }

            composable(NavGraph.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(NavGraph.HOME) {
                            popUpTo(NavGraph.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            composable(NavGraph.HOME) {
                HomeScreen(
                    onNavigateToTraining = { planId ->
                        navController.navigate(Route.TrainingSession.createRoute(planId))
                    },
                    onNavigateToProgress = {
                        navController.navigate(NavGraph.PROGRESS) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(NavGraph.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        navController.navigate(NavGraph.LOGIN) {
                            popUpTo(NavGraph.HOME) { inclusive = true }
                        }
                    },
                )
            }

            composable(
                route = NavGraph.TRAINING_SESSION,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId")
                    ?: return@composable
                TrainingSessionScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        navController.popBackStack(NavGraph.HOME, inclusive = false)
                    },
                )
            }

            composable(NavGraph.TRAINING) {
                TrainingScreen(
                    onBack = { navController.popBackStack() },
                    onStartTraining = { exerciseId ->
                        navController.navigate(Route.TrainingSession.createExerciseRoute(exerciseId))
                    },
                )
            }

            composable(NavGraph.PROGRESS) {
                ProgressScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(NavGraph.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(NavGraph.LOGIN) {
                            popUpTo(NavGraph.PROFILE) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
