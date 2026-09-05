package com.vajrax.ui.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme
import kotlinx.coroutines.delay

/**
 * Phase 10: Live Focus Timer Session.
 * Clean, distraction-free timer with Pause, Finish, and Minimum fallback.
 * Dynamically supports Light & Dark modes.
 */
@Composable
fun FocusTimerDialog(
    practiceTitle: String,
    targetDurationMinutes: Int,
    minimumDurationMinutes: Int,
    onFinish: (elapsedMinutes: Int) -> Unit,
    onCancel: () -> Unit
) {
    val colors = LuminaTheme.colors
    var elapsedSeconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "PROTECTED FOCUS SESSION",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = practiceTitle.uppercase(),
                    color = colors.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Target: ${targetDurationMinutes}m · Min: ${minimumDurationMinutes}m",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            // Big Timer Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    color = colors.primary,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (minutes >= minimumDurationMinutes) "✓ Minimum reached" else "Building momentum...",
                    color = if (minutes >= minimumDurationMinutes) colors.statusSuccess else colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surfaceContainerHigh,
                            contentColor = colors.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isRunning) "Pause" else "Resume", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val finalMinutes = if (minutes > 0) minutes else 1
                            onFinish(finalMinutes)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish Session", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onCancel) {
                    Text("Exit Timer", color = colors.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
