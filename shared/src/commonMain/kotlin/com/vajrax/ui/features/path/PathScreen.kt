package com.vajrax.ui.features.path

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme

data class DiscoverTemplateItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val taskCount: Int,
    val frequency: String,
    val author: String? = null,
    val isBookmarked: Boolean = false
)

/**
 * 1:1 Figma-Faithful Discover Screen for Lumina Life OS.
 * Features Provided Templates and Community Templates with clean metadata and 'Use' CTA buttons.
 */
@Composable
fun PathScreen(
    state: PathUiState,
    onIntent: (PathIntent) -> Unit
) {
    val colors = LuminaTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf<String?>(null) }

    val providedTemplates = listOf(
        DiscoverTemplateItem("morning_discipline", "Morning Discipline", "Build a structured morning routine", 6, "Daily"),
        DiscoverTemplateItem("deep_work_block", "Deep Work Block", "Lock in focus and maximize high-output hours", 4, "Daily"),
        DiscoverTemplateItem("30_day_challenge", "30-Day Challenge", "A rigorous month-long discipline protocol", 8, "30 days"),
        DiscoverTemplateItem("6am_routine", "6 AM Routine", "Wake up early and win the morning", 5, "Daily")
    )

    val communityTemplates = listOf(
        DiscoverTemplateItem("evening_wind_down", "Evening Wind Down", "Slow down your mind for deep, restful recovery", 4, "Daily", author = "sarah", isBookmarked = true),
        DiscoverTemplateItem("fitness_starter_pack", "Fitness Starter Pack", "Essential daily habits for athletic consistency", 5, "Daily", author = "mike", isBookmarked = true)
    )

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
        // 1. HEADER (Title + Search Icon Button)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discover",
                color = colors.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp
            )

            // Search Icon Circle Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .border(1.dp, colors.outlineVariant.copy(alpha = 0.8f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Search action */ },
                contentAlignment = Alignment.Center
            ) {
                // Search Magnifier Icon
                androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
                    val stroke = 1.8.dp.toPx()
                    drawCircle(
                        color = Color(0xFF64748B),
                        radius = size.width * 0.35f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.42f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                    )
                    drawLine(
                        color = Color(0xFF64748B),
                        start = androidx.compose.ui.geometry.Offset(size.width * 0.67f, size.height * 0.67f),
                        end = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.92f),
                        strokeWidth = stroke,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 2. PROVIDED TEMPLATES
        // ==========================================
        Text(
            text = "PROVIDED TEMPLATES",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            providedTemplates.forEach { item ->
                FigmaTemplateCard(
                    item = item,
                    onUse = { onIntent(PathIntent.SelectPath(item.id)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ==========================================
        // 3. COMMUNITY TEMPLATES
        // ==========================================
        Text(
            text = "COMMUNITY TEMPLATES",
            color = Color(0xFF8B95A5),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            communityTemplates.forEach { item ->
                FigmaTemplateCard(
                    item = item,
                    onUse = { onIntent(PathIntent.SelectPath(item.id)) }
                )
            }
        }

        // Safe clearance for bottom navigation dock
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}

@Composable
private fun FigmaTemplateCard(
    item: DiscoverTemplateItem,
    onUse: () -> Unit
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Title & Use Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = colors.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )

                    if (item.author != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "by @${item.author}",
                            color = colors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.subtitle,
                        color = colors.onSurfaceVariant,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.isBookmarked) {
                        // Bookmark icon
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
                            val w = size.width
                            val h = size.height
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(2f, 1f)
                                lineTo(w - 2f, 1f)
                                lineTo(w - 2f, h - 1f)
                                lineTo(w / 2f, h * 0.65f)
                                lineTo(2f, h - 1f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF94A3B8),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 1.6.dp.toPx(),
                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onUse
                            )
                            .padding(horizontal = 22.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Use",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata Row: [list icon] 6 tasks   [clock icon] Daily
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Task list vector icon
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
                        val stroke = 1.5.dp.toPx()
                        drawLine(Color(0xFF94A3B8), androidx.compose.ui.geometry.Offset(1f, 3f), androidx.compose.ui.geometry.Offset(4f, 3f), strokeWidth = stroke)
                        drawLine(Color(0xFF94A3B8), androidx.compose.ui.geometry.Offset(7f, 3f), androidx.compose.ui.geometry.Offset(size.width, 3f), strokeWidth = stroke)
                        drawLine(Color(0xFF94A3B8), androidx.compose.ui.geometry.Offset(1f, 7.5f), androidx.compose.ui.geometry.Offset(4f, 7.5f), strokeWidth = stroke)
                        drawLine(Color(0xFF94A3B8), androidx.compose.ui.geometry.Offset(7f, 7.5f), androidx.compose.ui.geometry.Offset(size.width, 7.5f), strokeWidth = stroke)
                        drawLine(Color(0xFF94A3B8), androidx.compose.ui.geometry.Offset(1f, 12f), androidx.compose.ui.geometry.Offset(4f, 12f), strokeWidth = stroke)
                        drawLine(Color(0xFF94A3B8), androidx.compose.ui.geometry.Offset(7f, 12f), androidx.compose.ui.geometry.Offset(size.width, 12f), strokeWidth = stroke)
                    }
                    Text(
                        text = "${item.taskCount} tasks",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Clock vector icon
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
                        val stroke = 1.5.dp.toPx()
                        drawCircle(
                            color = Color(0xFF94A3B8),
                            radius = size.width * 0.44f,
                            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                        )
                        drawLine(
                            color = Color(0xFF94A3B8),
                            start = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                            end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.28f),
                            strokeWidth = stroke,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF94A3B8),
                            start = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height / 2f),
                            strokeWidth = stroke,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                    Text(
                        text = item.frequency,
                        color = colors.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}



