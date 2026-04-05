package com.ndrive.cloudvault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ndrive.cloudvault.presentation.auth.LoginScreen
import com.ndrive.cloudvault.presentation.auth.SignupScreen
import com.ndrive.cloudvault.presentation.home.HomeScreen

@Composable
fun NDriveNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToHome = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignupScreen(
                onNavigateToHome = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onNavigateToLogin = { navController.navigate("login") { popUpTo("signup") { inclusive = true } } }
            )
        }
        composable("home") {
            HomeScreen()
        }
    }
}
