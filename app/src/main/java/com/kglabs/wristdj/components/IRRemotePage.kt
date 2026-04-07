package com.kglabs.wristdj.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kglabs.wristdj.RemoteUtils
import com.kglabs.wristdj.models.Remote
import com.kglabs.wristdj.navigation.MainNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

import androidx.compose.material3.ButtonDefaults
import com.kglabs.wristdj.RemoteSignalConstants

@MainNavGraph
@Destination
@Composable
fun IRRemotePage(
    navigator: DestinationsNavigator,
    remote: Remote
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = remote.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(remote.buttons) { button ->
                val backgroundColor = if (remote.name == "ColdPlay") {
                    RemoteSignalConstants.ColdPlayBand.getColorByName(button.name)
                } else {
                    MaterialTheme.colorScheme.primary
                }

                val contentColor = if (remote.name == "ColdPlay") {
                    if (button.name == "White" || button.name == "Cream" || button.name == "Light Yellow" || button.name == "Ice") {
                        androidx.compose.ui.graphics.Color.Black
                    } else {
                        androidx.compose.ui.graphics.Color.White
                    }
                } else {
                    MaterialTheme.colorScheme.onPrimary
                }

                Button(
                    onClick = { RemoteUtils.transmitSignal(button.code) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = backgroundColor,
                        contentColor = contentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Text(text = button.name, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
