package com.vajrax.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vajrax.ui.navigation.NavTabType

/**
 * Custom SVG Vector Tab Icons drawn with precision Canvas paths matching the reference designs:
 * - HOME: Minimalist house outline with pitched roof and doorway arch.
 * - CALENDAR: Desk calendar pad with header divider and binder hooks.
 * - DISCOVER: Clean circular focus / discovery ring.
 * - REPORT: 3-bar analytical metric chart (short, tall, medium).
 * - PROFILE: User avatar outline with circular head and curved shoulder arc.
 */
@Composable
fun NavTabIcon(
    tabType: NavTabType,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    isSelected: Boolean = false
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        val w = this.size.width
        val h = this.size.height
        val sx = w / 24f
        val sy = h / 24f

        val strokeWidth = (if (isSelected) 2.1f else 1.85f) * sx

        when (tabType) {
            NavTabType.HOME -> {
                // Outline House with pitched roof and central door arch
                val housePath = Path().apply {
                    moveTo(5f * sx, 20.5f * sy)
                    lineTo(5f * sx, 10.5f * sy)
                    lineTo(12f * sx, 3.8f * sy)
                    lineTo(19f * sx, 10.5f * sy)
                    lineTo(19f * sx, 20.5f * sy)
                    lineTo(14.5f * sx, 20.5f * sy)
                    lineTo(14.5f * sx, 14.5f * sy)
                    arcTo(
                        rect = Rect(
                            left = 9.5f * sx,
                            top = 12.2f * sy,
                            right = 14.5f * sx,
                            bottom = 16.8f * sy
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    lineTo(9.5f * sx, 20.5f * sy)
                    close()
                }

                drawPath(
                    path = housePath,
                    color = tint,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            NavTabType.CALENDAR -> {
                // Calendar Body
                val calendarRect = RoundRect(
                    rect = Rect(
                        left = 4f * sx,
                        top = 6f * sy,
                        right = 20f * sx,
                        bottom = 20.5f * sy
                    ),
                    cornerRadius = CornerRadius(3.5f * sx, 3.5f * sy)
                )
                val framePath = Path().apply {
                    addRoundRect(calendarRect)
                }

                drawPath(
                    path = framePath,
                    color = tint,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Divider Line
                drawLine(
                    color = tint,
                    start = Offset(4.2f * sx, 11f * sy),
                    end = Offset(19.8f * sx, 11f * sy),
                    strokeWidth = strokeWidth * 0.9f
                )

                // Left Hook / Binder Ring
                drawLine(
                    color = tint,
                    start = Offset(8f * sx, 3f * sy),
                    end = Offset(8f * sx, 7f * sy),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // Right Hook / Binder Ring
                drawLine(
                    color = tint,
                    start = Offset(16f * sx, 3f * sy),
                    end = Offset(16f * sx, 7f * sy),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            NavTabType.DISCOVER -> {
                // Compass Circle
                drawCircle(
                    color = tint,
                    radius = 8f * sx,
                    center = Offset(12f * sx, 12f * sy),
                    style = Stroke(width = strokeWidth)
                )
                // Compass Needle
                val needlePath = Path().apply {
                    moveTo(15.2f * sx, 8.8f * sy)
                    lineTo(12.8f * sx, 12.8f * sy)
                    lineTo(8.8f * sx, 15.2f * sy)
                    lineTo(11.2f * sx, 11.2f * sy)
                    close()
                }
                drawPath(
                    path = needlePath,
                    color = tint,
                    style = Stroke(width = strokeWidth * 0.9f, join = StrokeJoin.Round)
                )
            }

            NavTabType.REPORT -> {
                // 3 Vertical Bars with Rounded Caps (Short, Tall, Medium)
                val barWidth = 2.4f * sx

                // Bar 1 (Short - Left)
                drawLine(
                    color = tint,
                    start = Offset(6f * sx, 13.8f * sy),
                    end = Offset(6f * sx, 19.5f * sy),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )

                // Bar 2 (Tall - Center)
                drawLine(
                    color = tint,
                    start = Offset(12f * sx, 4.5f * sy),
                    end = Offset(12f * sx, 19.5f * sy),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )

                // Bar 3 (Medium - Right)
                drawLine(
                    color = tint,
                    start = Offset(18f * sx, 9f * sy),
                    end = Offset(18f * sx, 19.5f * sy),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )
            }

            NavTabType.PROFILE -> {
                // User Head (Circular outline)
                drawCircle(
                    color = tint,
                    radius = 4f * sx,
                    center = Offset(12f * sx, 7.5f * sy),
                    style = Stroke(width = strokeWidth)
                )

                // User Shoulders Arc
                val bodyPath = Path().apply {
                    moveTo(4.5f * sx, 20.5f * sy)
                    cubicTo(
                        5f * sx, 15.5f * sy,
                        8f * sx, 14f * sy,
                        12f * sx, 14f * sy
                    )
                    cubicTo(
                        16f * sx, 14f * sy,
                        19f * sx, 15.5f * sy,
                        19.5f * sx, 20.5f * sy
                    )
                }

                drawPath(
                    path = bodyPath,
                    color = tint,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
