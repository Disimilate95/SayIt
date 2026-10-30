package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SayItStrings
import com.example.ui.theme.LocalSayItColors

@Composable
fun DisclaimerDialog(
    strings: SayItStrings,
    onDismiss: () -> Unit
) {
    val customColors = LocalSayItColors.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (customColors.isDark) Color(0xF2111827) else Color(0xFDF8FAFC)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(
                    width = 1.dp,
                    color = customColors.cardBorder,
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("disclaimer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title in warm coral accent
                Text(
                    text = strings.disclaimerTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = customColors.accentCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )

                // Exact requested disclaimer text
                Text(
                    text = strings.disclaimerBody,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = customColors.textPrimary.copy(alpha = 0.90f),
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bright rounded button "Понятно, начинаем"
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.releaseButtonColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("disclaimer_confirm_button")
                ) {
                    Text(
                        text = strings.disclaimerButton,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                // Powered by tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
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
