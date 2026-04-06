package com.keshav.coldplayremote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.annotation.NavGraph

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavGraphs.main.route) {
        mainNavigation(navController)
    }
}

fun NavGraphBuilder.mainNavigation(navController: NavController) {
    navigation(startDestination = HomePageDestination.route, route = NavGraphs.main.root) {
        composable(HomePageDestination) {
            HomePage(destinationsNavigator(navController))
        }
        composable(IRRemotePageDestination) {
            IRRemotePage(destinationsNavigator(navController))
        }

    }
}

@NavGraph
annotation class MainNavGraph(
    val start: Boolean = false
)
