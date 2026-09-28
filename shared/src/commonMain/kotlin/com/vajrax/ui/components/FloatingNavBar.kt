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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.navigation.NavTabItem
import com.vajrax.ui.navigation.NavTabType
import com.vajrax.ui.theme.LuminaTheme

/**
 * Floating pill navigation bar from the designs: five labelled tabs, active tab in indigo.
 * The pill is opaque so scrolled content never bleeds through it.
 */
@Composable
fun FloatingPillNavBar(
    tabs: List<NavTabItem>,
    currentType: NavTabType?,
    onTabSelected: (NavTabItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors
    val shape = RoundedCornerShape(50.dp)
    val navBg = if (colors.isDark) Color(0xFF1E232E) else Color(0xFFF1F2F6)
    val navBorder = if (colors.isDark) Color(0xFF2B3242) else Color(0xFFE5E7EB)

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(if (colors.isDark) 0.dp else 10.dp, shape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                .clip(shape)
                .background(navBg)
                .border(1.dp, navBorder, shape)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = tab.type == currentType
                val tint by animateColorAsState(
                    if (selected) colors.primary else colors.onSurfaceVariant,
                    tween(200, easing = FastOutSlowInEasing),
                    label = "tab"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(shape)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onTabSelected(tab) }),
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
        }
    }
}

private fun iconFor(type: NavTabType) = when (type) {
    NavTabType.HOME -> VxIcons.Home
    NavTabType.CALENDAR -> VxIcons.Calendar
    NavTabType.DISCOVER -> VxIcons.Compass
    NavTabType.REPORT -> VxIcons.Chart
    NavTabType.PROFILE -> VxIcons.User
}
