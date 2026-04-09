package com.kglabs.wristdj.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlowingMediaButton(
    icon: ImageVector,
    glowColor: Color,
    secondaryColor: Color? = null,
    size: Dp = 60.dp,
    iconSize: Dp = 28.dp,
    onClick: () -> Unit
) {
    val brush = if (secondaryColor != null) {
        Brush.linearGradient(listOf(glowColor, secondaryColor))
    } else {
        SolidColor(glowColor)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = if (glowColor != Color.Transparent) 16.dp else 0.dp,
                shape = CircleShape,
                spotColor = glowColor
            )
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .clickable { onClick() }
    ) {
        // Inner Gradient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Boundary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 4.dp, brush = brush, shape = CircleShape)
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}
