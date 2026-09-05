package com.vajrax.ui.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.ui.theme.LuminaTheme

/**
 * Enhanced Evidence Capture Bottom Sheet.
 * Fast 1-tap reflection rating (Easy, Okay, Hard) + Optional Note.
 * Dynamically supports Light and Dark modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitEvidence: (note: String, rating: ReflectionRating?) -> Unit
) {
    val colors = LuminaTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var noteText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf<ReflectionRating?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "LOG EVIDENCE",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How was this session?",
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1-Tap Reflection Rating Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    ReflectionRating.EASY to "Easy",
                    ReflectionRating.OKAY to "Okay",
                    ReflectionRating.HARD to "Hard"
                ).forEach { (rating, label) ->
                    val isSelected = selectedRating == rating
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colors.primary else colors.surfaceContainerLow)
                            .border(1.dp, if (isSelected) colors.primary else colors.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { selectedRating = rating }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) colors.onPrimary else colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live AI Human Touch Reflection Feedback
            if (selectedRating != null) {
                val feedback = remember(selectedRating) {
                    com.vajrax.domain.ai.AiHumanTouchEngine().getEvidenceReflectionFeedback(selectedRating, "this practice")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryContainer)
                        .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💡", fontSize = 13.sp)
                        Text(
                            text = feedback,
                            color = colors.primary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }


            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What did you learn or accomplish? (Optional)", color = colors.onSurfaceVariant, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainerLow,
                    unfocusedContainerColor = colors.surfaceContainerLow,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedIndicatorColor = colors.primary,
                    unfocusedIndicatorColor = colors.outlineVariant
                ),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onSubmitEvidence("", selectedRating) }) {
                    Text("Skip Note", color = colors.onSurfaceVariant)
                }

                Button(
                    onClick = {
                        onSubmitEvidence(noteText, selectedRating)
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Evidence", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
