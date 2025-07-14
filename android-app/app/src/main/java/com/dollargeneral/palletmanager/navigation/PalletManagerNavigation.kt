package com.dollargeneral.palletmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dollargeneral.palletmanager.ui.screens.AddAssignmentScreen
import com.dollargeneral.palletmanager.ui.screens.MainScreen
import com.dollargeneral.palletmanager.ui.screens.StationDatabaseScreen
import com.dollargeneral.palletmanager.ui.screens.StationLookupScreen

/**
 * Navigation routes for the app
 */
object PalletManagerRoutes {
    const val STATION_LOOKUP = "station_lookup"
    const val MAIN = "main"
    const val ADD_ASSIGNMENT = "add_assignment"
    const val STATION_DATABASE = "station_database"
}

/**
 * Main navigation component for PalletManager app
 */
@Composable
fun PalletManagerNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = PalletManagerRoutes.MAIN
    ) {
        // Station lookup screen - primary interface for check digit lookup
        composable(PalletManagerRoutes.STATION_LOOKUP) {
            StationLookupScreen(
                onNavigateToAssignments = {
                    navController.navigate(PalletManagerRoutes.MAIN)
                },
                onNavigateToDatabase = {
                    navController.navigate(PalletManagerRoutes.STATION_DATABASE)
                }
            )
        }

        // Main screen - shows active pallet assignments
        composable(PalletManagerRoutes.MAIN) {
            MainScreen(
                onNavigateToAddAssignment = {
                    navController.navigate(PalletManagerRoutes.ADD_ASSIGNMENT)
                },
                onNavigateToStationDatabase = {
                    navController.navigate(PalletManagerRoutes.STATION_DATABASE)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Add assignment screen - form for new pallet assignments
        composable(PalletManagerRoutes.ADD_ASSIGNMENT) {
            AddAssignmentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAssignmentSaved = {
                    navController.popBackStack()
                }
            )
        }
        
        // Station database screen - manage check digit database
        composable(PalletManagerRoutes.STATION_DATABASE) {
            StationDatabaseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
