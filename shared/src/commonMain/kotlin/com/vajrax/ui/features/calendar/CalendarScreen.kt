package com.vajrax.ui.features.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.theme.LuminaTheme

/**
 * Human-Crafted Calendar / Task Matrix Screen for Lumina Life OS.
 * Faithfully matches the reference design with split view:
 * Left side: Tasks list with rounded icon containers & time ranges
 * Right side: Wed, 21 Jan header, 5 synchronized day columns, and aligned check circles.
 */
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onIntent: (CalendarIntent) -> Unit
) {
    val colors = LuminaTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // TOP HEADER: "Tasks" on Left | "Wed, 21 Jan" + Days on Right
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Left: "Tasks" Header
            Text(
                text = "Tasks",
                color = colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier
                    .weight(1.1f)
                    .padding(start = 4.dp, top = 4.dp)
            )

            // Right: Date & 5-Day Columns Header
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.selectedDateText,
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5 Days Header Row (19 Mon, 20 Tue, 21 Wed [Active], 22 Thu, 23 Fri)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.days.forEach { day ->
                        val isSelected = day.index == state.selectedDayOfWeek

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onIntent(CalendarIntent.SelectDay(day.index)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isSelected) {
                                    // Highlighted Circle for Active Day (e.g. 21 Wed)
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${day.dateNumber}",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = day.dayName,
                                        color = colors.primary,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "${day.dateNumber}",
                                        color = colors.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = day.dayName,
                                        color = colors.outline,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // MAIN BODY: SPLIT VIEW WITH VERTICAL DIVIDER
        // ==========================================
        state.tasks.forEachIndexed { index, task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Task Icon + Title + Time
                Row(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tinted Rounded Square Icon Container
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        TaskIconVector(taskId = task.id, tint = colors.primary)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = task.title,
                            color = colors.onSurface,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = task.timeSubtitle,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Center Vertical Hairline Divider
                Box(
                    modifier = Modifier
                        .width(0.6.dp)
                        .fillMaxHeight()
                        .background(colors.outlineVariant.copy(alpha = 0.7f))
                )

                // Right Column: 5 Aligned Check Circles
                val daysCompleted = listOf(
                    task.mondayCompleted,
                    task.tuesdayCompleted,
                    task.wednesdayCompleted,
                    task.thursdayCompleted,
                    task.fridayCompleted
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysCompleted.forEachIndexed { dayIdx, isCompleted ->
                        val isActiveDay = dayIdx == state.selectedDayOfWeek

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            HabitCheckCircle(
                                isCompleted = isCompleted,
                                isActiveDay = isActiveDay,
                                onClick = { onIntent(CalendarIntent.ToggleTaskDay(task.id, dayIdx)) }
                            )
                        }
                    }
                }
            }
        }

        // Safe clearance for bottom navigation bar
        Spacer(modifier = Modifier.navigationBarsPadding().height(96.dp))
    }
}

@Composable
private fun HabitCheckCircle(
    isCompleted: Boolean,
    isActiveDay: Boolean,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors

    when {
        // Active Day Completed -> Solid Royal Indigo Circle with White Check
        isCompleted && isActiveDay -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Past Day Completed -> Subtle Grey Circle Outline with Grey Check
        isCompleted && !isActiveDay -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.2.dp, colors.outline.copy(alpha = 0.6f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = colors.outline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Incomplete / Future Day -> Empty Circle Outline
        else -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.2.dp, colors.outlineVariant, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
            )
        }
    }
}

