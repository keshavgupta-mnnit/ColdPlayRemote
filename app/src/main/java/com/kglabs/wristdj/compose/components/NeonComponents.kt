package com.kglabs.wristdj.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kglabs.wristdj.R

// 🔥 Use your premium font here
val NeonFont = FontFamily(
    Font(R.font.satisfy) // <-- add this font in res/font
)

val NeonGradient = listOf(
    Color(0xFFFF4DA6), // Soft Neon Pink
    Color(0xFFFF9A3C), // Smooth Orange
    Color(0xFFFFE066), // Warm Yellow
    Color(0xFF4DFFB8)  // Neon Mint Green
)

@Composable
fun HeaderWithIcon(
    title: String,
    iconRes: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        // 🔥 ICON (balanced with font)
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // 🔥 TEXT (Satisfy + Neon Gradient)
        Text(
            text = title,
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = NeonFont,
                brush = Brush.linearGradient(
                    colors = NeonGradient,
                    start = Offset(0f, 0f),
                    end = Offset(400f, 200f)
                ),
                shadow = Shadow(
                    color = Color.White.copy(alpha = 0.35f),
                    blurRadius = 6f
                )
            )
        )
    }
}