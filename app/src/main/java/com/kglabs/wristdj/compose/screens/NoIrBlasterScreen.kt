package com.kglabs.wristdj.compose.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoIrBlasterScreen() {
    val context = LocalContext.current

    // Subtle pulsing animation for the red warning glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Deep atmospheric background, tinted slightly dark red for the error state
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    // Deep dark red fading to pure black
                    colors = listOf(Color(0xFF2A1010), Color(0xFF050505)),
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {

            // 1. Glowing Hardware Error Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Soft pulsing red radial bloom behind the icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF0040).copy(alpha = glowAlpha), Color.Transparent)
                            )
                        )
                )

                // The actual hollow neon icon container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF111111))
                        .border(2.dp, Color(0xFFFF0040), CircleShape)
                        .shadow(16.dp, CircleShape, spotColor = Color(0xFFFF0040))
                ) {
                    // Center Remote Icon
                    Icon(
                        imageVector = Icons.Rounded.SettingsRemote,
                        contentDescription = "Hardware Required",
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )

                    // Small floating warning badge
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = "Warning",
                        tint = Color(0xFFFF0040),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-12).dp, y = (-12).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 2. Typography
            Text(
                text = "Hardware Not Supported",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wrist DJ requires a built-in Infrared (IR) Blaster to transmit signals to your PixMob wristbands.\n\nUnfortunately, we couldn't detect an IR Blaster on this device.",
                color = Color.LightGray,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Exit Button (Styled exactly like our hollow neon toggle switch)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF111111)) // Hollow center
                    .border(2.dp, Color(0xFFFF0040), RoundedCornerShape(50))
                    .clickable {
                        // Safely closes the app
                        (context as? Activity)?.finish()
                    }
                    .shadow(12.dp, RoundedCornerShape(50), spotColor = Color(0xFFFF0040))
            ) {
                Text(
                    text = "CLOSE APP",
                    color = Color(0xFFFF0040),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}