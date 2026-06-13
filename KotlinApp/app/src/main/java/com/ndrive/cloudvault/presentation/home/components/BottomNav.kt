package com.ndrive.cloudvault.presentation.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Static data for bottom nav items. Declared outside the composable
 * so the lists are created once at class-load time, never during composition.
 */
private data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", "home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Starred", "starred", Icons.Filled.Star, Icons.Outlined.StarBorder),
    BottomNavItem("Photos", "photos", Icons.Filled.Photo, Icons.Outlined.Photo),
    BottomNavItem("Files", "files", Icons.Filled.Folder, Icons.Outlined.Folder),
)

@Composable
fun NDriveBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // derivedStateOf ensures we only recompose when the effective route
    // actually changes, not on every back-stack entry emission.
    val effectiveRoute by remember {
        derivedStateOf {
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute?.startsWith("folder") == true) "files" else currentRoute
        }
    }

    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = effectiveRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = { 
                    if (effectiveRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
