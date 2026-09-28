package com.vajrax.ui.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.designsystem.VajraMark
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import kotlinx.coroutines.delay

/**
 * Brand splash shown while the local database is prepared (migration, template library, today's
 * occurrences). Leaves as soon as [startRoute] is known, after a short minimum so it never flickers.
 */
@Composable
fun SplashScreen(startRoute: String?, error: String?, onReady: (String) -> Unit) {
    val colors = LuminaTheme.colors
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(450)) }
    LaunchedEffect(startRoute) {
        if (startRoute != null) {
            delay(650)
            onReady(startRoute)
        }
    }
    Box(Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(appear.value).scale(0.92f + 0.08f * appear.value)
        ) {
            VajraMark(size = 84.dp)
            Spacer(Modifier.height(VxSpace.xl))
            Text(
                "VAJRAX",
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 6.sp, fontWeight = FontWeight.ExtraBold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(VxSpace.xs))
            Text("Build yourself.", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = colors.statusError,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(VxSpace.xxl)
            )
        }
    }
}
