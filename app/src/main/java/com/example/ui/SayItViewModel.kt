package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRecorderManager
import com.example.model.AppLanguage
import com.example.model.InfoDialogType
import com.example.model.SayItStrings
import com.example.model.ThemeMode
import com.example.model.TranslationProvider
import com.example.model.TriggerCategory
import com.example.model.TriggerSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SayItUiState(
    val text: String = "",
    val isRecording: Boolean = false,
    val hasRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val durationSeconds: Int = 0,
    val playbackProgress: Float = 0f,
    val amplitudes: List<Float> = List(28) { 0.15f },
    val volume: Float = 1.0f,
    val isReleasing: Boolean = false,
    val releaseStage: Int = 0, // 0: idle, 1: messenger bubble visible for 2s, 2: smooth dissolve, 3: supportive message, 4: fade message
    val sentText: String? = null,
    val sentHasAudio: Boolean = false,
    val sentDurationSeconds: Int = 0,
    val sentTime: String = "",
    val recipient: String = "В пустоту",
    val sentRecipient: String = "В пустоту",
    val showRecipientPicker: Boolean = false,
    val supportMessage: String? = null,
    val activeTriggerCategory: TriggerCategory = TriggerCategory.NEUTRAL,
    val shakeTrigger: Long = 0L,
    val currentLanguage: AppLanguage = AppLanguage.RUSSIAN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showDisclaimer: Boolean = false,
    val showMenu: Boolean = false,
    val activeInfoDialog: InfoDialogType? = null,
    val toastMessage: String? = null
)

class SayItViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("sayit_prefs", Context.MODE_PRIVATE)
    val recorderManager = AudioRecorderManager(application)

    private val _text = MutableStateFlow("")
    private val _isReleasing = MutableStateFlow(false)
    private val _releaseStage = MutableStateFlow(0)
    private val _sentText = MutableStateFlow<String?>(null)
    private val _sentHasAudio = MutableStateFlow(false)
    private val _sentDurationSeconds = MutableStateFlow(0)
    private val _sentTime = MutableStateFlow("")
    private val _supportMessage = MutableStateFlow<String?>(null)
    private val _activeTriggerCategory = MutableStateFlow(TriggerCategory.NEUTRAL)
    private val _shakeTrigger = MutableStateFlow(0L)

    private val savedLangCode = prefs.getString("key_language", AppLanguage.RUSSIAN.code) ?: AppLanguage.RUSSIAN.code
    private val initialLang = AppLanguage.values().firstOrNull { it.code == savedLangCode } ?: AppLanguage.RUSSIAN
    private val _currentLanguage = MutableStateFlow(initialLang)

    private val defaultRecipient = when (initialLang) {
        AppLanguage.KYRGYZ -> "Ааламга"
        AppLanguage.RUSSIAN -> "В пустоту"
        AppLanguage.ENGLISH -> "Into the void"
    }
    private val savedRecipient = prefs.getString("key_recipient", defaultRecipient) ?: defaultRecipient
    private val _recipient = MutableStateFlow(savedRecipient)
    private val _sentRecipient = MutableStateFlow(savedRecipient)
    private val _showRecipientPicker = MutableStateFlow(false)

    private val savedTheme = prefs.getString("key_theme", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
    private val initialTheme = try { ThemeMode.valueOf(savedTheme) } catch (e: Exception) { ThemeMode.SYSTEM }
    private val _themeMode = MutableStateFlow(initialTheme)

    private val _showDisclaimer = MutableStateFlow(true)
    private val _showMenu = MutableStateFlow(false)
    private val _activeInfoDialog = MutableStateFlow<InfoDialogType?>(null)
    private val _toastMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SayItUiState> = combine(
        combine(
            _text,
            recorderManager.isRecording,
            recorderManager.hasRecording,
            recorderManager.isPlaying,
            recorderManager.durationSeconds
        ) { text, isRec, hasRec, isPlaying, duration ->
            SayItUiState(
                text = text,
                isRecording = isRec,
                hasRecording = hasRec,
                isPlaying = isPlaying,
                durationSeconds = duration
            )
        },
        combine(
            recorderManager.playbackProgress,
            recorderManager.amplitudes,
            recorderManager.volume,
            _isReleasing,
            _releaseStage
        ) { progress, amps, vol, isReleasing, stage ->
            PartialState2(progress, amps, vol, isReleasing, stage)
        },
        combine(
            _sentText,
            _sentHasAudio,
            _sentDurationSeconds,
            _sentTime,
            _supportMessage
        ) { sentText, sentAudio, sentDuration, sentTime, msg ->
            PartialSentState(sentText, sentAudio, sentDuration, sentTime, msg)
        },
        combine(
            _recipient,
            _sentRecipient,
            _showRecipientPicker,
            _activeTriggerCategory,
            _shakeTrigger
        ) { recipient, sentRecipient, showPicker, cat, shake ->
            PartialRecipientState(recipient, sentRecipient, showPicker, cat, shake)
        },
        combine(
            _currentLanguage,
            _themeMode,
            _showDisclaimer,
            _showMenu,
            combine(_activeInfoDialog, _toastMessage) { d, t -> Pair(d, t) }
        ) { lang, theme, showDisc, showMenu, dialogAndToast ->
            PartialState4(lang, theme, showDisc, showMenu, dialogAndToast.first, dialogAndToast.second)
        }
    ) { s1, s2, sSent, sRec, s4 ->
        s1.copy(
            playbackProgress = s2.progress,
            amplitudes = s2.amplitudes,
            volume = s2.volume,
            isReleasing = s2.isReleasing,
            releaseStage = s2.stage,
            sentText = sSent.sentText,
            sentHasAudio = sSent.sentAudio,
            sentDurationSeconds = sSent.sentDuration,
            sentTime = sSent.sentTime,
            supportMessage = sSent.msg,
            recipient = sRec.recipient,
            sentRecipient = sRec.sentRecipient,
            showRecipientPicker = sRec.showPicker,
            activeTriggerCategory = sRec.cat,
            shakeTrigger = sRec.shake,
            currentLanguage = s4.language,
            themeMode = s4.themeMode,
            showDisclaimer = s4.showDisc,
            showMenu = s4.showMenu,
            activeInfoDialog = s4.infoDialog,
            toastMessage = s4.toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SayItUiState(
            currentLanguage = initialLang,
            recipient = defaultRecipient,
            sentRecipient = defaultRecipient,
            themeMode = initialTheme,
            showDisclaimer = true
        )
    )

    init {
        recorderManager.onAutoStopMaxDuration = {
            vibrateDevice(50)
            showToast(getStrings().maxDurationReached)
        }
    }

    fun getStrings(): SayItStrings = TranslationProvider.get(_currentLanguage.value)

    fun onTextChanged(newText: String) {
        _text.value = newText
    }

    fun setRecipient(newRecipient: String) {
        val trimmed = newRecipient.trim()
        if (trimmed.isNotEmpty()) {
            _recipient.value = trimmed
            prefs.edit().putString("key_recipient", trimmed).apply()
        }
    }

    fun openRecipientPicker() {
        _showRecipientPicker.value = true
    }

    fun closeRecipientPicker() {
        _showRecipientPicker.value = false
    }

    fun startRecording(hasPermission: Boolean) {
        if (!hasPermission) {
            showToast(getStrings().micPermissionRequired)
            return
        }
        vibrateDevice(40)
        val started = recorderManager.startRecording()
        if (!started) {
            showToast("Could not access microphone")
        }
    }

    fun stopRecording() {
        vibrateDevice(40)
        recorderManager.stopRecording()
    }

    fun togglePlayback() {
        recorderManager.togglePlayback()
    }

    fun deleteRecording() {
        vibrateDevice(20)
        recorderManager.wipeData()
    }

    fun setVolume(vol: Float) {
        recorderManager.setVolume(vol)
    }

    fun releaseThought() {
        if (_isReleasing.value) return

        val rawText = _text.value.trim()
        val hasText = rawText.isNotEmpty()
        val hasAudio = recorderManager.hasRecording.value || recorderManager.isRecording.value

        if (!hasText && !hasAudio) {
            _shakeTrigger.value = System.currentTimeMillis()
            vibrateDevice(60)
            showToast(getStrings().emptyWarning)
            return
        }

        // 1. Precise trigger detection
        val category = TriggerSystem.detectCategory(rawText)
        val message = TriggerSystem.getSupportMessage(category, _currentLanguage.value)
        _activeTriggerCategory.value = category
        _supportMessage.value = message

        // 2. Prepare sent message snapshot
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _sentText.value = if (hasText) rawText else null
        _sentHasAudio.value = hasAudio
        _sentDurationSeconds.value = recorderManager.durationSeconds.value
        _sentTime.value = timeFormat
        _sentRecipient.value = _recipient.value

        // 3. Clear bottom input field immediately
        _text.value = ""

        vibrateDevice(45)

        viewModelScope.launch {
            // Stop recorder & pause playback
            recorderManager.stopRecording()
            recorderManager.pausePlayback()

            _isReleasing.value = true

            // Stage 1: Messenger bubble appears in the conversation area and stays for 2 seconds
            _releaseStage.value = 1
            delay(2000L) // Stays as a messenger bubble for 2 seconds

            // Stage 2: Bubble smoothly dissolves into the void without clutter particles
            _releaseStage.value = 2
            delay(700L)

            // Stage 3: Peaceful supportive message manifests
            _releaseStage.value = 3
            delay(2400L)

            // Stage 4: Supportive message gently fades away
            _releaseStage.value = 4
            delay(500L)

            // Reset back to idle state with all memory permanently cleared
            _sentText.value = null
            _sentHasAudio.value = false
            _sentDurationSeconds.value = 0
            _sentTime.value = ""
            recorderManager.wipeData()
            _releaseStage.value = 0
            _supportMessage.value = null
            _isReleasing.value = false
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("key_language", lang.code).apply()
        // If recipient was the default for previous language, update it
        val oldDefaults = listOf("В пустоту", "Ааламга", "Into the void")
        if (_recipient.value in oldDefaults) {
            val newDefault = when (lang) {
                AppLanguage.KYRGYZ -> "Ааламга"
                AppLanguage.RUSSIAN -> "В пустоту"
                AppLanguage.ENGLISH -> "Into the void"
            }
            _recipient.value = newDefault
            _sentRecipient.value = newDefault
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("key_theme", mode.name).apply()
    }

    fun dismissDisclaimer() {
        _showDisclaimer.value = false
        prefs.edit().putBoolean("key_has_seen_disclaimer", true).apply()
    }

    fun openMenu() {
        _showMenu.value = true
    }

    fun closeMenu() {
        _showMenu.value = false
    }

    fun openInfoDialog(type: InfoDialogType) {
        _activeInfoDialog.value = type
    }

    fun closeInfoDialog() {
        _activeInfoDialog.value = null
    }

    fun shareApp() {
        val strings = getStrings()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, strings.shareContent)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, strings.shareApp).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(2500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    private fun vibrateDevice(durationMs: Long) {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onCleared() {
        super.onCleared()
        recorderManager.wipeData()
    }
}

private data class PartialState2(
    val progress: Float,
    val amplitudes: List<Float>,
    val volume: Float,
    val isReleasing: Boolean,
    val stage: Int
)

private data class PartialSentState(
    val sentText: String?,
    val sentAudio: Boolean,
    val sentDuration: Int,
    val sentTime: String,
    val msg: String?
)

private data class PartialRecipientState(
    val recipient: String,
    val sentRecipient: String,
    val showPicker: Boolean,
    val cat: TriggerCategory,
    val shake: Long
)

private data class PartialState4(
    val language: AppLanguage,
    val themeMode: ThemeMode,
    val showDisc: Boolean,
    val showMenu: Boolean,
    val infoDialog: InfoDialogType?,
    val toast: String?
)
