package com.prashant.rtspviewer.homescreen.components

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun VideoSurface(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                val videoLayout = VLCVideoLayout(context)
                addView(videoLayout, MATCH_PARENT, MATCH_PARENT)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}


