package com.vajrax.ui.features.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI

private data class TemplateUi(
    val title: String,
    val subtitle: String,
    val tasksLabel: String,
    val freqLabel: String
)

private val providedFallback = listOf(
    TemplateUi("Morning Discipline", "Build a structured morning routine", "6 tasks", "Daily"),
    TemplateUi("Deep Work Block", "Lock in focus and maximize high-output hours", "4 tasks", "Daily"),
    TemplateUi("30-Day Challenge", "A rigorous month-long discipline protocol", "8 tasks", "30 days"),
    TemplateUi("6 AM Routine", "Wake up early and win the morning", "5 tasks", "Daily")
)

private val communityFallback = listOf(
    TemplateUi("Evening Wind Down", "Slow down your mind for deep, restful recovery", "4 tasks", "Daily"),
    TemplateUi("Fitness Starter Pack", "Essential daily habits for athletic consistency", "5 tasks", "Daily")
)

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val colors = com.vajrax.ui.theme.LuminaTheme.colors

    if (state.isLoading && state.paths.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.primary)
        }
        return
    }

    // Map repo paths to UI; fall back to reference-image demo data when empty.
    val provided: List<TemplateUi> = if (state.paths.isNotEmpty()) {
        state.paths.map {
            TemplateUi(
                title = it.title,
                subtitle = it.subtitle,
                tasksLabel = "${it.practices.size} tasks",
                freqLabel = "Daily"
            )
        }
    } else providedFallback

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 110.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discover",
                    color = colors.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.surfaceContainerLow)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⌕", fontSize = 18.sp, color = colors.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item(key = "provided_label") {
            Text(
                text = "PROVIDED TEMPLATES",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        provided.forEachIndexed { idx, t ->
            item(key = "provided_$idx") {
                DiscoverTemplateCard(
                    title = t.title,
                    subtitle = t.subtitle,
                    tasksLabel = t.tasksLabel,
                    freqLabel = t.freqLabel,
                    onUse = {
                        if (state.paths.isNotEmpty()) {
                            viewModel.onIntent(DiscoverIntent.UseTemplate(state.paths[idx]))
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item(key = "community_label") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "COMMUNITY TEMPLATES",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        communityFallback.forEachIndexed { idx, t ->
            item(key = "community_$idx") {
                DiscoverTemplateCard(
                    title = t.title,
                    subtitle = t.subtitle,
                    tasksLabel = t.tasksLabel,
                    freqLabel = t.freqLabel,
                    onUse = {}
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DiscoverTemplateCard(
    title: String,
    subtitle: String,
    tasksLabel: String,
    freqLabel: String,
    onUse: () -> Unit
) {
    val colors = com.vajrax.ui.theme.LuminaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☰ ", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        Text(text = tasksLabel, color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "◷ ", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        Text(text = freqLabel, color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Indigo "Use" pill button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onUse
                    )
                    .padding(horizontal = 22.dp, vertical = 10.dp),
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
}
