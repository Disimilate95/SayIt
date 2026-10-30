package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.InfoDialogType
import com.example.model.SayItStrings
import com.example.model.ThemeMode
import com.example.ui.theme.LocalSayItColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSheet(
    sheetState: SheetState,
    currentLanguage: AppLanguage,
    currentThemeMode: ThemeMode,
    strings: SayItStrings,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onOpenInfoDialog: (InfoDialogType) -> Unit,
    onShareApp: () -> Unit,
    onDismiss: () -> Unit
) {
    val customColors = LocalSayItColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (customColors.isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
        dragHandle = null,
        modifier = Modifier.testTag("menu_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.menuTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = customColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = customColors.textPrimary
                    )
                }
            }

            // Language Selector Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.languageLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = customColors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) customColors.releaseButtonColor
                                    else customColors.pillBackground
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) customColors.releaseButtonColor else customColors.pillBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onLanguageSelected(lang) }
                                .padding(vertical = 10.dp)
                                .testTag("lang_option_${lang.code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) Color.White else customColors.textPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // Theme Selector Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.themeLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = customColors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        title = strings.themeSystem,
                        icon = Icons.Default.SettingsBrightness,
                        isSelected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeSelected(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        title = strings.themeLight,
                        icon = Icons.Default.LightMode,
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        title = strings.themeDark,
                        icon = Icons.Default.DarkMode,
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        onClick = { onThemeModeSelected(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(
                color = customColors.cardBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Nav Actions
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MenuItemRow(
                    title = strings.howToUse,
                    icon = Icons.Default.HelpOutline,
                    onClick = {
                        onDismiss()
                        onOpenInfoDialog(InfoDialogType.HOW_TO_USE)
                    },
                    testTag = "menu_item_how_to_use"
                )

                MenuItemRow(
                    title = strings.aboutApp,
                    icon = Icons.Default.Info,
                    onClick = {
                        onDismiss()
                        onOpenInfoDialog(InfoDialogType.ABOUT)
                    },
                    testTag = "menu_item_about"
                )

                MenuItemRow(
                    title = strings.support,
                    icon = Icons.Default.Favorite,
                    onClick = {
                        onDismiss()
                        onOpenInfoDialog(InfoDialogType.SUPPORT)
                    },
                    testTag = "menu_item_support"
                )

                MenuItemRow(
                    title = strings.shareApp,
                    icon = Icons.Default.Share,
                    onClick = {
                        onDismiss()
                        onShareApp()
                    },
                    testTag = "menu_item_share"
                )
            }

            // Privacy pledge badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(customColors.pillBackground)
                    .border(1.dp, customColors.pillBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = customColors.releaseButtonColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "100% Offline & Ephemeral • Zero Tracking",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = customColors.textSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalSayItColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) customColors.releaseButtonColor
                else customColors.pillBackground
            )
            .border(
                1.dp,
                if (isSelected) customColors.releaseButtonColor else customColors.pillBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else customColors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) Color.White else customColors.textPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun MenuItemRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val customColors = LocalSayItColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(customColors.pillBackground)
                .border(1.dp, customColors.pillBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customColors.releaseButtonColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = customColors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        )
    }
}
