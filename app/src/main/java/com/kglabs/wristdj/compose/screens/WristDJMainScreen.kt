package com.kglabs.wristdj.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class StudioTab { MIC, PLAYER, COLORS }

@Composable
fun WristDJMainScreen() {
    var currentTab by remember { mutableStateOf(StudioTab.PLAYER) }

    val darkBackground = Color(0xFF000000)
    val bluishWhite = Color(0xFFE0F7FF) 
    val glowAccent = Color(0xFF00E5FF)

    Scaffold(
        containerColor = darkBackground,
        bottomBar = {
            Column {
                // Top glow line to integrate into the "Studio" aesthetic
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, glowAccent.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                NavigationBar(
                    containerColor = Color(0xFF050505),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == StudioTab.MIC,
                        onClick = { currentTab = StudioTab.MIC },
                        icon = { 
                            Box(contentAlignment = Alignment.Center) {
                                if (currentTab == StudioTab.MIC) {
                                    // Filled glow orb: light source centered behind icon
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .drawBehind {
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        0.0f to glowAccent.copy(alpha = 0.5f),
                                                        1.0f to Color.Transparent,
                                                        center = center,
                                                        radius = 21.dp.toPx()
                                                    ),
                                                    radius = 21.dp.toPx()
                                                )
                                            }
                                    )
                                }
                                Icon(Icons.Default.Mic, contentDescription = "Live Mic")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = bluishWhite,
                            unselectedIconColor = Color.DarkGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == StudioTab.PLAYER,
                        onClick = { currentTab = StudioTab.PLAYER },
                        icon = { 
                            Box(contentAlignment = Alignment.Center) {
                                if (currentTab == StudioTab.PLAYER) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .drawBehind {
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        0.0f to glowAccent.copy(alpha = 0.5f),
                                                        1.0f to Color.Transparent,
                                                        center = center,
                                                        radius = 21.dp.toPx()
                                                    ),
                                                    radius = 21.dp.toPx()
                                                )
                                            }
                                    )
                                }
                                Icon(Icons.Default.MusicNote, contentDescription = "Player")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = bluishWhite,
                            unselectedIconColor = Color.DarkGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == StudioTab.COLORS,
                        onClick = { currentTab = StudioTab.COLORS },
                        icon = { 
                            Box(contentAlignment = Alignment.Center) {
                                if (currentTab == StudioTab.COLORS) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .drawBehind {
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        0.0f to glowAccent.copy(alpha = 0.5f),
                                                        1.0f to Color.Transparent,
                                                        center = center,
                                                        radius = 21.dp.toPx()
                                                    ),
                                                    radius = 21.dp.toPx()
                                                )
                                            }
                                    )
                                }
                                Icon(Icons.Default.Palette, contentDescription = "Colors")
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = bluishWhite,
                            unselectedIconColor = Color.DarkGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
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