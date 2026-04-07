package com.keshav.coldplayremote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.keshav.coldplayremote.components.destinations.IRRemotePageDestination
import com.keshav.coldplayremote.navigation.MainNavGraph
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@MainNavGraph(true)
@Destination
@Composable
fun HomePage(navigator: DestinationsNavigator, viewModel: HomeViewModel = hiltViewModel()) {
    val remotes = viewModel.getRemotes()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Choose Remote",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(remotes) { remote ->
                Button(
                    onClick = {
                        navigator.navigate(IRRemotePageDestination(remote))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(remote.name)
                }
            }
        }
    }
}
