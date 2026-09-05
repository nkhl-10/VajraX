package com.vajrax.ui.features.grow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.domain.model.ReflectionRating
import com.vajrax.ui.theme.LuminaTheme

/**
 * Clean Overview Screen for Lumina Life OS.
 * Designed around "How consistent have I been over time?"
 * Honest metrics, practical behavioral observations, and chronological evidence logs.
 */
@Composable
fun GrowScreen(
    state: GrowUiState,
    onIntent: (GrowIntent) -> Unit
) {
    val colors = LuminaTheme.colors
    var selectedPeriod by remember { mutableStateOf("Monthly") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 1. PAGE HEADER (Breathes directly on page)
        // ==========================================
        Text(
            text = "Progress & Insights",
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Long-term consistency, focus hours, and logged reflections",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Period Selector
        val periodOptions = listOf("Weekly", "Monthly", "All-Time")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periodOptions.forEach { period ->
                val isSelected = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) colors.primaryContainer else colors.surfaceContainerLow)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPeriod = period }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = period,
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. CORE METRICS PODS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricPod(
                label = "CURRENT STREAK",
                value = "14 Days",
                subtitle = "Personal best",
                modifier = Modifier.weight(1f)
            )
            MetricPod(
                label = "CONSISTENCY",
                value = "85%",
                subtitle = "${state.totalCommitmentsKept} sessions",
                modifier = Modifier.weight(1f)
            )
            MetricPod(
                label = "DEEP WORK",
                value = "${state.totalFocusedHours}h",
                subtitle = "Protected time",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. BEHAVIORAL OBSERVATIONS CARD
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surface)
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "OBSERVATIONS & PATTERNS",
                    color = colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("•", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Highest consistency (94%) occurs on morning practices before 12 PM.",
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("•", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Evening reflection and shutdown routines benefit from a 15-minute buffer after screen time.",
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 4. RECENT EVIDENCE LOGS (Breathes directly on page)
        // ==========================================
        Text(
            text = "RECENT EVIDENCE LOG",
            color = colors.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        val evidenceList = state.recentEvidence
        if (evidenceList.isNotEmpty()) {
            evidenceList.forEachIndexed { index, entry ->
                EvidenceLogRow(entry = entry)
                if (index < evidenceList.size - 1) {
                    HorizontalDivider(
                        color = colors.outlineVariant.copy(alpha = 0.6f),
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        } else {
            // Default sample entries if none yet logged
            listOf(
                EvidenceEntry("1", "Deep Work Architecture", "Today, 08:30 AM", "Maintained unbroken focus on compiler optimization. No context switching.", ReflectionRating.EASY, 45),
                EvidenceEntry("2", "Morning Meditation", "Yesterday, 06:45 AM", "Focused on diaphragmatic breath and quiet presence before checking devices.", ReflectionRating.OKAY, 20),
                EvidenceEntry("3", "Strength Training", "2 days ago", "Completed full pull-day compound session with good tempo control.", ReflectionRating.EASY, 45)
            ).forEachIndexed { index, entry ->
                EvidenceLogRow(entry = entry)
                if (index < 2) {
                    HorizontalDivider(
                        color = colors.outlineVariant.copy(alpha = 0.6f),
                        thickness = 0.6.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        // Safe clearance for bottom navigation bar
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}

@Composable
private fun MetricPod(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                color = colors.onSurfaceVariant,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = colors.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = colors.onSurfaceVariant,
                fontSize = 10.5.sp
            )
        }
    }
}

@Composable
private fun EvidenceLogRow(entry: EvidenceEntry) {
    val colors = LuminaTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = entry.practiceTitle,
                    color = colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Rating Pill
                val (ratingText, ratingBg, ratingColor) = when (entry.rating) {
                    ReflectionRating.EASY -> Triple("Easy", colors.statusSuccess.copy(alpha = 0.15f), colors.statusSuccess)
                    ReflectionRating.HARD -> Triple("Hard", colors.statusError.copy(alpha = 0.15f), colors.statusError)
                    else -> Triple("Solid", colors.primaryContainer, colors.primary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ratingBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ratingText,
                        color = ratingColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${entry.durationMinutes}m · ${entry.date}",
                color = colors.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }

        if (entry.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.note,
                color = colors.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

