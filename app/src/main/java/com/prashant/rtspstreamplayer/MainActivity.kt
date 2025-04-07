package com.prashant.rtspstreamplayer


import RTSPViewerTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.prashant.rtspstreamplayer.viewmodel.MainViewModel
import com.prashant.rtspviewer.homescreen.main.MainScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RTSPViewerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(viewModel = MainViewModel(this))
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // If needed, enter PiP here automatically when app is backgrounded
    }
}
