package com.keshav.coldplayremote

object RemoteSignalConstants {

    object Fan {
        val buttons = listOf<Pair<String, String>>(
        )
    }

    object SoundBar {
        val buttons = listOf(
            "On" to "",
            "Off" to "",
            "Increase" to "9000,4400,600,1650,600,1650,550,550,550,550,550,600,550,550,550,600,550,1700,550,550,550,1700,550,1650,550,550,550,1700,550,1650,600,550,550,1650,600,1650,550,1650,600,550,550,1650,600,550,550,550,550,600,550,550,550,600,550,550,550,1700,550,550,550,1700,550,1650,600,1600,600,1650,600"
        )
    }

    object ColdPlayBand {
        fun getColorByName(name: String): androidx.compose.ui.graphics.Color {
            return when (name) {
                "Light Red" -> androidx.compose.ui.graphics.Color(0xFFFFCCCC)
                "Red" -> androidx.compose.ui.graphics.Color(0xFFFF0000)
                "Little Red" -> androidx.compose.ui.graphics.Color(0xFFFF3333)
                "Green" -> androidx.compose.ui.graphics.Color(0xFF00FF00)
                "Green 2" -> androidx.compose.ui.graphics.Color(0xFF32CD32)
                "Green 3" -> androidx.compose.ui.graphics.Color(0xFF228B22)
                "Light Green" -> androidx.compose.ui.graphics.Color(0xFF90EE90)
                "Mint" -> androidx.compose.ui.graphics.Color(0xFF98FF98)
                "Light Mint" -> androidx.compose.ui.graphics.Color(0xFFE0FFF0)
                "Pistacchio" -> androidx.compose.ui.graphics.Color(0xFF93C572)
                "Little Green" -> androidx.compose.ui.graphics.Color(0xFF00FA9A)
                "Green Yellow" -> androidx.compose.ui.graphics.Color(0xFFADFF2F)
                "Green Yellow 2" -> androidx.compose.ui.graphics.Color(0xFF9ACD32)
                "Green Light Yellow" -> androidx.compose.ui.graphics.Color(0xFFE0FF2F)
                "Blue" -> androidx.compose.ui.graphics.Color(0xFF0000FF)
                "Cyan" -> androidx.compose.ui.graphics.Color(0xFF00FFFF)
                "Little Blue" -> androidx.compose.ui.graphics.Color(0xFFADD8E6)
                "Turquoise" -> androidx.compose.ui.graphics.Color(0xFF40E0D0)
                "Magenta" -> androidx.compose.ui.graphics.Color(0xFFFF00FF)
                "Yellow" -> androidx.compose.ui.graphics.Color(0xFFFFFF00)
                "Yellow 2" -> androidx.compose.ui.graphics.Color(0xFFFFFF33)
                "Warm Yellow" -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                "Light Yellow" -> androidx.compose.ui.graphics.Color(0xFFFFFFE0)
                "Amber" -> androidx.compose.ui.graphics.Color(0xFFFFBF00)
                "Pink" -> androidx.compose.ui.graphics.Color(0xFFFFC0CB)
                "Light Pink" -> androidx.compose.ui.graphics.Color(0xFFFFB6C1)
                "Orange" -> androidx.compose.ui.graphics.Color(0xFFFFA500)
                "Orange 2" -> androidx.compose.ui.graphics.Color(0xFFFF8C00)
                "Red Orange" -> androidx.compose.ui.graphics.Color(0xFFFF4500)
                "Orange Yellow" -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                "White" -> androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                "Cream" -> androidx.compose.ui.graphics.Color(0xFFFFFDD0)
                "Ice" -> androidx.compose.ui.graphics.Color(0xFFA5F2F3)
                else -> androidx.compose.ui.graphics.Color.Gray
            }
        }

