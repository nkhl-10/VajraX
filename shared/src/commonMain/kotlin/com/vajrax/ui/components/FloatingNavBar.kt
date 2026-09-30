package com.vajrax.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vajrax.ui.designsystem.tiltShadow
import com.vajrax.ui.designsystem.VxHaptic
import com.vajrax.ui.designsystem.rememberHaptics
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.navigation.NavTabItem
import com.vajrax.ui.navigation.NavTabType
import com.vajrax.ui.theme.LuminaTheme

/**
 * Floating glass navigation bar (same material as the TelepMaster bottom nav): the screen behind
 * is frosted with a live blur, then a translucent tint, a sheen along the top edge and a hairline
 * border are drawn over it. The selected tab sits on its own pill.
 *
 * [backdrop] is the screen content recorded by MainNavigation. It is drawn by reference, so the
 * frost follows scrolling without re-recording. Without it (Android 11 and older, where live blur
 * isn't available) the tint is simply more opaque.
 */
@Composable
fun FloatingPillNavBar(
    tabs: List<NavTabItem>,
    currentType: NavTabType?,
    onTabSelected: (NavTabItem) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GraphicsLayer? = null,
    backdropOrigin: Offset = Offset.Zero
) {
    val colors = LuminaTheme.colors
    val dark = colors.isDark
    val shape = RoundedCornerShape(28.dp)
    val blurred = backdrop != null
    val tint = if (dark) Color(0xFF161E2C).copy(alpha = if (blurred) 0.62f else 0.94f)
    else Color.White.copy(alpha = if (blurred) 0.66f else 0.94f)
    val sheen = Color.White.copy(alpha = if (dark) 0.14f else 0.6f)
    val edge = if (dark) Color.White.copy(alpha = 0.22f) else Color(0xFFE5E7EB).copy(alpha = 0.9f)
    val blurPx = with(LocalDensity.current) { 22.dp.toPx() }
    val frost = rememberGraphicsLayer()
    var barOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .onGloballyPositioned { barOrigin = it.positionInRoot() }
                .tiltShadow(shape, com.vajrax.platform.LocalDeviceTilt.current, elevation = 14.dp, dark = dark)
                .clip(shape)
                .drawBehind {
                    if (backdrop != null) {
                        frost.renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
                        frost.record {
                            translate(backdropOrigin.x - barOrigin.x, backdropOrigin.y - barOrigin.y) {
                                drawLayer(backdrop)
                            }
                        }
                        drawLayer(frost)
                    }
                    drawRect(tint)
                    drawRect(Brush.verticalGradient(0f to sheen, 0.35f to Color.Transparent))
                }
                .border(1.dp, edge, shape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = tab.type == currentType,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavItem(tab: NavTabItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = LuminaTheme.colors
    val selectedTint = if (colors.isDark) Color(0xFFC7D2FE) else colors.primary
    val tint by animateColorAsState(
        if (selected) selectedTint else colors.onSurfaceVariant,
        tween(180, easing = FastOutSlowInEasing),
        label = "tab"
    )
    val haptics = rememberHaptics()
    val pill by animateColorAsState(
        if (selected) (if (colors.isDark) Color.White.copy(alpha = 0.10f) else colors.primary.copy(alpha = 0.10f)) else Color.Transparent,
        tween(180, easing = FastOutSlowInEasing),
        label = "pill"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(pill)
            .selectable(selected = selected, role = Role.Tab, onClick = { if (!selected) haptics(VxHaptic.Select); onClick() }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(iconFor(tab.type), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = tint
        )
    }
}

private fun iconFor(type: NavTabType) = when (type) {
    NavTabType.HOME -> VxIcons.Home
    NavTabType.CALENDAR -> VxIcons.Calendar
    NavTabType.DISCOVER -> VxIcons.Compass
    NavTabType.REPORT -> VxIcons.Chart
    NavTabType.PROFILE -> VxIcons.User
}
