package com.kglabs.wristdj.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.kglabs.wristdj.components.HomePage
import com.kglabs.wristdj.components.IRRemotePage
import com.kglabs.wristdj.components.InternalMusicPlayerScreen
import com.kglabs.wristdj.components.MusicSyncScreen
import com.kglabs.wristdj.components.NavGraphs
import com.kglabs.wristdj.components.destinations.HomePageDestination
import com.kglabs.wristdj.components.destinations.IRRemotePageDestination
import com.kglabs.wristdj.components.destinations.InternalMusicPlayerScreenDestination
import com.kglabs.wristdj.components.destinations.MusicSyncScreenDestination
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
