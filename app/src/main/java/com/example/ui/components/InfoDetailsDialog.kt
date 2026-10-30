package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.InfoDialogType
import com.example.model.SayItStrings
import com.example.ui.theme.LocalSayItColors

@Composable
fun InfoDetailsDialog(
    type: InfoDialogType,
    strings: SayItStrings,
    onDismiss: () -> Unit
) {
    val customColors = LocalSayItColors.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (customColors.isDark) Color(0xF2111827) else Color(0xFDF8FAFC)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(1.dp, customColors.cardBorder, RoundedCornerShape(28.dp))
                .testTag("info_details_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (type) {
                    InfoDialogType.SUPPORT -> {
                        Text(
                            text = strings.support,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = customColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )

                        Text(
                            text = strings.supportIntro,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = customColors.textSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        )

                        // Emergency hotlines list
                        strings.hotlines.forEach { hotline ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(customColors.pillBackground)
                                    .border(1.dp, customColors.pillBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (hotline.number.startsWith("http") || hotline.number.contains(".info")) {
                                            val url = if (hotline.number.startsWith("http")) hotline.number else "https://${hotline.number}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hotline.number}")).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = hotline.number,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = customColors.releaseButtonColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    modifier = Modifier.width(68.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = hotline.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = customColors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = hotline.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = customColors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Warm supportive quote card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(customColors.pillBackground)
                                .border(1.dp, customColors.pillBorder, RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = customColors.releaseButtonColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = strings.supportQuote,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = customColors.textPrimary.copy(alpha = 0.90f),
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action buttons row: "Написать команде" and "Закрыть"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:support@sayit.app")
                                        putExtra(Intent.EXTRA_SUBJECT, "SayIt Support")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MailOutline,
                                    contentDescription = null,
                                    tint = customColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.supportWriteTeam,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = customColors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = customColors.releaseButtonColor,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = strings.closeButton,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }
                    }
                    InfoDialogType.ABOUT, InfoDialogType.HOW_TO_USE, InfoDialogType.DISCLAIMER -> {
                        val title = if (type == InfoDialogType.ABOUT) strings.aboutApp else strings.howToUse
                        val body = if (type == InfoDialogType.ABOUT) strings.aboutAppBody else strings.howToUseBody

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = customColors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )

                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = customColors.textSecondary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = customColors.releaseButtonColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("info_dialog_close_button")
                        ) {
                            Text(
                                text = strings.closeButton,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }

                // Footer tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✦ ${strings.poweredByText}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = customColors.textMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
