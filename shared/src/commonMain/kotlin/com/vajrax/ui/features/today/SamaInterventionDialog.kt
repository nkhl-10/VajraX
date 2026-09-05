package com.vajrax.ui.features.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme

/**
 * Phase 10: Sāma (Align) Interceptor Dialog.
 * Triggered on 'Skip'. Replaces shame/guilt with identity preservation.
 * Dynamically supports Light and Dark modes.
 */
@Composable
fun SamaInterventionDialog(
    practiceTitle: String,
    minimumMinutes: Int,
    onAcceptMinimum: () -> Unit,
    onConfirmSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LuminaTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "KEEP THE IDENTITY ALIVE",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "You are building the identity of someone who executes $practiceTitle.",
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Can you do just $minimumMinutes minutes instead of the full target?",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAcceptMinimum,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Do $minimumMinutes Min Minimum", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onConfirmSkip) {
                Text("Skip Anyway", color = colors.onSurfaceVariant)
            }
        }
    )
}
