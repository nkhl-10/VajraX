package com.vajrax.ui.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.ui.theme.LuminaTheme

/**
 * Onboarding Screen — first-launch template selection.
 * Goes directly to template selection (no welcome tap needed).
 * Templates load eagerly from ViewModel init.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit
) {
    val colors = LuminaTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Loading templates...",
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            TemplateSelectionContent(
                templates = state.templates,
                error = state.error,
                onSelect = { onIntent(OnboardingIntent.SelectTemplate(it)) },
                onStartBlank = { onIntent(OnboardingIntent.StartBlank) }
            )
        }
    }
}

@Composable
private fun TemplateSelectionContent(
    templates: List<DefaultTemplate>,
    error: String?,
    onSelect: (String) -> Unit,
    onStartBlank: () -> Unit
) {
    val colors = LuminaTheme.colors
    // Group into General and Arc tabs
    val tabs = listOf("All", "Arc", "General")
    var selectedTab by remember { mutableStateOf("All") }

    val displayed = when (selectedTab) {
        "Arc" -> templates.filter { it.category == "Arc" }
        "General" -> templates.filter { it.category == "General" }
        else -> templates
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ==========================================
        // HEADER
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand mark
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚡", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Choose your template",
                color = colors.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pick a ready-made routine or build your own from scratch.",
                color = colors.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error banner
            if (error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text("Could not load templates. $error", color = Color(0xFFDC2626), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // CATEGORY TABS
            // ==========================================
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabs.forEach { tab ->
                    val isActive = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) colors.surface else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedTab = tab }
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isActive) colors.onSurface else colors.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (selectedTab == "Arc") "15-ARC TEMPLATES" else "TEMPLATES",
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // ==========================================
        // TEMPLATE CARDS
        // ==========================================
        items(displayed, key = { it.id }) { template ->
            OnboardingTemplateCard(template = template, onSelect = { onSelect(template.id) })
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ==========================================
        // START FROM SCRATCH
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OR",
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStartBlank
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = colors.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start from scratch", color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Build your own custom daily routine", color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Text("›", color = colors.onSurfaceVariant, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun OnboardingTemplateCard(
    template: DefaultTemplate,
    onSelect: () -> Unit
) {
    val colors = LuminaTheme.colors

    // Per-template accent color
    val accentColors = listOf(
        Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981), Color(0xFFF59E0B),
        Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFEC4899),
        Color(0xFF84CC16), Color(0xFFF97316), Color(0xFF6366F1), Color(0xFF14B8A6),
        Color(0xFFD946EF), Color(0xFF64748B), Color(0xFF0F766E), Color(0xFFB45309)
    )
    val accentIdx = template.id.hashCode().mod(accentColors.size).let { if (it < 0) it + accentColors.size else it }
    val accent = accentColors[accentIdx]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Emoji icon box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = templateEmoji(template.id), fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    color = colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = template.description,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TemplateBadge(
                        text = "${template.habits.size} habits",
                        bgColor = Color(0xFFF3F4F6),
                        textColor = colors.onSurfaceVariant
                    )
                    TemplateBadge(
                        text = template.difficulty,
                        bgColor = Color(0xFFF3F4F6),
                        textColor = colors.onSurfaceVariant
                    )
                    if (template.category == "Arc") {
                        TemplateBadge(
                            text = "Arc",
                            bgColor = accent.copy(alpha = 0.1f),
                            textColor = accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Use this template button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSelect
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Use this template",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private fun templateEmoji(id: String): String = when {
    id == "arc_master_daily"        -> "🔥"
    id == "arc_winter"              -> "❄️"
    id == "arc_gym"                 -> "💪"
    id == "arc_study"               -> "🧠"
    id == "arc_career"              -> "💻"
    id == "arc_money"               -> "💰"
    id == "arc_monk"                -> "📵"
    id == "arc_spiritual"           -> "🧘"
    id == "arc_health"              -> "🥗"
    id == "arc_knowledge"           -> "📚"
    id == "arc_discipline"          -> "🎯"
    id == "arc_glowup"              -> "✨"
    id == "arc_reset"               -> "🌱"
    id == "arc_build"               -> "🚀"
    id == "arc_peace"               -> "🧘‍♂️"
    id == "arc_transformation"      -> "🔥"
    id.contains("morning")          -> "🌅"
    id.contains("night")            -> "🌙"
    id.contains("fitness")          -> "💪"
    id.contains("mindful")          -> "🧘"
    id.contains("study")            -> "📚"
    id.contains("work")             -> "💼"
    id.contains("reading")          -> "📖"
    id.contains("detox")            -> "📵"
    id.contains("finance")          -> "💰"
    id.contains("home")             -> "🏠"
    id.contains("growth")           -> "🌱"
    id.contains("healthy")          -> "🥗"
    id.contains("weekend")          -> "🌿"
    id.contains("challenge")        -> "⚡"
    else                            -> "📋"
}