@Composable
private fun TaskIconVector(
    taskId: String,
    tint: Color,
    size: Dp = 18.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sx = w / 24f
        val sy = h / 24f
        val strokeWidth = 1.9f * sx

        when (taskId) {
            "1" -> { // Clock (Daily Review)
                drawCircle(color = tint, radius = 9f * sx, center = Offset(12f * sx, 12f * sy), style = Stroke(width = strokeWidth))
                drawLine(color = tint, start = Offset(12f * sx, 12f * sy), end = Offset(12f * sx, 7f * sy), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(12f * sx, 12f * sy), end = Offset(15.5f * sx, 12f * sy), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            }
            "2" -> { // Briefcase (Deep Work)
                val body = RoundRect(Rect(4f * sx, 8f * sy, 20f * sx, 20f * sy), CornerRadius(2.5f * sx, 2.5f * sy))
                val handle = RoundRect(Rect(9f * sx, 4.5f * sy, 15f * sx, 8f * sy), CornerRadius(1.5f * sx, 1.5f * sy))
                drawPath(Path().apply { addRoundRect(body) }, color = tint, style = Stroke(width = strokeWidth))
                drawPath(Path().apply { addRoundRect(handle) }, color = tint, style = Stroke(width = strokeWidth))
                drawLine(color = tint, start = Offset(4f * sx, 13f * sy), end = Offset(20f * sx, 13f * sy), strokeWidth = strokeWidth * 0.8f)
            }
            "3" -> { // Location Pin (Lunch Walk)
                val pinPath = Path().apply {
                    moveTo(12f * sx, 21f * sy)
                    cubicTo(6f * sx, 14f * sy, 5f * sx, 10f * sy, 5f * sx, 7.5f * sy)
                    arcTo(Rect(5f * sx, 3f * sy, 19f * sx, 17f * sy), -180f, 180f, false)
                    cubicTo(19f * sx, 10f * sy, 18f * sx, 14f * sy, 12f * sx, 21f * sy)
                    close()
                }
                drawPath(pinPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawCircle(color = tint, radius = 2.5f * sx, center = Offset(12f * sx, 9f * sy), style = Stroke(width = strokeWidth))
            }
            "4" -> { // Book (Learning)
                val bookPath = Path().apply {
                    moveTo(12f * sx, 19f * sy)
                    cubicTo(8f * sx, 17f * sy, 4f * sx, 17f * sy, 3f * sx, 17.5f * sy)
                    lineTo(3f * sx, 6.5f * sy)
                    cubicTo(4f * sx, 6f * sy, 8f * sx, 6f * sy, 12f * sx, 8f * sy)
                    cubicTo(16f * sx, 6f * sy, 20f * sx, 6f * sy, 21f * sx, 6.5f * sy)
                    lineTo(21f * sx, 17.5f * sy)
                    cubicTo(20f * sx, 17f * sy, 16f * sx, 17f * sy, 12f * sx, 19f * sy)
                }
                drawPath(bookPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawLine(color = tint, start = Offset(12f * sx, 8f * sy), end = Offset(12f * sx, 19f * sy), strokeWidth = strokeWidth)
            }
            "5" -> { // Lightning Bolt (Workout)
                val boltPath = Path().apply {
                    moveTo(13f * sx, 2.5f * sy)
                    lineTo(6f * sx, 13f * sy)
                    lineTo(12f * sx, 13f * sy)
                    lineTo(11f * sx, 21.5f * sy)
                    lineTo(18f * sx, 11f * sy)
                    lineTo(12f * sx, 11f * sy)
                    close()
                }
                drawPath(boltPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "6" -> { // Smartphone / Screen (Reading)
                val phone = RoundRect(Rect(6.5f * sx, 3.5f * sy, 17.5f * sx, 20.5f * sy), CornerRadius(3f * sx, 3f * sy))
                drawPath(Path().apply { addRoundRect(phone) }, color = tint, style = Stroke(width = strokeWidth))
                drawLine(color = tint, start = Offset(10f * sx, 18f * sy), end = Offset(14f * sx, 18f * sy), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            }
            "7" -> { // Edit / Note (Plan Tomorrow)
                val editPath = Path().apply {
                    moveTo(4f * sx, 20f * sy)
                    lineTo(8f * sx, 20f * sy)
                    lineTo(19f * sx, 9f * sy)
                    lineTo(15f * sx, 5f * sy)
                    lineTo(4f * sx, 16f * sy)
                    close()
                }
                drawPath(editPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "8" -> { // Moon (Sleep)
                val moonPath = Path().apply {
                    moveTo(12f * sx, 3.5f * sy)
                    cubicTo(7f * sx, 3.5f * sy, 3.5f * sx, 7f * sy, 3.5f * sx, 12f * sy)
                    cubicTo(3.5f * sx, 17f * sy, 7.5f * sx, 20.5f * sy, 12.5f * sy, 20.5f * sy)
                    cubicTo(16f * sx, 20.5f * sy, 19f * sx, 18.5f * sy, 20.5f * sx, 15.5f * sy)
                    cubicTo(14.5f * sx, 16f * sy, 9.5f * sx, 11f * sy, 10f * sx, 5f * sy)
                    cubicTo(10.7f * sx, 4.3f * sy, 11.4f * sx, 3.8f * sy, 12f * sx, 3.5f * sy)
                    close()
                }
                drawPath(moonPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}


