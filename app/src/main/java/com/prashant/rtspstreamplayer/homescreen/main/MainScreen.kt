package com.prashant.rtspviewer.homescreen.main

import android.app.Activity
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.prashant.rtspstreamplayer.viewmodel.MainViewModel
import org.videolan.libvlc.util.VLCVideoLayout
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))

    var layoutAttached by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    val rtspUrl by viewModel.currentRtspUrl.collectAsState()

    val outputFileName = "recorded_video"

    // Get screen dimensions to calculate video box size
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Calculate video height as a ratio of available width (16:9 aspect ratio)
    val videoHeight = (screenWidth * 10f) / 16f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RTSP Stream Viewer")
                    }
                },
                actions = {
                    // Settings icon
                    IconButton(onClick = { /* Add your settings action here */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // PiP icon
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                activity?.enterPictureInPictureMode()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Picture in Picture",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // URL input section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Stream Source",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rtspUrl,
                            onValueChange = { viewModel.setRtspUrl(it) },
                            label = { Text("Enter RTSP URL") },
                            placeholder = { Text("rtsp://example.com/stream") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video player section with height based on screen ratio
                Text(
                    "Video Stream",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(videoHeight) // Use calculated height based on screen ratio
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(Color.Black)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            FrameLayout(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                val vlcLayout = VLCVideoLayout(ctx)
                                addView(
                                    vlcLayout,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                if (!layoutAttached) {
                                    viewModel.attachVideoLayout(vlcLayout)
                                    layoutAttached = true
                                }
                            }
                        }
                    )

                    if (!isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Enter URL and press Play to start streaming",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Added weight to push the controls to the bottom
                Spacer(modifier = Modifier.weight(1f))
            }

            // Controls section at the bottom with improved button sizing
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Improved button row with better alignment and sizing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Using SmallButton for more compact layout
                        SmallButton(
                            icon = Icons.Default.PlayArrow,
                            text = "Play",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = {
                                if (rtspUrl.isNotEmpty()) {
                                    viewModel.playStream(rtspUrl)
                                    isPlaying = true
                                }
                            }
                        )

                        SmallButton(
                            icon = Icons.Default.Stop,
                            text = "Stop",
                            color = MaterialTheme.colorScheme.error,
                            onClick = {
                                viewModel.stopStream()
                                isPlaying = false
                            }
                        )
                        val isRecording by viewModel.isRecording.collectAsState()


                        SmallButton(
                            icon = Icons.Default.FiberManualRecord,
                            text = if (isRecording) "Stop Rec" else "Rec",
                            color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            onClick = {
                                if (isRecording) {
                                    viewModel.stopRecording()
                                } else {
                                    if (rtspUrl.isNotEmpty()) {
                                        viewModel.startRecording(rtspUrl, outputFileName)
                                    }
                                }
                            }
                        )

                    }
                }
            }
        }
    }

    BackHandler(onBack = {
        viewModel.releasePlayer()
        activity?.finish()
    })
}

@Composable
private fun SmallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp) // Reduced height
            .padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), // Smaller internal padding
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp) // Smaller icon
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall, // Smaller text
                maxLines = 1
            )
        }
    }
}