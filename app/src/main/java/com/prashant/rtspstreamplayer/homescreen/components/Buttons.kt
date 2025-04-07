package com.prashant.rtspviewer.homescreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Buttons(
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onRecord: () -> Unit,
    onPip: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPlay) {
                Text("Play")
            }
            Button(onClick = onStop) {
                Text("Stop")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRecord) {
                Text("Record")
            }
            Button(onClick = onPip) {
                Text("PiP")
            }
        }
    }
}


