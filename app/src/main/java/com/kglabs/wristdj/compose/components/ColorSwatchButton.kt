package com.kglabs.wristdj.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorSwatchButton(
    uiColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(uiColor)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) Color.White else Color(0x66FFFFFF),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}
