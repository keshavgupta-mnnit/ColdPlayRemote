package com.keshav.coldplayremote.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.keshav.coldplayremote.components.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    DestinationsNavHost(
        navGraph = NavGraphs.root,
        navController = navController,
        startRoute = NavGraphs.root.startRoute
    )
}
