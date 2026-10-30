package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.ui.theme.LocalSayItColors

data class RecipientPreset(
    val emoji: String,
    val ru: String,
    val ky: String,
    val en: String
)

val RECIPIENT_PRESETS = listOf(
    RecipientPreset("🌌", "В пустоту", "Ааламга", "Into the void"),
    RecipientPreset("⏳", "Себе из прошлого", "Өткөндөгү өзүмө", "To my past self"),
    RecipientPreset("🔮", "Себе в будущее", "Келечектеги өзүмө", "To my future self"),
    RecipientPreset("💔", "Бывшему / Бывшей", "Мурунку сүйгөнүмө", "To an ex"),
    RecipientPreset("👥", "Другу / Подруге", "Досума", "To a friend"),
    RecipientPreset("🏡", "Родителям / Семье", "Үй-бүлөмө / Ата-энеме", "To parents / family"),
    RecipientPreset("💼", "Начальнику / Коллеге", "Жетекчиме / Кесиптешиме", "To boss / colleague"),
    RecipientPreset("🕊️", "Кого больше нет рядом", "Жаныбызда жок адамга", "To someone no longer here"),
    RecipientPreset("⚡", "Обидчику", "Таарынткан адамга", "To someone who hurt me")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipientPickerDialog(
    currentRecipient: String,
    currentLanguage: AppLanguage,
    onRecipientSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val customColors = LocalSayItColors.current
    val scrollState = rememberScrollState()

    var selectedRecipient by remember { mutableStateOf(currentRecipient) }
    var customInputText by remember {
        mutableStateOf(
            if (RECIPIENT_PRESETS.none { it.getLabel(currentLanguage) == currentRecipient }) {
                currentRecipient
            } else ""
        )
    }

    val title = when (currentLanguage) {
        AppLanguage.KYRGYZ -> "Кимге ойду жөнөтөсүз?"
        AppLanguage.RUSSIAN -> "Кому адресована мысль?"
        AppLanguage.ENGLISH -> "Who is this thought for?"
    }

    val subtitle = when (currentLanguage) {
        AppLanguage.KYRGYZ -> "Адресат сиздин кыялыңызда гана болот. Билдирүү эч жакка жөнөтүлбөйт жана 2 секунддан кийин өчүп калат."
        AppLanguage.RUSSIAN -> "Адресат существует только в вашем воображении. Сообщение никуда не отправляется и растворяется через 2 секунды."
        AppLanguage.ENGLISH -> "The recipient exists only in your imagination. The message is never sent anywhere and dissolves in 2 seconds."
    }

    val customPlaceholder = when (currentLanguage) {
        AppLanguage.KYRGYZ -> "Өз вариантыңыз (мисалы: Апама, Жакыныма)..."
        AppLanguage.RUSSIAN -> "Свой вариант (например: Маме, Сергею)..."
        AppLanguage.ENGLISH -> "Custom name (e.g. Mom, Alex, The Universe)..."
    }

    val applyButtonText = when (currentLanguage) {
        AppLanguage.KYRGYZ -> "Тандоо"
        AppLanguage.RUSSIAN -> "Выбрать адресата"
        AppLanguage.ENGLISH -> "Select Recipient"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (customColors.isDark) Color(0xF2111827) else Color(0xFDF8FAFC)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .border(1.dp, customColors.cardBorder, RoundedCornerShape(28.dp))
                .testTag("recipient_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(customColors.releaseButtonColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = customColors.releaseButtonColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = customColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = customColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = customColors.textSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                )

                // Chips FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RECIPIENT_PRESETS.forEach { preset ->
                        val label = preset.getLabel(currentLanguage)
                        val isSelected = selectedRecipient == label && customInputText.isEmpty()

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) customColors.releaseButtonColor
                                    else customColors.pillBackground
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) customColors.releaseButtonColor else customColors.pillBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    selectedRecipient = label
                                    customInputText = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = preset.emoji,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White else customColors.textPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom text input
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.KYRGYZ -> "Же өзүңүз жазыңыз:"
                            AppLanguage.RUSSIAN -> "Или введите своё имя адресата:"
                            AppLanguage.ENGLISH -> "Or enter a custom recipient:"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = customColors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(customColors.inputBackground)
                            .border(1.dp, customColors.inputBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                    ) {
                        BasicTextField(
                            value = customInputText,
                            onValueChange = {
                                customInputText = it
                                if (it.isNotBlank()) {
                                    selectedRecipient = it.trim()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = customColors.textPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(customColors.releaseButtonColor),
                            decorationBox = { innerTextField ->
                                if (customInputText.isEmpty()) {
                                    Text(
                                        text = customPlaceholder,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = customColors.textMuted,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Apply button
                Button(
                    onClick = {
                        val finalChoice = if (customInputText.isNotBlank()) customInputText.trim() else selectedRecipient
                        onRecipientSelected(finalChoice)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.releaseButtonColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_recipient_button")
                ) {
                    Text(
                        text = applyButtonText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

private fun RecipientPreset.getLabel(language: AppLanguage): String = when (language) {
    AppLanguage.KYRGYZ -> ky
    AppLanguage.RUSSIAN -> ru
    AppLanguage.ENGLISH -> en
}
