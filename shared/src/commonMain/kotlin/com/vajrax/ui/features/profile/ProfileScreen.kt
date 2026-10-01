package com.vajrax.ui.features.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.platform.LocalPlatformActions
import com.vajrax.resources.*
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.features.legal.LegalDoc
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.ThemeMode
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    effects: Flow<ProfileEffect>,
    onIntent: (ProfileIntent) -> Unit,
    onViewRoutine: () -> Unit,
    onChangeTemplate: () -> Unit,
    onCreateTemplate: () -> Unit,
    onEditTemplate: (String) -> Unit,
    onUseTemplate: (String) -> Unit,
    onOpenLegal: (LegalDoc) -> Unit,
    onDataWiped: () -> Unit
) {
    val colors = LuminaTheme.colors
    val snackbar = LocalVxSnackbar.current
    val platform = LocalPlatformActions.current
    val scope = rememberCoroutineScope()
    var editProfile by remember { mutableStateOf(false) }
    var appearance by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var templateMenu by remember { mutableStateOf<DefaultTemplate?>(null) }
    var confirmDeleteTemplate by remember { mutableStateOf<DefaultTemplate?>(null) }
    // After a denial Android stops showing the permission prompt, so offer the settings page.
    val notificationsBlocked: () -> Unit = {
        scope.launch {
            val result = snackbar.showSnackbar(getString(Res.string.profile_notifications_are_off_for_vajrax), actionLabel = getString(Res.string.profile_settings))
            if (result == SnackbarResult.ActionPerformed) platform.openNotificationSettings()
        }
    }

    LaunchedEffect(effects) {
        effects.collect { e ->
            when (e) {
                is ProfileEffect.ShowMessage -> scope.launch { snackbar.showSnackbar(e.message) }
                is ProfileEffect.ExportReady -> platform.exportFile(e.fileName, e.json) { ok ->
                    scope.launch {
                        snackbar.showSnackbar(if (ok) getString(Res.string.profile_export_saved_it_includes_your) else getString(Res.string.profile_export_cancelled))
                    }
                }
                ProfileEffect.DataWiped -> onDataWiped()
            }
        }
    }

    Column(
        // statusBarsPadding before the scroll: content never slides under the status bar.
        Modifier.fillMaxSize().background(colors.background).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = VxSpace.gutter)
    ) {
        Spacer(Modifier.height(VxSpace.lg))
        ScreenTitle(stringResource(Res.string.profile_profile))
        Spacer(Modifier.height(VxSpace.xl))

        ProfileHero(state, onEdit = { editProfile = true })

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel(stringResource(Res.string.profile_current_routine))
        Spacer(Modifier.height(VxSpace.sm))
        VxCard {
            val t = state.tracker
            if (t == null) {
                Text(stringResource(Res.string.profile_no_active_routine), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(stringResource(Res.string.profile_choose_a_template_to_start), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.md))
                PrimaryButton(stringResource(Res.string.today_choose_a_template), onChangeTemplate, Modifier.fillMaxWidth())
            } else {
                Text(t.name, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                if (state.trackerDescription.isNotBlank()) {
                    Text(state.trackerDescription, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(VxSpace.md))
                Row {
                    if (state.cycleComplete) {
                        Text(stringResource(Res.string.profile_day_cycle_complete_fmt, t.totalDays), style = MaterialTheme.typography.labelLarge, color = colors.primary, modifier = Modifier.weight(1f))
                        Text(stringResource(Res.string.profile_day_fmt, state.daysIn), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    } else {
                        Text(stringResource(Res.string.profile_day_of_fmt, state.dayNumber, t.totalDays), style = MaterialTheme.typography.labelLarge, color = colors.primary, modifier = Modifier.weight(1f))
                        Text(stringResource(Res.string.profile_complete_fmt, (state.cycleFraction * 100).toInt()), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(VxSpace.sm))
                LinearBar(state.cycleFraction)
                Spacer(Modifier.height(VxSpace.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                    // Looking at the routine is the everyday action; switching it is rare.
                    PrimaryButton(stringResource(Res.string.profile_view_routine), onViewRoutine, Modifier.weight(1f))
                    SecondaryButton(stringResource(Res.string.profile_change), onChangeTemplate, Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel(stringResource(Res.string.profile_home_screen_widget))
        Spacer(Modifier.height(VxSpace.sm))
        VxCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(VxIcons.Smartphone, colors.primary, size = 44.dp)
                Spacer(Modifier.width(VxSpace.md))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.profile_check_in_from_your_home), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(
                        stringResource(Res.string.profile_check_in_without_opening_the),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(VxSpace.lg))
            val addWidget: (Boolean) -> Unit = { list ->
                if (platform.canPinWidget()) platform.requestPinWidget(list)
                else scope.launch { snackbar.showSnackbar(getString(Res.string.profile_long_press_your_home_screen)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                SecondaryButton(stringResource(Res.string.profile_now_card), onClick = { addWidget(false) }, modifier = Modifier.weight(1f), icon = VxIcons.Check)
                SecondaryButton(stringResource(Res.string.profile_today_list), onClick = { addWidget(true) }, modifier = Modifier.weight(1f), icon = VxIcons.ListChecks)
            }
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel(stringResource(Res.string.profile_my_templates))
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            state.myTemplates.forEach { t ->
                ListRow(
                    title = t.name,
                    value = if (t.isDraft) stringResource(Res.string.profile_draft) else stringResource(Res.string.profile_habits_fmt, t.habits.size),
                    onClick = { templateMenu = t }
                )
                RowDivider()
            }
            ListRow(stringResource(Res.string.profile_create_new_template), onClick = onCreateTemplate, icon = VxIcons.Plus, titleColor = colors.primary, showChevron = false)
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel(stringResource(Res.string.profile_preferences))
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            // "On" only when reminders can really ring (setting on and Android permission granted).
            ListRow(stringResource(Res.string.profile_notifications), onClick = { notifications = true }, value = if (LocalReminderAccess.current.on) stringResource(Res.string.profile_on) else stringResource(Res.string.profile_off), icon = VxIcons.Bell)
            RowDivider()
            ListRow(
                stringResource(Res.string.profile_appearance),
                onClick = { appearance = true },
                value = when (state.themeMode) { ThemeMode.AUTO -> "System"; ThemeMode.LIGHT -> "Light"; ThemeMode.DARK -> "Dark" },
                icon = VxIcons.Sun
            )
            // Shown once there is a second language to choose; a row that can't change anything is clutter.
            if (AVAILABLE_LANGUAGES > 1) {
                RowDivider()
                ListRow(stringResource(Res.string.profile_language), onClick = { scope.launch { snackbar.showSnackbar(getString(Res.string.profile_english_is_the_only_language)) } }, value = stringResource(Res.string.profile_english), icon = VxIcons.Globe)
            }
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel(stringResource(Res.string.profile_data_privacy))
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            ListRow(stringResource(Res.string.profile_export_my_data), onClick = { onIntent(ProfileIntent.Export) }, icon = VxIcons.Download, value = if (state.isExporting) stringResource(Res.string.profile_preparing) else stringResource(Res.string.profile_json))
            RowDivider()
            ListRow(stringResource(Res.string.profile_delete_all_data_2), onClick = { confirmDelete = true }, icon = VxIcons.Trash, titleColor = colors.statusError, showChevron = false)
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel(stringResource(Res.string.profile_about))
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            ListRow(LegalDoc.PRIVACY.title, onClick = { onOpenLegal(LegalDoc.PRIVACY) }, icon = VxIcons.Shield)
            RowDivider()
            ListRow(LegalDoc.TERMS.title, onClick = { onOpenLegal(LegalDoc.TERMS) }, icon = VxIcons.Note)
            RowDivider()
            ListRow(LegalDoc.HEALTH.title, onClick = { onOpenLegal(LegalDoc.HEALTH) }, icon = VxIcons.Heart)
            RowDivider()
            ListRow(LegalDoc.LICENSES.title, onClick = { onOpenLegal(LegalDoc.LICENSES) }, icon = VxIcons.Info)
        }
        Spacer(Modifier.height(VxSpace.md))
        Text(
            stringResource(Res.string.profile_offline_on_this_device_v_fmt, platform.appVersion),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(VxSpace.navClearance))
    }

    if (editProfile) EditProfileDialog(state, onDismiss = { editProfile = false }, onSave = { n, e ->
        onIntent(ProfileIntent.SaveProfile(n, e))
        editProfile = false
    })

    if (appearance) {
        OptionDialog(
            title = stringResource(Res.string.profile_appearance),
            options = listOf(ThemeMode.AUTO to stringResource(Res.string.profile_use_system_setting), ThemeMode.LIGHT to stringResource(Res.string.profile_light), ThemeMode.DARK to stringResource(Res.string.profile_dark)),
            selected = state.themeMode,
            onSelect = {
                onIntent(ProfileIntent.SetTheme(it))
                appearance = false
            },
            onDismiss = { appearance = false }
        )
    }

    if (notifications) {
        VxBottomSheet(onDismiss = { notifications = false }) {
            Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
                Text(stringResource(Res.string.profile_notifications), style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                Text(stringResource(Res.string.profile_set_per_habit), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.lg))
                SwitchRow(
                    title = stringResource(Res.string.profile_habit_reminders),
                    subtitle = if (state.remindersEnabled) stringResource(Res.string.profile_on_for_habits_with_a) else stringResource(Res.string.profile_off),
                    checked = state.remindersEnabled,
                    onChange = { enable ->
                        if (enable && !platform.notificationsPermitted()) {
                            platform.requestNotificationPermission { granted ->
                                onIntent(ProfileIntent.SetReminders(granted))
                                if (!granted) notificationsBlocked()
                            }
                        } else {
                            onIntent(ProfileIntent.SetReminders(enable))
                        }
                    }
                )
                Spacer(Modifier.height(VxSpace.lg))
                Text(stringResource(Res.string.profile_on_the_lock_screen), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Spacer(Modifier.height(VxSpace.sm))
                listOf(
                    com.vajrax.domain.habit.NotificationPrivacy.FULL.name to "Show habit names",
                    com.vajrax.domain.habit.NotificationPrivacy.GENERIC.name to "Generic: “You have a habit due”",
                    com.vajrax.domain.habit.NotificationPrivacy.HIDDEN.name to "No details"
                ).forEach { (key, label) ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).hapticSelectable(state.privacy == key, role = Role.RadioButton) { onIntent(ProfileIntent.SetPrivacy(key)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = state.privacy == key, onClick = null)
                        Spacer(Modifier.width(VxSpace.sm))
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                    }
                }
                Spacer(Modifier.height(VxSpace.md))
                SecondaryButton(
                    stringResource(Res.string.profile_send_a_test_reminder),
                    onClick = {
                        val send = {
                            platform.sendTestReminder()
                            scope.launch { snackbar.showSnackbar(getString(Res.string.profile_test_reminder_sent)) }
                        }
                        if (platform.notificationsPermitted()) send()
                        else platform.requestNotificationPermission { granted ->
                            if (granted) send() else notificationsBlocked()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = VxIcons.Bell
                )
                Spacer(Modifier.height(VxSpace.md))
                SwitchRow(
                    title = stringResource(Res.string.profile_hide_habit_names_on_the),
                    subtitle = null,
                    checked = state.widgetHideNames,
                    onChange = { onIntent(ProfileIntent.SetWidgetHideNames(it)) }
                )
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(Res.string.profile_delete_all_data),
            message = stringResource(Res.string.profile_removes_all_routines_history_and),
            confirmLabel = stringResource(Res.string.profile_delete_everything),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                onIntent(ProfileIntent.DeleteAllData)
            },
            onDismiss = { confirmDelete = false }
        )
    }

    templateMenu?.let { t ->
        VxBottomSheet(onDismiss = { templateMenu = null }) {
            Column(Modifier.padding(bottom = VxSpace.xxxl)) {
                Text(t.name, style = MaterialTheme.typography.titleLarge, color = colors.onSurface, modifier = Modifier.padding(horizontal = VxSpace.xl))
                Text(if (t.isDraft) stringResource(Res.string.profile_draft) else stringResource(Res.string.profile_habits_fmt, t.habits.size), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = VxSpace.xl))
                Spacer(Modifier.height(VxSpace.md))
                if (!t.isDraft && t.habits.isNotEmpty()) {
                    ListRow(stringResource(Res.string.profile_use_this_template), onClick = { templateMenu = null; onUseTemplate(t.id) }, icon = VxIcons.Check)
                }
                ListRow(stringResource(Res.string.profile_edit), onClick = { templateMenu = null; onEditTemplate(t.id) }, icon = VxIcons.Pencil)
                ListRow(stringResource(Res.string.profile_duplicate), onClick = { templateMenu = null; onIntent(ProfileIntent.DuplicateTemplate(t.id)) }, icon = VxIcons.Copy)
                ListRow(stringResource(Res.string.profile_delete), onClick = { templateMenu = null; confirmDeleteTemplate = t }, icon = VxIcons.Trash, titleColor = colors.statusError, showChevron = false)
            }
        }
    }
    confirmDeleteTemplate?.let { t ->
        ConfirmDialog(
            title = stringResource(Res.string.profile_delete_fmt, t.name),
            message = stringResource(Res.string.profile_started_routines_keep_their_history),
            confirmLabel = stringResource(Res.string.profile_delete),
            destructive = true,
            onConfirm = {
                onIntent(ProfileIntent.DeleteTemplate(t.id))
                confirmDeleteTemplate = null
            },
            onDismiss = { confirmDeleteTemplate = null }
        )
    }
}

/**
 * Top of Profile: a gradient band with the avatar overlapping its edge, name and email, an edit
 * button, and three stats (day of the routine, day streak, this week's completion).
 */
@Composable
private fun ProfileHero(state: ProfileUiState, onEdit: () -> Unit) {
    val colors = LuminaTheme.colors
    val gradient = Brush.linearGradient(listOf(colors.primary, com.vajrax.ui.theme.BrandViolet))
    val overlap = 36.dp
    VxCard(elevated = true, contentPadding = PaddingValues(0.dp)) {
        Box(Modifier.fillMaxWidth().height(88.dp).background(gradient)) {
            // Soft rings on the band, echoing the Home dial.
            Canvas(Modifier.matchParentSize()) {
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.95f, center = Offset(size.width - 28.dp.toPx(), 4.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.07f), radius = size.height * 0.55f, center = Offset(size.width * 0.66f, size.height))
            }
            Box(
                Modifier.align(Alignment.TopEnd).padding(6.dp).size(48.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .hapticClickable(onClickLabel = "Edit profile", onClick = onEdit)
                    .semantics { contentDescription = "Edit profile" },
                contentAlignment = Alignment.Center
            ) {
                Icon(VxIcons.Pencil, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Row(
            Modifier.padding(horizontal = VxSpace.xl)
                // Pull the row up so the avatar straddles the band's edge.
                .layout { measurable, constraints ->
                    val p = measurable.measure(constraints)
                    val up = overlap.roundToPx()
                    layout(p.width, p.height - up) { p.place(0, -up) }
                },
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                Modifier.size(80.dp).clip(CircleShape).background(colors.surface).padding(4.dp)
                    .clip(CircleShape).background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Text(state.initials, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
            Spacer(Modifier.width(VxSpace.md))
            Column(
                Modifier.weight(1f).padding(bottom = 2.dp).clip(VxShape.small).hapticClickable(onClickLabel = "Edit profile", onClick = onEdit)
            ) {
                Text(
                    state.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    state.email.ifBlank { "On this device · offline" },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = VxSpace.xl, end = VxSpace.xl, top = VxSpace.lg, bottom = VxSpace.xl),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val t = state.tracker
            StatPod(
                stringResource(Res.string.profile_day),
                when {
                    t == null -> "—"
                    state.cycleComplete -> "${state.daysIn}"
                    else -> "${state.dayNumber}/${t.totalDays}"
                },
                Modifier.weight(1f)
            )
            StatPod(stringResource(Res.string.profile_streak), stringResource(Res.string.profile_d_fmt, state.streakDays), Modifier.weight(1f))
            StatPod(stringResource(Res.string.report_this_week), state.weekRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
        }
    }
}

@Composable
private fun EditProfileDialog(state: ProfileUiState, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    val colors = LuminaTheme.colors
    var name by remember { mutableStateOf(if (state.displayName == "You") "" else state.displayName) }
    var email by remember { mutableStateOf(state.email) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val edited = name != (if (state.displayName == "You") "" else state.displayName) || email != state.email
    AlertDialog(
        onDismissRequest = onDismiss,
        // A stray tap outside shouldn't throw away what was typed.
        properties = androidx.compose.ui.window.DialogProperties(dismissOnClickOutside = !edited),
        title = { Text(stringResource(Res.string.profile_edit_profile), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                VxTextField(stringResource(Res.string.profile_name), name, { name = it }, maxChars = 40)
                Spacer(Modifier.height(VxSpace.md))
                VxTextField(
                    stringResource(Res.string.profile_email_optional),
                    email,
                    {
                        email = it
                        emailError = null
                    },
                    keyboardType = KeyboardType.Email,
                    helper = stringResource(Res.string.profile_on_this_device_only),
                    error = emailError
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Keep the dialog open with the error instead of closing and losing the input.
                emailError = com.vajrax.domain.usecase.ProfileRules.emailError(email)
                if (emailError == null) onSave(name, email)
            }) { Text(stringResource(Res.string.today_save), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
        containerColor = colors.surface
    )
}

@Composable
private fun <T> OptionDialog(title: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).hapticSelectable(value == selected, role = Role.RadioButton) { onSelect(value) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = value == selected, onClick = null)
                        Spacer(Modifier.width(VxSpace.sm))
                        Text(label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.profile_close)) } },
        containerColor = colors.surface
    )
}

/** Languages the UI is translated into (English only for now). */
private const val AVAILABLE_LANGUAGES = 1
