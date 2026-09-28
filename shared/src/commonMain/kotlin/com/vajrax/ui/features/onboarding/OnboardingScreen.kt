package com.vajrax.ui.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vajrax.domain.template.TemplateCatalog
import com.vajrax.platform.LocalPlatformActions
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.templates.BottomBar
import com.vajrax.ui.features.templates.TemplateCard
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    recommended: List<com.vajrax.domain.template.DefaultTemplate>,
    onIntent: (OnboardingIntent) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onUseTemplate: (String) -> Unit
) {
    val colors = LuminaTheme.colors
    val platform = LocalPlatformActions.current
    var pickWake by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(colors.background).statusBarsPadding()) {
        if (state.step != OnboardingStep.WELCOME) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = VxSpace.md, vertical = VxSpace.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(VxIcons.ArrowLeft, "Back", onClick = { onIntent(OnboardingIntent.Back) })
                Spacer(Modifier.weight(1f))
                StepDots(current = state.stepIndex, total = OnboardingStep.entries.size)
                Spacer(Modifier.weight(1f))
                if (state.step == OnboardingStep.ROUTINE) {
                    TextButton(onClick = { onIntent(OnboardingIntent.SkipPreferences) }) { Text("Skip") }
                } else {
                    Spacer(Modifier.width(48.dp))
                }
            }
        }

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally(tween(260)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(260))) togetherWith
                    (slideOutHorizontally(tween(180)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(180)))
            },
            modifier = Modifier.weight(1f),
            label = "onboarding"
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep(onStart = { onIntent(OnboardingIntent.Next) })
                OnboardingStep.ABOUT -> AboutStep(state, onIntent)
                OnboardingStep.ROUTINE -> RoutineStep(state, onIntent, onPickWake = { pickWake = true }, onContinue = {
                    if (state.reminderStyle != ReminderStyle.OFF && !platform.notificationsPermitted()) {
                        platform.requestNotificationPermission { granted ->
                            if (!granted) onIntent(OnboardingIntent.SetReminderStyle(ReminderStyle.OFF))
                            onIntent(OnboardingIntent.Next)
                        }
                    } else {
                        onIntent(OnboardingIntent.Next)
                    }
                })
                OnboardingStep.TEMPLATE -> TemplateStep(state, recommended, onIntent, onOpenTemplate, onUseTemplate)
            }
        }
    }

    if (pickWake) {
        TimePickerDialog(state.wakeTime, title = "Wake-up time", onDismiss = { pickWake = false }, onConfirm = {
            onIntent(OnboardingIntent.SetWakeTime(it))
            pickWake = false
        })
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    val colors = LuminaTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics { contentDescription = "Step ${current + 1} of $total" }
    ) {
        repeat(total) { i ->
            Box(
                Modifier.height(6.dp).width(if (i == current) 22.dp else 6.dp).clip(VxShape.pill)
                    .background(if (i <= current) colors.primary else colors.surfaceContainerHigh)
            )
        }
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(VxSpace.xxxl))
            VajraMark(size = 64.dp)
            Spacer(Modifier.height(VxSpace.xxl))
            Text("Choose the life you want to build.", style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.md))
            Text(
                "VAJRAX turns it into today's actions — small steps, tracked honestly.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.xxxl))
            ValueRow(VxIcons.ListChecks, "Start from a ready-made template", "Morning, fitness, study, focus and more — then make it yours.")
            ValueRow(VxIcons.Check, "Check in with one tap", "Works fully offline. Missed a day? Nothing resets.")
            ValueRow(VxIcons.Chart, "See real progress", "Weekly and monthly reports built only from what you recorded.")
        }
        BottomBar { PrimaryButton("Get started", onStart, Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun ValueRow(icon: ImageVector, title: String, body: String) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = VxSpace.sm), verticalAlignment = Alignment.Top) {
        IconBadge(icon, colors.primary, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(VxSpace.lg))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutStep(state: OnboardingUiState, onIntent: (OnboardingIntent) -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl, vertical = VxSpace.lg)) {
            Text("Let's set you up", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text("Two quick questions. Everything stays on this device.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.xxl))
            VxTextField(
                label = "What should we call you?",
                value = state.name,
                onValueChange = { onIntent(OnboardingIntent.SetName(it)) },
                placeholder = "Your name (optional)",
                maxChars = 40
            )
            Spacer(Modifier.height(VxSpace.xxl))
            Text("What do you want to improve?", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text("Pick up to 3 — we'll recommend templates.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                TemplateCatalog.goalToCategories.keys.forEach { goal ->
                    CategoryChip(goal, goal in state.goals, onClick = { onIntent(OnboardingIntent.ToggleGoal(goal)) })
                }
            }
        }
        BottomBar { PrimaryButton("Continue", { onIntent(OnboardingIntent.Next) }, Modifier.fillMaxWidth()) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoutineStep(state: OnboardingUiState, onIntent: (OnboardingIntent) -> Unit, onPickWake: () -> Unit, onContinue: () -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl, vertical = VxSpace.lg)) {
            Text("Your day", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text("We'll suggest times around your morning. You can change every habit later.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.xxl))
            TimeField("Wake-up time", state.wakeTime, onPick = onPickWake)
            Spacer(Modifier.height(VxSpace.xxl))
            Text("Time available in the morning", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                listOf(15, 30, 60, 90).forEach { m ->
                    CategoryChip(if (m == 90) "90+ min" else "$m min", state.morningMinutes == m, onClick = { onIntent(OnboardingIntent.SetMorningMinutes(m)) })
                }
            }
            Spacer(Modifier.height(VxSpace.xxl))
            Text("Reminders", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.md))
            ReminderStyle.entries.forEach { style ->
                val selected = state.reminderStyle == style
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(VxShape.medium)
                        .background(if (selected) colors.primaryContainer else colors.surface)
                        .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, VxShape.medium)
                        .selectable(selected, role = Role.RadioButton) { onIntent(OnboardingIntent.SetReminderStyle(style)) }
                        .padding(VxSpace.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (style == ReminderStyle.OFF) VxIcons.Moon else VxIcons.Bell, null,
                        tint = if (selected) colors.primary else colors.onSurfaceVariant, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(VxSpace.md))
                    Column(Modifier.weight(1f)) {
                        Text(style.label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                        Text(style.description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    if (selected) Icon(VxIcons.Check, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
        BottomBar { PrimaryButton("Continue", onContinue, Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun TemplateStep(
    state: OnboardingUiState,
    recommended: List<com.vajrax.domain.template.DefaultTemplate>,
    onIntent: (OnboardingIntent) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onUseTemplate: (String) -> Unit
) {
    val colors = LuminaTheme.colors
    if (state.isLoading) {
        LoadingSkeleton(Modifier.padding(VxSpace.gutter))
        return
    }
    if (state.error != null && state.templates.isEmpty()) {
        ErrorState(state.error, onRetry = { onIntent(OnboardingIntent.Restart) })
        return
    }
    val filtering = state.category != null || state.query.isNotBlank()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = VxSpace.gutter, end = VxSpace.gutter, bottom = VxSpace.xxxl + 48.dp)
    ) {
        item {
            Spacer(Modifier.height(VxSpace.sm))
            Text("Choose your tracker", style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(
                "Select a starting point. You can customize the habits before activating.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField(
                label = "Search",
                value = state.query,
                onValueChange = { onIntent(OnboardingIntent.Search(it)) },
                placeholder = "Morning, study, fitness…"
            )
            Spacer(Modifier.height(VxSpace.md))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                CategoryChip("All", state.category == null, onClick = { onIntent(OnboardingIntent.SelectCategory(null)) })
                TemplateCatalog.categories.filter { c -> state.templates.any { it.category == c } }.forEach { c ->
                    CategoryChip(c, state.category == c, onClick = { onIntent(OnboardingIntent.SelectCategory(if (state.category == c) null else c)) })
                }
            }
            Spacer(Modifier.height(VxSpace.lg))
            BlankTrackerCard(onClick = { onUseTemplate(TemplateCatalog.BLANK_ID) })
            Spacer(Modifier.height(VxSpace.xl))
        }
        if (!filtering && recommended.isNotEmpty()) {
            item { SectionLabel("Recommended for you"); Spacer(Modifier.height(VxSpace.sm)) }
            items(recommended, key = { "rec_" + it.id }) { t ->
                TemplateCard(t, onOpen = { onOpenTemplate(t.id) }, onUse = { onUseTemplate(t.id) })
                Spacer(Modifier.height(VxSpace.md))
            }
            item { Spacer(Modifier.height(VxSpace.md)) }
        }
        item { SectionLabel(if (filtering) "${state.filtered.size} templates" else "All templates"); Spacer(Modifier.height(VxSpace.sm)) }
        if (state.filtered.isEmpty()) {
            item { EmptyState(VxIcons.Search, "No templates found", "Try another word, or start with a blank tracker.") }
        }
        items(state.filtered, key = { it.id }) { t ->
            TemplateCard(t, onOpen = { onOpenTemplate(t.id) }, onUse = { onUseTemplate(t.id) })
            Spacer(Modifier.height(VxSpace.md))
        }
    }
}

@Composable
private fun BlankTrackerCard(onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(VxShape.medium).background(colors.surface)
            .border(1.5.dp, colors.primary.copy(alpha = 0.3f), VxShape.medium)
            .clickable(role = Role.Button, onClick = onClick).padding(VxSpace.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(VxIcons.Plus, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f)) {
            Text("Blank tracker", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text("Create a tracker entirely your own.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
