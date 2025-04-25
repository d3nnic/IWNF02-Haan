package com.dd.sfa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dd.sfa.ui.screens.home.CustomExerciseScreen
import com.dd.sfa.ui.screens.home.EditExerciseScreen
import com.dd.sfa.ui.screens.home.ExerciseSelectionScreen
import com.dd.sfa.ui.screens.login.WelcomeScreen
import com.dd.sfa.ui.screens.home.HomeScreen
import com.dd.sfa.ui.screens.home.PlanDetailScreen
import com.dd.sfa.ui.screens.login.LoginScreen
import com.dd.sfa.ui.screens.login.RegisterScreen
import com.dd.sfa.ui.screens.settings.SettingsScreen
import com.dd.sfa.viewmodels.AuthState
import com.dd.sfa.viewmodels.AuthViewModel
import com.dd.sfa.viewmodels.DataViewModel
import com.dd.sfa.viewmodels.SettingsViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    dataViewModel: DataViewModel
) {
    // create NavController to manage navigation stack.
    val navController = rememberNavController()

    // observe authentication state to choose start destination dynamically.
    val authState by authViewModel.authState.observeAsState()

    // if authenticated, start at home; otherwise show welcome screen.
    val startDestination = if (authState is AuthState.Authenticated) "home" else "welcome"

    // define the navigation graph for all app destinations.
    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            // entry point offering login or registration.
            WelcomeScreen(navController = navController)
        }
        composable("login") {
            // screen for existing users to log in.
            LoginScreen(modifier, authViewModel, navController)
        }
        composable("register") {
            // screen for new user registration.
            RegisterScreen(modifier, authViewModel, navController)
        }
        composable("home") {
            // main screen showing list of training plans.
            HomeScreen(modifier, authViewModel, dataViewModel, navController)
        }
        composable("settings") {
            // settings screen for toggles and account management.
            SettingsScreen(modifier, authViewModel, navController, settingsViewModel, dataViewModel)
        }
        composable("planDetail/{planId}") { backStackEntry ->
            // show details and exercises for the selected training plan.
            backStackEntry.arguments?.getString("planId")?.let { planId ->
                PlanDetailScreen(planId, navController, dataViewModel, authViewModel)
            }
        }
        composable("exerciseSelection/{planId}") { backStackEntry ->
            // let user select exercises to add to a plan.
            backStackEntry.arguments?.getString("planId")?.let { planId ->
                ExerciseSelectionScreen(planId, navController, dataViewModel, authViewModel)
            }
        }
        composable("customExercise") {
            // screen to create a new custom exercise.
            CustomExerciseScreen(navController = navController, dataViewModel = dataViewModel, authViewModel = authViewModel)
        }
        composable("editExercise/{planId}/{exerciseId}") { backStackEntry ->
            // screen to edit existing exercise details.
            val planId = backStackEntry.arguments?.getString("planId")
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            if (planId != null && exerciseId != null) {
                EditExerciseScreen(navController, dataViewModel, authViewModel, planId, exerciseId)
            }
        }
    }
}