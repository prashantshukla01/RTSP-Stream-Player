package com.prashant.rtspstreamplayer.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class MainViewModel(private val context: Context) : ViewModel() {

    private val libVLC by lazy {
        LibVLC(context, arrayListOf("--no-drop-late-frames", "--no-skip-frames"))
    }
    private val _currentRtspUrl = MutableStateFlow("")
    val currentRtspUrl = _currentRtspUrl.asStateFlow()

    fun setRtspUrl(url: String) {
        _currentRtspUrl.value = url
    }
    private var mediaPlayer: MediaPlayer? = null
    private var videoLayout: VLCVideoLayout? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private var recordingFile: File? = null


    /**
     * Attach the VLC video layout to the media player.
     * This should only be done once to avoid LibVLC internal errors.
     */
    fun attachVideoLayout(layout: VLCVideoLayout) {
        if (videoLayout != layout) {
            videoLayout = layout
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer(libVLC).apply {
                    attachViews(layout, null, false, false)
                    Log.d("RTSP", "Video layout attached to player")
                }
            } else {
                mediaPlayer?.detachViews()
                mediaPlayer?.attachViews(layout, null, false, false)
                Log.d("RTSP", "Video layout re-attached")
            }
        }
    }

    /**
     * Start playing an RTSP stream.
     * Make sure the video layout is attached first.
     */
    fun playStream(rtspUrl: String) {
        try {
            stopStream() // stop any previous stream if active

            val media = Media(libVLC, Uri.parse(rtspUrl)).apply {
                setHWDecoderEnabled(true, false)
            }

            // release previous media safely
            mediaPlayer?.media?.release()

            mediaPlayer?.media = media
            mediaPlayer?.play()
            _isPlaying.value = true

            Log.d("RTSP", "Playing stream: $rtspUrl")

        } catch (e: Exception) {
            Log.e("RTSP", "Error playing stream", e)
        }
    }

    /**
     * Stop current video stream and update state.
     */
    fun stopStream() {
        if (_isPlaying.value) {
            mediaPlayer?.stop()
            _isPlaying.value = false
            Log.d("RTSP", "Stream stopped")
        }
    }

    /**
     * Clean up resources when player is no longer needed.
     */
    fun releasePlayer() {
        mediaPlayer?.detachViews()
        mediaPlayer?.release()
        libVLC.release()
        Log.d("RTSP", "Player and LibVLC released")
    }

    /**
     * Stub for future recording logic (requires native FFmpeg or VLC config).
     */
    fun startRecording(rtspUrl: String, outputFileName: String) {
        try {
            stopStream() // Stop any existing stream

            // Create output file
            recordingFile = File(context.getExternalFilesDir(null), "$outputFileName-${System.currentTimeMillis()}.mp4")

            // Prepare media with recording options
            val media = Media(libVLC, Uri.parse(rtspUrl)).apply {
                setHWDecoderEnabled(true, false)

                // VLC stream output options for recording
                val sout = "#transcode{vcodec=h264,vb=800,scale=1,acodec=none}:std{access=file,mux=mp4,dst=${recordingFile?.absolutePath}}"
                addOption(":sout=$sout")
                addOption(":sout-keep")
            }

            mediaPlayer?.media?.release()
            mediaPlayer?.media = media
            mediaPlayer?.play()

            _isPlaying.value = true
            _isRecording.value = true

            Log.d("RTSP", "Recording started to: ${recordingFile?.absolutePath}")

        } catch (e: Exception) {
            Log.e("RTSP", "Error starting recording", e)
            _isRecording.value = false
        }
    }
    fun stopRecording() {
        try {
            if (_isRecording.value) {
                mediaPlayer?.stop()
                _isRecording.value = false
                _isPlaying.value = false

                recordingFile?.let { file ->
                    if (file.exists()) {
                        Log.d("RTSP", "Recording saved to: ${file.absolutePath}")
                        // Here you could add a notification or callback to inform the user
                    }
                }
                recordingFile = null
            }
        } catch (e: Exception) {
            Log.e("RTSP", "Error stopping recording", e)
        }
    }




    /**
     * Stub for triggering Picture-in-Picture mode.
     */
    fun enterPipMode() {
        Log.d("RTSP", "PiP mode requested")
    }
}
