package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppLanguage
import com.example.model.InfoDialogType
import com.example.model.SayItStrings
import com.example.model.ThemeMode
import com.example.model.TranslationProvider
import com.example.ui.components.BackgroundGlow
import com.example.ui.components.DisclaimerDialog
import com.example.ui.components.InfoDetailsDialog
import com.example.ui.components.MenuSheet
import com.example.ui.components.RecipientPickerDialog
import com.example.ui.components.VoiceRecordBar
import com.example.ui.theme.AccentRedRecord
import com.example.ui.theme.AccentRedRecordGlow
import com.example.ui.theme.LocalSayItColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SayItScreen(
    viewModel: SayItViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val strings = TranslationProvider.get(state.currentLanguage)
    val customColors = LocalSayItColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Permission launcher for audio recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording(hasPermission = true)
        }
    }

    // Modal bottom sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Shake animation for empty release attempt
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(state.shakeTrigger) {
        if (state.shakeTrigger > 0L) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(-14f, tween(45))
            shakeOffset.animateTo(14f, tween(50))
            shakeOffset.animateTo(-10f, tween(45))
            shakeOffset.animateTo(10f, tween(45))
            shakeOffset.animateTo(-4f, tween(40))
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    // Airplane flight animation in release button
    val planeFlightOffset = remember { Animatable(0f) }
    LaunchedEffect(state.isReleasing) {
        if (state.isReleasing) {
            planeFlightOffset.animateTo(40f, tween(500, easing = FastOutSlowInEasing))
            planeFlightOffset.snapTo(0f)
        }
    }

    // Pulsing mic button animation
    val infiniteTransition = rememberInfiniteTransition(label = "MicPulseTransition")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicScale"
    )

    // Touch interaction scales
    val micInteractionSource = remember { MutableInteractionSource() }
    val isMicPressed by micInteractionSource.collectIsPressedAsState()
    val animatedMicTouchScale by animateFloatAsState(
        targetValue = if (isMicPressed) 0.90f else 1f,
        animationSpec = tween(100),
        label = "MicTouchScale"
    )

    val releaseInteractionSource = remember { MutableInteractionSource() }
    val isReleasePressed by releaseInteractionSource.collectIsPressedAsState()
    val animatedReleaseTouchScale by animateFloatAsState(
        targetValue = if (isReleasePressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "ReleaseTouchScale"
    )

    BackgroundGlow(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP BAR (SayIt Brand Pill, Language, Theme, Menu)
                ModernTopBar(
                    strings = strings,
                    currentLanguage = state.currentLanguage,
                    isDarkActive = customColors.isDark,
                    onOpenMenu = { viewModel.openMenu() },
                    onOpenDisclaimer = { viewModel.openInfoDialog(InfoDialogType.DISCLAIMER) },
                    onToggleLanguage = {
                        val nextLang = when (state.currentLanguage) {
                            AppLanguage.RUSSIAN -> AppLanguage.KYRGYZ
                            AppLanguage.KYRGYZ -> AppLanguage.ENGLISH
                            AppLanguage.ENGLISH -> AppLanguage.RUSSIAN
                        }
                        viewModel.setLanguage(nextLang)
                    },
                    onToggleTheme = {
                        val nextMode = when (state.themeMode) {
                            ThemeMode.SYSTEM -> if (customColors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                        }
                        viewModel.setThemeMode(nextMode)
                    }
                )

                // 2. WORKING RECIPIENT SELECTOR ROW (Clickable to change recipient anytime)
                InteractiveRecipientRow(
                    recipient = state.recipient,
                    strings = strings,
                    onClick = { viewModel.openRecipientPicker() }
                )

                // 3. CENTRAL CHAT AREA & EPHEMERAL VOID
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val chatScrollState = rememberScrollState()

                    // --- IDLE STATE: Tranquil welcome when no message is being released ---
                    if (state.releaseStage == 0) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Serene status badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(customColors.pillBackground)
                                    .border(1.dp, customColors.pillBorder, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "✨",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = strings.quietTitle,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = customColors.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = strings.quietSubtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = customColors.textSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.widthIn(max = 320.dp)
                            )
                        }
                    }

                    // --- STAGE 1 & 2: OUTGOING MESSENGER MESSAGE BUBBLE (Clean, modern messenger look, dissolves cleanly) ---
                    val isBubbleVisible = state.releaseStage in 1..2
                    val bubbleAlpha by animateFloatAsState(
                        targetValue = when (state.releaseStage) {
                            1 -> 1f
                            2 -> 0f // smooth evaporation into the void
                            else -> 0f
                        },
                        animationSpec = tween(
                            durationMillis = if (state.releaseStage == 2) 650 else 250,
                            easing = FastOutSlowInEasing
                        ),
                        label = "MessengerBubbleAlpha"
                    )
                    val bubbleOffsetY by animateFloatAsState(
                        targetValue = when (state.releaseStage) {
                            1 -> 0f
                            2 -> -32f // floats smoothly upward
                            else -> 16f
                        },
                        animationSpec = tween(
                            durationMillis = if (state.releaseStage == 2) 650 else 250,
                            easing = FastOutSlowInEasing
                        ),
                        label = "MessengerBubbleOffsetY"
                    )
                    val bubbleScale by animateFloatAsState(
                        targetValue = when (state.releaseStage) {
                            1 -> 1f
                            2 -> 1.04f // gentle expansion as it dissolves
                            else -> 0.94f
                        },
                        animationSpec = tween(
                            durationMillis = if (state.releaseStage == 2) 650 else 250,
                            easing = FastOutSlowInEasing
                        ),
                        label = "MessengerBubbleScale"
                    )

                    if (isBubbleVisible && bubbleAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(chatScrollState),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset { IntOffset(0, bubbleOffsetY.roundToInt()) }
                                    .scale(bubbleScale)
                                    .alpha(bubbleAlpha),
                                horizontalAlignment = Alignment.End
                            ) {
                                // Real Messenger Message Bubble (outgoing, clean single-layer design)
                                val outgoingBubbleShape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 4.dp
                                )

                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = outgoingBubbleShape,
                                            ambientColor = Color(0x33000000),
                                            spotColor = Color(0x33000000)
                                        )
                                        .clip(outgoingBubbleShape)
                                        .background(
                                            brush = Brush.horizontalGradient(customColors.bubbleGradient)
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))
                                            ),
                                            shape = outgoingBubbleShape
                                        )
                                        .widthIn(min = 130.dp, max = 320.dp)
                                        .testTag("thought_bubble_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(
                                            start = 13.dp,
                                            end = 13.dp,
                                            top = 9.dp,
                                            bottom = 7.dp
                                        )
                                    ) {
                                        // Header inside bubble: Recipient indication badge
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier
                                                .padding(bottom = 5.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x28FFFFFF))
                                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.95f),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "Кому: ${state.sentRecipient}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }

                                        // If audio was sent: modern audio message snippet
                                        if (state.sentHasAudio) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0x22FFFFFF))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.25f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GraphicEq,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(17.dp)
                                                    )
                                                }
                                                val m = state.sentDurationSeconds / 60
                                                val s = state.sentDurationSeconds % 60
                                                Text(
                                                    text = String.format("%02d:%02d", m, s),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp
                                                    )
                                                )
                                                Text(
                                                    text = "• Голосовое",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontSize = 11.5.sp
                                                    )
                                                )
                                            }
                                        }

                                        // If text was sent: crisp message typography seamlessly blended
                                        state.sentText?.let { textMsg ->
                                            Text(
                                                text = textMsg,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    lineHeight = 21.sp,
                                                    fontWeight = FontWeight.Normal
                                                ),
                                                modifier = Modifier.padding(top = 1.dp)
                                            )
                                        }

                                        // Messenger timestamp & read checks
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .padding(top = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = state.sentTime,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White.copy(alpha = 0.80f),
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }

                                // Ephemeral note under the message
                                Text(
                                    text = "✦ Растворится навсегда через 2 сек",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = customColors.textMuted,
                                        fontSize = 10.5.sp
                                    ),
                                    modifier = Modifier.padding(top = 6.dp, end = 4.dp)
                                )
                            }
                        }
                    }

                    // --- STAGE 3 & 4: SUPPORTIVE MESSAGE EMERGENCE (Clean and soothing without particles) ---
                    val isMessagePhase = state.releaseStage in 3..4
                    val messageAlpha by animateFloatAsState(
                        targetValue = when (state.releaseStage) {
                            3 -> 1f
                            else -> 0f
                        },
                        animationSpec = tween(
                            durationMillis = if (state.releaseStage == 3) 550 else 450,
                            easing = FastOutSlowInEasing
                        ),
                        label = "SupportMessageAlpha"
                    )

                    val messageOffsetY by animateFloatAsState(
                        targetValue = when (state.releaseStage) {
                            3 -> 0f
                            4 -> -22f
                            else -> 16f
                        },
                        animationSpec = tween(
                            durationMillis = if (state.releaseStage == 3) 550 else 450,
                            easing = FastOutSlowInEasing
                        ),
                        label = "SupportMessageOffset"
                    )

                    val messageScale by animateFloatAsState(
                        targetValue = when (state.releaseStage) {
                            3 -> 1.0f
                            else -> 0.94f
                        },
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
                        label = "SupportMessageScale"
                    )

                    if (isMessagePhase && messageAlpha > 0.01f && state.supportMessage != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .widthIn(max = 420.dp)
                                .offset { IntOffset(0, messageOffsetY.roundToInt()) }
                                .scale(messageScale)
                                .alpha(messageAlpha)
                        ) {
                            // Soft ethereal back-glow halo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(130.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                customColors.releaseButtonColor.copy(alpha = 0.20f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Glass supportive card
                            Surface(
                                shape = RoundedCornerShape(26.dp),
                                color = if (customColors.isDark) Color(0xF2111827) else Color(0xFDF8FAFC),
                                border = BorderStroke(1.dp, customColors.cardBorder),
                                shadowElevation = 14.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Soothing glowing emblem
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        customColors.releaseButtonColor.copy(alpha = 0.22f),
                                                        customColors.releaseButtonColor.copy(alpha = 0.06f)
                                                    )
                                                )
                                            )
                                            .border(1.dp, customColors.releaseButtonColor.copy(alpha = 0.35f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null,
                                            tint = customColors.releaseButtonColor,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    Text(
                                        text = state.supportMessage ?: "",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = customColors.textPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.5.sp,
                                            lineHeight = 24.sp
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. BOTTOM DOCK WITH TEXT INPUT IN THE MIDDLE
                // Between the Voice Recording button (left) and the Release button (right)
                ModernMessengerBottomDock(
                    text = state.text,
                    isRecording = state.isRecording,
                    hasRecording = state.hasRecording,
                    isPlaying = state.isPlaying,
                    durationSeconds = state.durationSeconds,
                    playbackProgress = state.playbackProgress,
                    amplitudes = state.amplitudes,
                    volume = state.volume,
                    strings = strings,
                    shakeOffset = shakeOffset.value,
                    animatedMicScale = if (state.isRecording) micPulseScale * animatedMicTouchScale else animatedMicTouchScale,
                    animatedReleaseScale = animatedReleaseTouchScale,
                    planeFlightOffset = planeFlightOffset.value,
                    onTextChanged = { viewModel.onTextChanged(it) },
                    onMicClick = {
                        if (state.isRecording) {
                            viewModel.stopRecording()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                viewModel.startRecording(true)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    onReleaseClick = { viewModel.releaseThought() },
                    onStopRecording = { viewModel.stopRecording() },
                    onTogglePlay = { viewModel.togglePlayback() },
                    onRerecord = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            viewModel.startRecording(true)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onDeleteRecording = { viewModel.deleteRecording() },
                    onVolumeChange = { viewModel.setVolume(it) },
                    micInteractionSource = micInteractionSource,
                    releaseInteractionSource = releaseInteractionSource
                )
            }

            // Floating Toast notification
            state.toastMessage?.let { toast ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (customColors.isDark) Color(0xE6111827) else Color(0xFAF8FAFC),
                        border = BorderStroke(1.dp, customColors.cardBorder),
                        shadowElevation = 10.dp
                    ) {
                        Text(
                            text = toast,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = customColors.textPrimary,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Recipient Picker Dialog
    if (state.showRecipientPicker) {
        RecipientPickerDialog(
            currentRecipient = state.recipient,
            currentLanguage = state.currentLanguage,
            onRecipientSelected = { viewModel.setRecipient(it) },
            onDismiss = { viewModel.closeRecipientPicker() }
        )
    }

    // Modal Disclaimer
    if (state.showDisclaimer) {
        DisclaimerDialog(
            strings = strings,
            onDismiss = { viewModel.dismissDisclaimer() }
        )
    }

    // Modal Info Dialog (About, Support with hotlines)
    state.activeInfoDialog?.let { dialogType ->
        InfoDetailsDialog(
            type = dialogType,
            strings = strings,
            onDismiss = { viewModel.closeInfoDialog() }
        )
    }

    // Modal Bottom Sheet Menu
    if (state.showMenu) {
        MenuSheet(
            sheetState = sheetState,
            currentLanguage = state.currentLanguage,
            currentThemeMode = state.themeMode,
            strings = strings,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onThemeModeSelected = { viewModel.setThemeMode(it) },
            onOpenInfoDialog = { viewModel.openInfoDialog(it) },
            onShareApp = { viewModel.shareApp() },
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    viewModel.closeMenu()
                }
            }
        )
    }
}

@Composable
private fun ModernTopBar(
    strings: SayItStrings,
    currentLanguage: AppLanguage,
    isDarkActive: Boolean,
    onOpenMenu: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val customColors = LocalSayItColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: SayIt Brand Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(customColors.pillBackground)
                .border(1.dp, customColors.pillBorder, RoundedCornerShape(22.dp))
                .clickable(onClick = onOpenDisclaimer)
                .padding(start = 12.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = customColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp
                )
            )

            // Privacy indicator dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38BDF8))
            )
        }

        // Right Action Cluster: Language pill + Theme toggle + Menu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Language selector button
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(customColors.pillBackground)
                    .border(1.dp, customColors.pillBorder, RoundedCornerShape(19.dp))
                    .clickable(onClick = onToggleLanguage)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentLanguage.badge,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = customColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            // Theme toggle button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(customColors.pillBackground)
                    .border(1.dp, customColors.pillBorder, CircleShape)
                    .clickable(onClick = onToggleTheme),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkActive) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = strings.themeLabel,
                    tint = customColors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // More / Menu button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(customColors.pillBackground)
                    .border(1.dp, customColors.pillBorder, CircleShape)
                    .clickable(onClick = onOpenMenu),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = strings.menuTitle,
                    tint = customColors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Interactive Recipient Row:
 * Clicking opens the RecipientPickerDialog to choose or enter who the thought is addressed to.
 */
