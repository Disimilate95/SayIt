package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

class AudioRecorderManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var tempFile: File? = null

    private var sampleJob: Job? = null
    private var playbackJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _hasRecording = MutableStateFlow(false)
    val hasRecording: StateFlow<Boolean> = _hasRecording.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(List(28) { 0.15f })
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    var onAutoStopMaxDuration: (() -> Unit)? = null

    fun startRecording(): Boolean {
        wipeData()
        try {
            val file = File(context.cacheDir, "sayit_ephem_audio.m4a")
            if (file.exists()) file.delete()
            tempFile = file

            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(file.absolutePath)
            recorder.setMaxDuration(120_000) // 2 minutes max
            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            _isRecording.value = true
            _durationSeconds.value = 0
            _amplitudes.value = List(28) { 0.15f }

            startSamplingLoop()
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error starting record: ${e.message}", e)
            wipeData()
            return false
        }
    }

    private fun startSamplingLoop() {
        sampleJob?.cancel()
        sampleJob = scope.launch {
            var elapsedMs = 0L
            while (isActive && _isRecording.value) {
                delay(70)
                elapsedMs += 70
                _durationSeconds.value = (elapsedMs / 1000).toInt()

                val amp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }
                // Normalize 0..32767 with soft curve
                val normalized = min(1f, (amp / 12000f) + 0.12f)
                val currentList = _amplitudes.value.toMutableList()
                if (currentList.isNotEmpty()) {
                    currentList.removeAt(0)
                    currentList.add(normalized)
                    _amplitudes.value = currentList
                }

                if (elapsedMs >= 120_000) {
                    stopRecording()
                    onAutoStopMaxDuration?.invoke()
                    break
                }
            }
        }
    }

    fun stopRecording() {
        sampleJob?.cancel()
        sampleJob = null
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.w("AudioRecorderManager", "Stop recorder error: ${e.message}")
        } finally {
            try {
                mediaRecorder?.release()
            } catch (e: Exception) {
                // Ignore
            }
            mediaRecorder = null
            _isRecording.value = false
        }

        if (tempFile != null && tempFile!!.exists() && tempFile!!.length() > 0) {
            _hasRecording.value = true
        } else {
            _hasRecording.value = false
        }
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        val file = tempFile ?: return
        if (!file.exists()) return

        try {
            if (mediaPlayer == null) {
                val player = MediaPlayer()
                player.setDataSource(file.absolutePath)
                player.prepare()
                player.setVolume(_volume.value, _volume.value)
                player.setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackProgress.value = 0f
                    playbackJob?.cancel()
                }
                mediaPlayer = player
            }

            mediaPlayer?.start()
            _isPlaying.value = true
            startPlaybackTracker()
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Playback error: ${e.message}", e)
            _isPlaying.value = false
        }
    }

    fun pausePlayback() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.w("AudioRecorderManager", "Pause error: ${e.message}")
        }
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    private fun startPlaybackTracker() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive && _isPlaying.value) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val current = player.currentPosition
                    val duration = player.duration
                    if (duration > 0) {
                        _playbackProgress.value = current.toFloat() / duration.toFloat()
                    }
                }
                delay(80)
            }
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun wipeData() {
        sampleJob?.cancel()
        sampleJob = null
        playbackJob?.cancel()
        playbackJob = null

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null

        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null

        tempFile?.let {
            if (it.exists()) {
                it.delete()
            }
        }
        tempFile = null

        _isRecording.value = false
        _isPlaying.value = false
        _hasRecording.value = false
        _durationSeconds.value = 0
        _playbackProgress.value = 0f
        _amplitudes.value = List(28) { 0.15f }
    }
}
