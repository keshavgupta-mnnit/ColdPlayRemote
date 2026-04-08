package com.kglabs.wristdj.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class StudioTab { MIC, PLAYER, COLORS }

@Composable
fun WristDJMainScreen() { // NO PARAMETERS NEEDED AT ALL!
    var currentTab by remember { mutableStateOf(StudioTab.MIC) }

    val darkBackground = Color(0xFF000000)
    val neonAccent = Color(0xFF00FF00)

    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF111111),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == StudioTab.MIC,
                    onClick = { currentTab = StudioTab.MIC },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Live Mic") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = neonAccent,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentTab == StudioTab.PLAYER,
                    onClick = { currentTab = StudioTab.PLAYER },
                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Player") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = neonAccent,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentTab == StudioTab.COLORS,
                    onClick = { currentTab = StudioTab.COLORS },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Colors") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = neonAccent,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Notice how clean these calls are now!
            when (currentTab) {
                StudioTab.MIC -> LiveMicDeck()
                StudioTab.PLAYER -> PlayerDeck()
                StudioTab.COLORS -> ManualColorsDeck() // You'll just need to remove IrController from this one too!
            }
        }
    }
}