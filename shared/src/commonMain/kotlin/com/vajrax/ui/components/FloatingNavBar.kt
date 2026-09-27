package com.vajrax.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.ui.navigation.NavTabItem
import com.vajrax.ui.theme.LuminaTheme

/**
 * Floating Pill Navigation Bar — Exact Figma match.
 * tabs-row: fills rgba(0.749, 0.749, 0.749, 0.20), cornerRadius=50
 * Active tab: indigo #4F46E5 icon + label
 * Inactive tab: gray #9CA3AF
 */
@Composable
fun FloatingPillNavBar(
    tabs: List<NavTabItem>,
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors

    // Solid pill — reference shows opaque light-gray bar so scrolled content
    // (e.g. MOST CONSISTENT) never bleeds through from behind.
    val navBgColor = if (colors.isDark) {
        Color(0xFF1E232E) // solid dark frosted
    } else {
        Color(0xFFF1F2F6) // solid light gray, matches reference screenshots
    }
    val navBorderColor = if (colors.isDark) {
        Color(0xFF2B3242)
    } else {
        Color(0xFFE5E7EB)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(50.dp))                    // Figma cornerRadius=50
                .background(navBgColor)
                .border(1.dp, navBorderColor, RoundedCornerShape(50.dp))
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                FigmaNavTabItem(
                    item = tab,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FigmaNavTabItem(
    item: NavTabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors

    // Figma: active = #4F46E5 (primary), inactive = #9CA3AF (secondary text)
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "navTabColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(50.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NavTabIcon(
                tabType = item.type,
                tint = contentColor,
                size = 22.dp,
                isSelected = isSelected
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                letterSpacing = 0.sp
            )
        }
    }
}
