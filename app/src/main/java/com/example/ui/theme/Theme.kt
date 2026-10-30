package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SayItCustomColors(
    val bgGradient: List<Color>,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val bubbleGradient: List<Color>,
    val bubbleTextColor: Color,
    val bubbleTimestampColor: Color,
    val releaseButtonGradient: List<Color>,
    val releaseButtonColor: Color,
    val micIdleBackground: Color,
    val micIdleIconColor: Color,
    val micRecordColor: Color,
    val micRecordGlow: Color,
    val dockBackground: Color,
    val dockBorder: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val pillBackground: Color,
    val pillBorder: Color,
    val accentCyan: Color,
    val accentCoral: Color,
    val isDark: Boolean
)

val LocalSayItColors = staticCompositionLocalOf {
    SayItCustomColors(
        bgGradient = listOf(LightBgStart, LightBgMiddle, LightBgEnd),
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textMuted = LightTextMuted,
        bubbleGradient = listOf(BubbleAzureStart, BubbleAzureEnd),
        bubbleTextColor = BubbleTextColor,
        bubbleTimestampColor = BubbleTimestampColor,
        releaseButtonGradient = listOf(BubbleAzureStart, BubbleAzureEnd),
        releaseButtonColor = AccentSkyBlue,
        micIdleBackground = LightMicIdleBg,
        micIdleIconColor = LightMicIdleIcon,
        micRecordColor = AccentRedRecord,
        micRecordGlow = AccentRedRecordGlow,
        dockBackground = LightDockBg,
        dockBorder = LightDockBorder,
        cardBackground = LightCardBg,
        cardBorder = LightCardBorder,
        inputBackground = LightInputBg,
        inputBorder = LightInputBorder,
        pillBackground = LightPillBg,
        pillBorder = LightPillBorder,
        accentCyan = AccentCyanGlow,
        accentCoral = AccentCoral,
        isDark = false
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = AccentSkyBlue,
    secondary = AccentCyanGlow,
    background = DarkBgMiddle,
    surface = DarkCardBg,
    onPrimary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = AccentSkyBlue,
    secondary = AccentCyanGlow,
    background = LightBgMiddle,
    surface = LightCardBg,
    onPrimary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun SayItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) {
        SayItCustomColors(
            bgGradient = listOf(DarkBgStart, DarkBgMiddle, DarkBgEnd),
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            bubbleGradient = listOf(BubbleAzureStart, BubbleAzureEnd),
            bubbleTextColor = BubbleTextColor,
            bubbleTimestampColor = BubbleTimestampColor,
            releaseButtonGradient = listOf(BubbleAzureStart, BubbleAzureEnd),
            releaseButtonColor = AccentSkyBlue,
            micIdleBackground = DarkMicIdleBg,
            micIdleIconColor = DarkMicIdleIcon,
            micRecordColor = AccentRedRecord,
            micRecordGlow = AccentRedRecordGlow,
            dockBackground = DarkDockBg,
            dockBorder = DarkDockBorder,
            cardBackground = DarkCardBg,
            cardBorder = DarkCardBorder,
            inputBackground = DarkInputBg,
            inputBorder = DarkInputBorder,
            pillBackground = DarkPillBg,
            pillBorder = DarkPillBorder,
            accentCyan = AccentCyanGlow,
            accentCoral = AccentCoral,
            isDark = true
        )
    } else {
        SayItCustomColors(
            bgGradient = listOf(LightBgStart, LightBgMiddle, LightBgEnd),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            bubbleGradient = listOf(BubbleAzureStart, BubbleAzureEnd),
            bubbleTextColor = BubbleTextColor,
            bubbleTimestampColor = BubbleTimestampColor,
            releaseButtonGradient = listOf(BubbleAzureStart, BubbleAzureEnd),
            releaseButtonColor = AccentSkyBlue,
            micIdleBackground = LightMicIdleBg,
            micIdleIconColor = LightMicIdleIcon,
            micRecordColor = AccentRedRecord,
            micRecordGlow = AccentRedRecordGlow,
            dockBackground = LightDockBg,
            dockBorder = LightDockBorder,
            cardBackground = LightCardBg,
            cardBorder = LightCardBorder,
            inputBackground = LightInputBg,
            inputBorder = LightInputBorder,
            pillBackground = LightPillBg,
            pillBorder = LightPillBorder,
            accentCyan = AccentCyanGlow,
            accentCoral = AccentCoral,
            isDark = false
        )
    }

    val materialColors = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalSayItColors provides customColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = AppTypography,
            content = content
        )
    }
}
