package com.kglabs.wristdj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kglabs.wristdj.compose.screens.WristDJMainScreen
import com.kglabs.wristdj.ui.theme.WristDJTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WristDJTheme {
                WristDJMainScreen()
            }
        }
    }
}
