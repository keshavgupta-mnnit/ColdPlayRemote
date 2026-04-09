package com.kglabs.wristdj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kglabs.wristdj.compose.screens.NoIrBlasterScreen
import com.kglabs.wristdj.compose.screens.WristDJMainScreen
import com.kglabs.wristdj.ui.theme.WristDJTheme
import com.kglabs.wristdj.utils.GlobalAudioPlayer
import com.kglabs.wristdj.utils.IRUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalAudioPlayer.init(this)
        setContent {
            WristDJTheme {
                if (!IRUtils.hasIrEmitter()) {
                    NoIrBlasterScreen()
                } else {
                    WristDJMainScreen()
                }
            }
        }
    }
}
