package com.nextrank.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nextrank.core.designsystem.theme.HudLine
import com.nextrank.core.navigation.NavGraph
import com.nextrank.core.navigation.Route
import com.nextrank.feature.auth.presentation.LoginScreen
import com.nextrank.feature.auth.presentation.RegisterScreen
import com.nextrank.feature.home.presentation.HomeScreen
import com.nextrank.feature.onboarding.presentation.OnboardingScreen
import com.nextrank.feature.profile.presentation.ProfileScreen
import com.nextrank.feature.progress.presentation.ProgressScreen
import com.nextrank.feature.training.presentation.TrainingScreen
import com.nextrank.feature.training.presentation.TrainingSessionScreen
import org.koin.androidx.compose.koinViewModel

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(NavGraph.HOME, "Сегодня", Icons.Default.Home),
    BottomNavItem(NavGraph.TRAINING, "Трек", Icons.Default.Route),
    BottomNavItem(NavGraph.PROGRESS, "Прогресс", Icons.Default.BarChart),
    BottomNavItem(NavGraph.PROFILE, "Профиль", Icons.Default.Person),
)

private val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavGraph.SPLASH,
) {
    val sessionViewModel = koinViewModel<SessionViewModel>()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val sessionState by sessionViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sessionState.logoutError) {
        val message = sessionState.logoutError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        sessionViewModel.consumeLogoutError()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                CyberGymBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = { route -> navController.navigateBottomBarRoute(route) },
                )
            }
        },
    ) { innerPadding ->
        CyberGymNavHost(
            navController = navController,
            startDestination = startDestination,
            onLogout = {
                sessionViewModel.logout {
                    navController.navigate(NavGraph.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun CyberGymBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemClick(item.route) },
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

@Composable
private fun CyberGymNavHost(
    navController: NavHostController,
    startDestination: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(NavGraph.SPLASH) {
            StartupRoute(navController = navController)
        }
        composable(NavGraph.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.replaceRoute(NavGraph.LOGIN, NavGraph.SPLASH) },
                onNavigateToRegister = { navController.navigate(NavGraph.REGISTER) },
            )
        }
        composable(NavGraph.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.replaceRoute(NavGraph.REGISTER, NavGraph.ONBOARDING) },
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
        composable(NavGraph.ONBOARDING) {
            OnboardingScreen(
                onComplete = { navController.replaceRoute(NavGraph.ONBOARDING, NavGraph.HOME) },
            )
        }
        composable(NavGraph.HOME) {
            HomeScreen(
                onNavigateToTraining = { planId -> navController.navigate(Route.TrainingSession.createRoute(planId)) },
                onNavigateToProgress = { navController.navigateSingleTop(NavGraph.PROGRESS) },
                onNavigateToProfile = { navController.navigateSingleTop(NavGraph.PROFILE) },
                onLogout = onLogout,
            )
        }
        composable(
            route = NavGraph.TRAINING_SESSION,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            TrainingSessionScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack(NavGraph.HOME, inclusive = false) },
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
            ProgressScreen(onBack = { navController.popBackStack() })
        }
        composable(NavGraph.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = onLogout,
            )
        }
    }
}

@Composable
private fun StartupRoute(
    navController: NavHostController,
    viewModel: StartupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val targetRoute = when (val currentState = state) {
            StartupState.Checking -> return@LaunchedEffect
            StartupState.Unauthenticated -> NavGraph.LOGIN
            is StartupState.Ready -> when (currentState.target) {
                NavTarget.Home -> NavGraph.HOME
                NavTarget.Onboarding -> NavGraph.ONBOARDING
            }
        }

        navController.navigate(targetRoute) {
            popUpTo(NavGraph.SPLASH) { inclusive = true }
            launchSingleTop = true
        }
    }
}

private fun NavHostController.navigateBottomBarRoute(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

private fun NavHostController.replaceRoute(from: String, to: String) {
    navigate(to) {
        popUpTo(from) { inclusive = true }
    }
}
