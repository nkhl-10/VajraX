package com.vajrax.ui.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Geometric V·X mark: a "V" whose arms continue into a crossing, with a small vajra point. */
@Composable
fun VajraMark(modifier: Modifier = Modifier, size: Dp = 72.dp) {
    Canvas(modifier = modifier.size(size).semantics { contentDescription = "VAJRAX" }) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)), Offset(0f, 0f), Offset(w, h)),
            cornerRadius = CornerRadius(w * 0.28f)
        )
        val stroke = w * 0.085f
        val v = Path().apply {
            moveTo(w * 0.28f, h * 0.30f)
            lineTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.72f, h * 0.30f)
        }
        drawPath(v, Color.White, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Faint crossing strokes complete the X behind the V.
        drawLine(Color.White.copy(alpha = 0.35f), Offset(w * 0.36f, h * 0.72f), Offset(w * 0.44f, h * 0.58f), stroke * 0.7f, StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.35f), Offset(w * 0.64f, h * 0.72f), Offset(w * 0.56f, h * 0.58f), stroke * 0.7f, StrokeCap.Round)
        // Vajra point.
        val d = w * 0.06f
        val diamond = Path().apply {
            moveTo(w * 0.5f, h * 0.20f - d)
            lineTo(w * 0.5f + d, h * 0.20f)
            lineTo(w * 0.5f, h * 0.20f + d)
            lineTo(w * 0.5f - d, h * 0.20f)
            close()
        }
        drawPath(diamond, Color(0xFFF5C451))
    }
}
