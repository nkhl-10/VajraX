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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.vajrax.resources.*
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.legal.LegalDoc
import com.vajrax.ui.features.templates.BottomBar
import com.vajrax.ui.features.templates.TemplateCard
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    recommended: List<com.vajrax.domain.template.DefaultTemplate>,
    onIntent: (OnboardingIntent) -> Unit,
    onOpenTemplate: (String) -> Unit,
    onUseTemplate: (String) -> Unit,
    onOpenLegal: (LegalDoc) -> Unit
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
                RoundIconButton(VxIcons.ArrowLeft, stringResource(Res.string.today_back), onClick = { onIntent(OnboardingIntent.Back) })
                Spacer(Modifier.weight(1f))
                StepDots(current = state.stepIndex, total = OnboardingStep.entries.size)
                Spacer(Modifier.weight(1f))
                if (state.step == OnboardingStep.ROUTINE) {
                    TextButton(onClick = { onIntent(OnboardingIntent.SkipPreferences) }) { Text(stringResource(Res.string.today_skip)) }
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
                OnboardingStep.WELCOME -> WelcomeStep(onStart = { onIntent(OnboardingIntent.Next) }, onOpenLegal = onOpenLegal)
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
        TimePickerDialog(state.wakeTime, title = stringResource(Res.string.onboarding_wake_up_time), onDismiss = { pickWake = false }, onConfirm = {
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
private fun WelcomeStep(onStart: () -> Unit, onOpenLegal: (LegalDoc) -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(VxSpace.xxxl))
            VajraMark(size = 64.dp)
            Spacer(Modifier.height(VxSpace.xxl))
            Text(stringResource(Res.string.onboarding_choose_the_life_you_want), style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.md))
            Text(
                stringResource(Res.string.onboarding_small_steps_every_day),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.xxxl))
            ValueRow(VxIcons.ListChecks, stringResource(Res.string.onboarding_ready_made_templates), "")
            ValueRow(VxIcons.Check, stringResource(Res.string.onboarding_one_tap_check_ins), "")
            ValueRow(VxIcons.Chart, stringResource(Res.string.onboarding_real_progress), "")
        }
        BottomBar {
            PrimaryButton(stringResource(Res.string.onboarding_get_started), onStart, Modifier.fillMaxWidth())
            Spacer(Modifier.height(VxSpace.xs))
            // Stated up front: nothing leaves the phone. Links open the full documents.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.onboarding_stays_on_this_device_2), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                TextButton(onClick = { onOpenLegal(LegalDoc.PRIVACY) }) { Text(stringResource(Res.string.onboarding_privacy), style = MaterialTheme.typography.labelMedium) }
                TextButton(onClick = { onOpenLegal(LegalDoc.TERMS) }) { Text(stringResource(Res.string.onboarding_terms), style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

@Composable
private fun ValueRow(icon: ImageVector, title: String, body: String) {
    val colors = LuminaTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = VxSpace.sm), verticalAlignment = Alignment.Top) {
        IconBadge(icon, colors.primary, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(VxSpace.lg))
        Column(Modifier.align(Alignment.CenterVertically)) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
            if (body.isNotBlank()) Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AboutStep(state: OnboardingUiState, onIntent: (OnboardingIntent) -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl, vertical = VxSpace.lg)) {
            Text(stringResource(Res.string.onboarding_let_s_set_you_up), style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(stringResource(Res.string.onboarding_stays_on_this_device), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.xxl))
            VxTextField(
                label = stringResource(Res.string.onboarding_what_should_we_call_you),
                value = state.name,
                onValueChange = { onIntent(OnboardingIntent.SetName(it)) },
                placeholder = stringResource(Res.string.onboarding_your_name_optional),
                maxChars = 40
            )
            Spacer(Modifier.height(VxSpace.xxl))
            Text(stringResource(Res.string.onboarding_what_do_you_want_to), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(stringResource(Res.string.onboarding_pick_up_to_3), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                TemplateCatalog.goalToCategories.keys.forEach { goal ->
                    CategoryChip(goal, goal in state.goals, onClick = { onIntent(OnboardingIntent.ToggleGoal(goal)) })
                }
            }
        }
        BottomBar { PrimaryButton(stringResource(Res.string.onboarding_continue), { onIntent(OnboardingIntent.Next) }, Modifier.fillMaxWidth()) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoutineStep(state: OnboardingUiState, onIntent: (OnboardingIntent) -> Unit, onPickWake: () -> Unit, onContinue: () -> Unit) {
    val colors = LuminaTheme.colors
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.xxl, vertical = VxSpace.lg)) {
            Text(stringResource(Res.string.onboarding_your_day), style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(stringResource(Res.string.onboarding_used_to_suggest_habit_times), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(VxSpace.xxl))
            TimeField(stringResource(Res.string.onboarding_wake_up_time), state.wakeTime, onPick = onPickWake)
            Spacer(Modifier.height(VxSpace.xxl))
            Text(stringResource(Res.string.onboarding_time_available_in_the_morning), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm), verticalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                listOf(15, 30, 60, 90).forEach { m ->
                    CategoryChip(if (m == 90) stringResource(Res.string.onboarding_90_min) else stringResource(Res.string.onboarding_min_fmt, m), state.morningMinutes == m, onClick = { onIntent(OnboardingIntent.SetMorningMinutes(m)) })
                }
            }
            Spacer(Modifier.height(VxSpace.xxl))
            Text(stringResource(Res.string.onboarding_reminders), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.md))
            ReminderStyle.entries.forEach { style ->
                val selected = state.reminderStyle == style
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(VxShape.medium)
                        .background(if (selected) colors.primaryContainer else colors.surface)
                        .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, VxShape.medium)
                        .hapticSelectable(selected, role = Role.RadioButton) { onIntent(OnboardingIntent.SetReminderStyle(style)) }
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
        BottomBar { PrimaryButton(stringResource(Res.string.onboarding_continue), onContinue, Modifier.fillMaxWidth()) }
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
    val filtering = state.category != null || state.query.isNotBlank() || state.arcTab
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = VxSpace.gutter, end = VxSpace.gutter, bottom = VxSpace.xxxl + 48.dp)
    ) {
        item {
            Spacer(Modifier.height(VxSpace.sm))
            Text(stringResource(Res.string.onboarding_choose_your_routine), style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
            Spacer(Modifier.height(VxSpace.xs))
            Text(
                stringResource(Res.string.onboarding_pick_one_you_can_edit),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField(
                label = stringResource(Res.string.onboarding_search),
                value = state.query,
                onValueChange = { onIntent(OnboardingIntent.Search(it)) },
                placeholder = stringResource(Res.string.onboarding_morning_study_fitness)
            )
            Spacer(Modifier.height(VxSpace.md))
            SegmentedToggle(
                options = listOf(stringResource(Res.string.onboarding_templates), stringResource(Res.string.onboarding_arc)),
                selectedIndex = if (state.arcTab) 1 else 0,
                onSelect = { onIntent(OnboardingIntent.ShowArc(it == 1)) },
                modifier = Modifier.fillMaxWidth()
            )
            if (!state.arcTab) {
                Spacer(Modifier.height(VxSpace.md))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
                    CategoryChip(stringResource(Res.string.discover_all), state.category == null, onClick = { onIntent(OnboardingIntent.SelectCategory(null)) })
                    TemplateCatalog.categories.filter { c -> c != "Arc" && state.templates.any { it.category == c } }.forEach { c ->
                        CategoryChip(c, state.category == c, onClick = { onIntent(OnboardingIntent.SelectCategory(if (state.category == c) null else c)) })
                    }
                }
            }
            Spacer(Modifier.height(VxSpace.xl))
        }
        if (!filtering && recommended.isNotEmpty()) {
            item { SectionLabel(stringResource(Res.string.onboarding_recommended_for_you)); Spacer(Modifier.height(VxSpace.sm)) }
            items(recommended, key = { "rec_" + it.id }) { t ->
                TemplateCard(t, onOpen = { onOpenTemplate(t.id) }, onUse = { onUseTemplate(t.id) })
                Spacer(Modifier.height(VxSpace.md))
            }
            item { Spacer(Modifier.height(VxSpace.md)) }
        }
        item { SectionLabel(if (filtering) stringResource(Res.string.onboarding_templates_fmt, state.filtered.size) else stringResource(Res.string.onboarding_all_templates)); Spacer(Modifier.height(VxSpace.sm)) }
        if (state.filtered.isEmpty()) {
            item { EmptyState(VxIcons.Search, stringResource(Res.string.discover_no_templates_found), stringResource(Res.string.onboarding_try_another_word)) }
        }
        items(state.filtered, key = { it.id }) { t ->
            TemplateCard(t, onOpen = { onOpenTemplate(t.id) }, onUse = { onUseTemplate(t.id) })
            Spacer(Modifier.height(VxSpace.md))
        }
        // Starting from scratch is the fallback path, so it closes the list instead of pushing
        // the recommendations below the fold.
        item(key = "blank") {
            Spacer(Modifier.height(VxSpace.sm))
            BlankTrackerCard(onClick = { onUseTemplate(TemplateCatalog.BLANK_ID) })
        }
    }
}

@Composable
private fun BlankTrackerCard(onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(VxShape.medium).background(colors.surface)
            .border(1.5.dp, colors.primary.copy(alpha = 0.3f), VxShape.medium)
            .hapticClickable(onClick = onClick).padding(VxSpace.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(VxIcons.Plus, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.onboarding_blank_tracker), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(stringResource(Res.string.onboarding_create_a_tracker_entirely_your), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