        val buttons = listOf(
            "Light Red" to "1400,1400,700,700,700,1400,700,2800,700,2100,1400,700,700,700,700,1400,1400,2800,1400,2800,700",
            "Red" to "1400,1400,700,700,700,1400,700,2800,700,2800,700,700,700,700,700,1400,1400,2800,1400,2800,700",
            "Little Red" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,1400,1400,700,700,1400,700,700,700,700,1400,2800,700",
            "Green" to "1400,1400,700,700,700,700,1400,2800,700,1400,700,1400,700,1400,700,1400,1400,2800,1400,2800,700",
            "Green 2" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,1400,700,1400,700,2100,1400,2100,1400,2800,700",
            "Green 3" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,1400,700,1400,700,1400,700,700,700,2100,1400,2800,700",
            "Light Green" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,1400,700,1400,700,700,700,1400,700,2100,700,1400,700,1400,700",
            "Mint" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,1400,700,1400,700,1400,1400,2800,700,1400,700,1400,700",
            "Light Mint" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,1400,700,1400,700,700,700,1400,700,2100,700,700,700,2100,700",
            "Pistacchio" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,2100,700,700,700,2100,700,2800,1400,2800,700",
            "Little Green" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,2100,700,700,700,2100,700,2800,1400,2800,700",
            "Green Yellow" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,1400,700,1400,700,700,700,1400,700,2100,1400,2800,700",
            "Green Yellow 2" to "700,700,700,700,1400,1400,1400,2800,700,2100,700,700,700,1400,700,1400,700,700,700,2100,1400,2800,700",
            "Green Light Yellow" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,700,1400,1400,700,1400,1400,2800,1400,2800,700",
            "Blue" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,1400,1400,700,700,1400,700,700,700,700,700,700,700,2100,700",
            "Cyan" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,1400,700,700,700,700,700,700,700,700,1400,1400,700,2100,700",
            "Little Blue" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,1400,1400,700,700,1400,700,700,700,700,700,1400,700,1400,700",
            "Turquoise" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,700,1400,1400,700,1400,1400,2800,700,700,700,2100,700",
            "Magenta" to "700,700,700,700,1400,1400,1400,2800,700,2100,1400,2100,700,700,700,700,1400,2100,700,700,700,2100,700",
            "Yellow" to "1400,1400,700,700,700,700,1400,2800,700,2100,700,700,700,1400,700,1400,1400,2800,1400,2800,700",
            "Yellow 2" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,1400,700,700,1400,1400,700,700,1400,700,700,2800,700",
            "Warm Yellow" to "700,700,700,700,1400,1400,1400,2800,700,2100,1400,2100,700,2100,1400,2100,1400,2800,700",
            "Light Yellow" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,1400,700,700,1400,1400,700,700,1400,1400,700,2100,700",
            "Amber" to "1400,1400,700,700,700,1400,700,2800,700,2100,1400,700,700,700,700,2100,700,2800,1400,2800,700",
            "Pink" to "700,700,700,2100,1400,700,700,2800,700,2100,1400,2800,1400,1400,700,2100,700,700,700,2100,700",
            "Light Pink" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,2800,1400,1400,700,2100,700,700,700,2100,700",
            "Orange" to "700,700,700,700,1400,1400,1400,2800,700,2100,700,700,700,1400,700,700,700,1400,700,2100,1400,2800,700",
            "Orange 2" to "700,700,700,700,1400,1400,1400,2800,700,2800,1400,1400,700,1400,1400,2800,1400,2800,700",
            "Red Orange" to "700,700,700,700,1400,1400,1400,2800,700,2800,1400,1400,700,700,700,1400,700,2100,1400,2800,700",
            "Orange Yellow" to "700,700,700,700,1400,1400,1400,2800,700,1400,700,700,700,2100,700,700,700,700,1400,2100,1400,2800,700",
            "White" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,2800,1400,1400,700,2100,700,700,700,2100,700",
            "Cream" to "700,700,700,2100,1400,700,700,2800,700,1400,700,700,700,2800,1400,1400,700,2100,700,1400,700,1400,700",
            "Ice" to "1400,1400,700,700,700,700,1400,2800,700,2100,700,700,700,1400,700,1400,1400,2800,700,700,700,2100,700"
        )
    }
}
