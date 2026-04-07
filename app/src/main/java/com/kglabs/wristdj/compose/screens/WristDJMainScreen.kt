package com.kglabs.wristdj.compose.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kglabs.wristdj.compose.components.HeroVisualizer

enum class StudioTab { MIC, PLAYER, COLORS }

@Composable
fun WristDJMainScreen() {
    var currentTab by remember { mutableStateOf(StudioTab.MIC) }
    val darkBackground = Color(0xFF000000)
    val neonAccent = Color(0xFF00E5FF)

    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == StudioTab.MIC,
                    onClick = { currentTab = StudioTab.MIC },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = neonAccent, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = currentTab == StudioTab.PLAYER,
                    onClick = { currentTab = StudioTab.PLAYER },
                    icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = neonAccent, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
                )
                NavigationBarItem(
                    selected = currentTab == StudioTab.COLORS,
                    onClick = { currentTab = StudioTab.COLORS },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = neonAccent, unselectedIconColor = Color.Gray, indicatorColor = Color.Transparent)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wrist DJ", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(24.dp))

            HeroVisualizer(accentColor = neonAccent)
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                when (currentTab) {
                    StudioTab.MIC -> LiveMicDeck()
                    StudioTab.PLAYER -> PlayerDeck()
                    StudioTab.COLORS -> ManualColorsDeck()
                }
            }
        }
    }
}