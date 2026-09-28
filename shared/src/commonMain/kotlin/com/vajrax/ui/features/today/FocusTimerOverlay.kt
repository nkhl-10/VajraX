@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.vajrax.ui.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.core.time.TimeFormat
import com.vajrax.ui.designsystem.PrimaryButton
import com.vajrax.ui.designsystem.ProgressRing
import com.vajrax.ui.designsystem.SecondaryButton
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * Distraction-free focus session. Elapsed time is derived from the persisted start instant
 * (see [TimerState]), so it keeps counting across rotation, backgrounding and app restarts.
 */
@Composable
fun FocusTimerOverlay(
    timer: TimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = LuminaTheme.colors
    var nowMs by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(timer.running) {
        while (true) {
            nowMs = Clock.System.now().toEpochMilliseconds()
            delay(500)
        }
    }
    val elapsed = timer.elapsedMs(nowMs)
    val totalSeconds = elapsed / 1000
    val clockText = "${(totalSeconds / 60).toString().padStart(2, '0')}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    val target = timer.item.habit.durationMinutes.coerceAtLeast(1)
    val minimum = timer.item.habit.minimumMinutes
    val minutes = (totalSeconds / 60).toInt()

    Box(
        Modifier.fillMaxSize().background(colors.background).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = VxSpace.xxl, vertical = VxSpace.xl)
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(VxSpace.xxl))
            Text("FOCUS SESSION", style = MaterialTheme.typography.labelSmall, color = colors.primary)
            Spacer(Modifier.height(VxSpace.sm))
            Text(timer.item.habit.title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Text(
                "Target ${TimeFormat.duration(target)} · minimum ${TimeFormat.duration(minimum)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            ProgressRing(fraction = elapsed / (target * 60_000f), size = 240.dp, stroke = 12.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        clockText,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 52.sp, fontFeatureSettings = "tnum"),
                        color = colors.onSurface,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                    Text(
                        when {
                            !timer.running -> "Paused"
                            minutes >= target -> "Target reached"
                            minutes >= minimum -> "Minimum reached"
                            else -> "Stay with it"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                SecondaryButton(
                    if (timer.running) "Pause" else "Resume",
                    onClick = if (timer.running) onPause else onResume,
                    modifier = Modifier.weight(1f),
                    icon = if (timer.running) VxIcons.Timer else VxIcons.Play
                )
                PrimaryButton("Finish", onFinish, Modifier.weight(1f), icon = VxIcons.Check)
            }
            TextButton(onClick = onCancel) { Text("Discard session", color = colors.onSurfaceVariant) }
        }
    }
}
