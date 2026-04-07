package com.keshav.coldplayremote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.keshav.coldplayremote.components.HomePage
import com.keshav.coldplayremote.components.IRRemotePage
import com.keshav.coldplayremote.components.InternalMusicPlayerScreen
import com.keshav.coldplayremote.components.MusicSyncScreen
import com.keshav.coldplayremote.components.NavGraphs
import com.keshav.coldplayremote.components.destinations.HomePageDestination
import com.keshav.coldplayremote.components.destinations.IRRemotePageDestination
import com.keshav.coldplayremote.components.destinations.InternalMusicPlayerScreenDestination
import com.keshav.coldplayremote.components.destinations.MusicSyncScreenDestination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.utils.composable

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavGraphs.main.route) {
        mainNavigation(navController)
    }
}

fun NavGraphBuilder.mainNavigation(navController: NavController) {
    navigation(startDestination = HomePageDestination.route, route = NavGraphs.main.route) {
        composable(HomePageDestination) {
            HomePage(destinationsNavigator(navController))
        }
        composable(IRRemotePageDestination) {
            IRRemotePage(
                destinationsNavigator(navController),
                navArgs.remote
            )
        }
        composable(MusicSyncScreenDestination) {
            MusicSyncScreen()
        }
        composable(InternalMusicPlayerScreenDestination) {
            InternalMusicPlayerScreen()
        }

    }
}

@NavGraph
annotation class MainNavGraph(
    val start: Boolean = false
)
