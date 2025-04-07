package com.prashant.rtspstreamplayer.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import kotlinx.coroutines.isActive
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class MainViewModel(private val context: Context) : ViewModel() {

    private val libVLC by lazy {
        LibVLC(context, arrayListOf(
            "--no-drop-late-frames",
            "--no-skip-frames",
            "--avcodec-hw=any",
            "--rtsp-tcp"
        ))
    }

    private val _currentRtspUrl = MutableStateFlow("")
    val currentRtspUrl = _currentRtspUrl.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var videoLayout: VLCVideoLayout? = null
    private var recordingFile: File? = null

    // States
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingMessage = MutableStateFlow<String?>(null)
    val recordingMessage = _recordingMessage.asStateFlow()

    // Timer state
    private var recordingStartTime: Long = 0L
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration = _recordingDuration.asStateFlow()
    private var timerJob: Job? = null

    fun setRtspUrl(url: String) {
        _currentRtspUrl.value = url
    }

    fun attachVideoLayout(layout: VLCVideoLayout) {
        if (videoLayout != layout) {
            videoLayout = layout
            mediaPlayer?.detachViews()
            mediaPlayer = MediaPlayer(libVLC).apply {
                attachViews(layout, null, false, false)
                Log.d("RTSP", "Video layout attached")
            }
        }
    }

    fun playStream(rtspUrl: String) {
        try {
            val media = Media(libVLC, Uri.parse(rtspUrl)).apply {
                setHWDecoderEnabled(true, true)
                addOption(":network-caching=300")
            }

            mediaPlayer?.apply {
                stop()
                setMedia(media)
                play()
                _isPlaying.value = true
            }
            Log.d("RTSP", "Playing stream: $rtspUrl")
        } catch (e: Exception) {
            Log.e("RTSP", "Play error", e)
        }
    }

    fun startRecording(outputFileName: String) {
        try {
            recordingFile = File(context.getExternalFilesDir(null),
                "$outputFileName-${System.currentTimeMillis()}.mp4")

            val media = Media(libVLC, Uri.parse(_currentRtspUrl.value)).apply {
                setHWDecoderEnabled(true, true)
                addOption(":sout=#duplicate{dst=display,dst=standard{access=file,mux=mp4,dst='${recordingFile?.absolutePath}'}}")
            }

            mediaPlayer?.apply {
                stop()
                setMedia(media)
                play()
                _isRecording.value = true
                startRecordingTimer()
            }
            Log.d("RTSP", "Recording started: ${recordingFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e("RTSP", "Recording failed", e)
            _recordingMessage.value = "Recording failed: ${e.message}"
            _isRecording.value = false
        }
    }

    fun stopRecording() {
        try {
            val media = Media(libVLC, Uri.parse(_currentRtspUrl.value)).apply {
                setHWDecoderEnabled(true, true)
            }

            mediaPlayer?.apply {
                stop()
                setMedia(media)
                play()
                stopRecordingTimer()
                _isRecording.value = false
                _recordingMessage.value = "Saved to: ${recordingFile?.absolutePath}"
            }
        } catch (e: Exception) {
            Log.e("RTSP", "Stop recording failed", e)
            _recordingMessage.value = "Stop failed: ${e.message}"
        }
    }

    private fun startRecordingTimer() {
        recordingStartTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (isActive) {  // Changed from while(true)
                _recordingDuration.value = System.currentTimeMillis() - recordingStartTime
                delay(1000)
            }
        }
    }

    private fun stopRecordingTimer() {
        timerJob?.cancel()
        timerJob = null
        recordingStartTime = 0L
        _recordingDuration.value = 0L
    }

    fun getFormattedDuration(): String {
        val totalSeconds = _recordingDuration.value / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    fun stopStream() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.media?.release()

                // Handle recording state
                if (_isRecording.value) {
                    _isRecording.value = false
                    stopRecordingTimer()
                    Log.d("RTSP", "Recording stopped with stream")
                }

                _isPlaying.value = false
                Log.d("RTSP", "Stream stopped successfully")

            } catch (e: Exception) {
                Log.e("RTSP", "Error stopping stream", e)
                _isPlaying.value = false
                _isRecording.value = false
            }
        } ?: run {
            Log.w("RTSP", "Stop attempted with null mediaPlayer")
            _isPlaying.value = false
            _isRecording.value = false
        }
    }


    fun releasePlayer() {
        mediaPlayer?.detachViews()
        mediaPlayer?.release()
        libVLC.release()
        Log.d("RTSP", "Player and LibVLC released")
    }

    fun clearRecordingMessage() {
        _recordingMessage.value = null
    }
}