@Composable
private fun InteractiveRecipientRow(
    recipient: String,
    strings: SayItStrings,
    onClick: () -> Unit
) {
    val customColors = LocalSayItColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(customColors.pillBackground)
                .border(1.dp, customColors.pillBorder, RoundedCornerShape(20.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("recipient_row")
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = customColors.releaseButtonColor,
                modifier = Modifier.size(13.dp)
            )

            Text(
                text = "Кому:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = customColors.textSecondary,
                    fontSize = 12.5.sp
                )
            )

            Text(
                text = recipient,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = customColors.releaseButtonColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Change recipient",
                tint = customColors.releaseButtonColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Modern Messenger Bottom Dock:
 * - Left: Voice Recording button
 * - Middle: Text Input Field
 * - Right: "Отпустить" (Release/Send) button
 */
@Composable
private fun ModernMessengerBottomDock(
    text: String,
    isRecording: Boolean,
    hasRecording: Boolean,
    isPlaying: Boolean,
    durationSeconds: Int,
    playbackProgress: Float,
    amplitudes: List<Float>,
    volume: Float,
    strings: SayItStrings,
    shakeOffset: Float,
    animatedMicScale: Float,
    animatedReleaseScale: Float,
    planeFlightOffset: Float,
    onTextChanged: (String) -> Unit,
    onMicClick: () -> Unit,
    onReleaseClick: () -> Unit,
    onStopRecording: () -> Unit,
    onTogglePlay: () -> Unit,
    onRerecord: () -> Unit,
    onDeleteRecording: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    micInteractionSource: MutableInteractionSource,
    releaseInteractionSource: MutableInteractionSource
) {
    val customColors = LocalSayItColors.current
    val hasContent = text.trim().isNotEmpty() || hasRecording || isRecording

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .offset { IntOffset(shakeOffset.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable voice recording player card above the input row
        AnimatedVisibility(
            visible = isRecording || hasRecording,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            VoiceRecordBar(
                isRecording = isRecording,
                hasRecording = hasRecording,
                isPlaying = isPlaying,
                durationSeconds = durationSeconds,
                playbackProgress = playbackProgress,
                amplitudes = amplitudes,
                volume = volume,
                strings = strings,
                onStopRecording = onStopRecording,
                onTogglePlay = onTogglePlay,
                onRerecord = onRerecord,
                onDelete = onDeleteRecording,
                onVolumeChange = onVolumeChange,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Modern Messenger Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            shape = RoundedCornerShape(28.dp),
            color = customColors.dockBackground,
            border = BorderStroke(1.dp, customColors.dockBorder),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. LEFT: Voice Recording Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(animatedMicScale)
                        .shadow(
                            elevation = if (isRecording) 12.dp else 2.dp,
                            shape = CircleShape,
                            spotColor = if (isRecording) AccentRedRecordGlow else Color(0x30000000)
                        )
                        .clip(CircleShape)
                        .background(
                            if (isRecording) {
                                Brush.radialGradient(listOf(AccentRedRecord, Color(0xFFDC2626)))
                            } else {
                                SolidColor(customColors.micIdleBackground)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isRecording) Color(0x80FFFFFF) else customColors.pillBorder,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = micInteractionSource,
                            indication = null,
                            onClick = onMicClick
                        )
                        .testTag("microphone_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) strings.stopRecording else strings.recordVoice,
                        tint = if (isRecording) Color.White else customColors.micIdleIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 2. MIDDLE: Rounded Text Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 110.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(customColors.inputBackground)
                        .border(1.dp, customColors.inputBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = text,
                                onValueChange = onTextChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("thought_text_field"),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = customColors.textPrimary,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp
                                ),
                                cursorBrush = SolidColor(customColors.releaseButtonColor),
                                decorationBox = { innerTextField ->
                                    if (text.isEmpty()) {
                                        Text(
                                            text = strings.placeholder.lines().firstOrNull() ?: strings.placeholder,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = customColors.textMuted,
                                                fontSize = 14.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        // Clear button if user has entered text
                        if (text.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = customColors.textMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onTextChanged("") }
                            )
                        }
                    }
                }

                // 3. RIGHT: "Отпустить" (Release/Send) Button
                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .scale(animatedReleaseScale)
                        .shadow(
                            elevation = if (hasContent) 10.dp else 2.dp,
                            shape = RoundedCornerShape(23.dp),
                            spotColor = if (hasContent) Color(0x500284C7) else Color(0x20000000)
                        )
                        .clip(RoundedCornerShape(23.dp))
                        .background(
                            brush = if (hasContent) {
                                Brush.horizontalGradient(customColors.releaseButtonGradient)
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        customColors.releaseButtonColor.copy(alpha = 0.55f),
                                        customColors.releaseButtonColor.copy(alpha = 0.45f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0x50FFFFFF), Color(0x10FFFFFF))
                            ),
                            shape = RoundedCornerShape(23.dp)
                        )
                        .clickable(
                            interactionSource = releaseInteractionSource,
                            indication = null,
                            onClick = onReleaseClick
                        )
                        .padding(horizontal = 14.dp)
                        .testTag("release_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.releaseButton,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .offset(
                                    x = planeFlightOffset.dp,
                                    y = (-planeFlightOffset).dp
                                )
                        )
                    }
                }
            }
        }
    }
}
