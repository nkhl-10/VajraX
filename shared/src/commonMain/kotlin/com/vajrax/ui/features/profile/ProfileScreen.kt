package com.vajrax.ui.features.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.vajrax.ui.theme.VxShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vajrax.domain.template.DefaultTemplate
import com.vajrax.ui.features.legal.LegalDoc
import com.vajrax.platform.LocalPlatformActions
import com.vajrax.ui.designsystem.*
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.ThemeMode
import com.vajrax.ui.theme.VxSpace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
    var confirmExport by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var templateMenu by remember { mutableStateOf<DefaultTemplate?>(null) }
    var confirmDeleteTemplate by remember { mutableStateOf<DefaultTemplate?>(null) }
    // After a denial Android stops showing the permission prompt, so offer the settings page.
    val notificationsBlocked: () -> Unit = {
        scope.launch {
            val result = snackbar.showSnackbar("Notifications are off for VAJRAX", actionLabel = "Settings")
            if (result == SnackbarResult.ActionPerformed) platform.openNotificationSettings()
        }
    }

    LaunchedEffect(effects) {
        effects.collect { e ->
            when (e) {
                is ProfileEffect.ShowMessage -> scope.launch { snackbar.showSnackbar(e.message) }
                is ProfileEffect.ExportReady -> platform.exportFile(e.fileName, e.json) { ok ->
                    scope.launch { snackbar.showSnackbar(if (ok) "Export saved" else "Export cancelled") }
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
        ScreenTitle("Profile")
        Spacer(Modifier.height(VxSpace.xl))

        ProfileHero(state, onEdit = { editProfile = true })

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel("Current template")
        Spacer(Modifier.height(VxSpace.sm))
        VxCard {
            val t = state.tracker
            if (t == null) {
                Text("No active routine", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text("Choose a template to start tracking.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.md))
                PrimaryButton("Choose a template", onChangeTemplate, Modifier.fillMaxWidth())
            } else {
                Text(t.name, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                if (state.trackerDescription.isNotBlank()) {
                    Text(state.trackerDescription, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(VxSpace.md))
                Row {
                    Text("Day ${state.dayNumber} of ${t.totalDays}", style = MaterialTheme.typography.labelLarge, color = colors.primary, modifier = Modifier.weight(1f))
                    Text("${(state.cycleFraction * 100).toInt()}% Complete", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(VxSpace.sm))
                LinearBar(state.cycleFraction)
                Spacer(Modifier.height(VxSpace.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                    SecondaryButton("View Template", onViewRoutine, Modifier.weight(1f))
                    PrimaryButton("Change Template", onChangeTemplate, Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel("Home-screen widget")
        Spacer(Modifier.height(VxSpace.sm))
        VxCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(VxIcons.Smartphone, colors.primary, size = 44.dp)
                Spacer(Modifier.width(VxSpace.md))
                Column(Modifier.weight(1f)) {
                    Text("Check in from your home screen", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(
                        "Check in without opening the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(VxSpace.lg))
            val addWidget: (Boolean) -> Unit = { list ->
                if (platform.canPinWidget()) platform.requestPinWidget(list)
                else scope.launch { snackbar.showSnackbar("Long-press your home screen → Widgets → VAJRAX.") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.md)) {
                SecondaryButton("Now card", onClick = { addWidget(false) }, modifier = Modifier.weight(1f), icon = VxIcons.Check)
                PrimaryButton("Today list", onClick = { addWidget(true) }, modifier = Modifier.weight(1f), icon = VxIcons.ListChecks)
            }
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel("My templates")
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            state.myTemplates.forEach { t ->
                ListRow(
                    title = t.name,
                    value = if (t.isDraft) "Draft" else "${t.habits.size} habits",
                    onClick = { templateMenu = t }
                )
                RowDivider()
            }
            ListRow("Create New Template", onClick = onCreateTemplate, icon = VxIcons.Plus, titleColor = colors.primary, showChevron = false)
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel("Preferences")
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            ListRow("Notifications", onClick = { notifications = true }, value = if (state.remindersEnabled) "On" else "Off", icon = VxIcons.Bell)
            RowDivider()
            ListRow(
                "Appearance",
                onClick = { appearance = true },
                value = when (state.themeMode) { ThemeMode.AUTO -> "System"; ThemeMode.LIGHT -> "Light"; ThemeMode.DARK -> "Dark" },
                icon = VxIcons.Sun
            )
            RowDivider()
            ListRow("Language", onClick = { scope.launch { snackbar.showSnackbar("English is the only language for now.") } }, value = "English", icon = VxIcons.Globe)
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel("Data & privacy")
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            ListRow("Export my data", onClick = { confirmExport = true }, icon = VxIcons.Download, value = if (state.isExporting) "Preparing…" else "JSON")
            RowDivider()
            ListRow("Delete all data", onClick = { confirmDelete = true }, icon = VxIcons.Trash, titleColor = colors.statusError, showChevron = false)
        }

        Spacer(Modifier.height(VxSpace.xxl))
        SectionLabel("About")
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
            "Offline · on this device · v${platform.appVersion}",
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
            title = "Appearance",
            options = listOf(ThemeMode.AUTO to "Use system setting", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark"),
            selected = state.themeMode,
            onSelect = {
                onIntent(ProfileIntent.SetTheme(it))
                appearance = false
            },
            onDismiss = { appearance = false }
        )
    }

    if (notifications) {
        ModalBottomSheet(onDismissRequest = { notifications = false }, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
            Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
                Text("Notifications", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                Text("Set per habit.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.lg))
                SwitchRow(
                    title = "Habit reminders",
                    subtitle = if (state.remindersEnabled) "On for habits with a reminder time" else "Off",
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
                Text("On the lock screen", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Spacer(Modifier.height(VxSpace.sm))
                listOf("FULL" to "Show habit names", "GENERIC" to "Generic: “You have a habit due”", "HIDDEN" to "No details").forEach { (key, label) ->
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
                    "Send a test reminder",
                    onClick = {
                        val send = {
                            platform.sendTestReminder()
                            scope.launch { snackbar.showSnackbar("Test reminder sent") }
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
                    title = "Hide habit names on the home-screen widget",
                    subtitle = null,
                    checked = state.widgetHideNames,
                    onChange = { onIntent(ProfileIntent.SetWidgetHideNames(it)) }
                )
            }
        }
    }

    if (confirmExport) {
        ConfirmDialog(
            title = "Export your data?",
            message = "Includes habits, history and notes. Saved only where you choose.",
            confirmLabel = "Export",
            onConfirm = {
                confirmExport = false
                onIntent(ProfileIntent.Export)
            },
            onDismiss = { confirmExport = false }
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete all data?",
            message = "Removes all routines, history and templates from this device. This can't be undone.",
            confirmLabel = "Delete everything",
            destructive = true,
            onConfirm = {
                confirmDelete = false
                onIntent(ProfileIntent.DeleteAllData)
            },
            onDismiss = { confirmDelete = false }
        )
    }

    templateMenu?.let { t ->
        ModalBottomSheet(onDismissRequest = { templateMenu = null }, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
            Column(Modifier.padding(bottom = VxSpace.xxxl)) {
                Text(t.name, style = MaterialTheme.typography.titleLarge, color = colors.onSurface, modifier = Modifier.padding(horizontal = VxSpace.xl))
                Text(if (t.isDraft) "Draft" else "${t.habits.size} habits", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = VxSpace.xl))
                Spacer(Modifier.height(VxSpace.md))
                if (!t.isDraft && t.habits.isNotEmpty()) {
                    ListRow("Use this template", onClick = { templateMenu = null; onUseTemplate(t.id) }, icon = VxIcons.Check)
                }
                ListRow("Edit", onClick = { templateMenu = null; onEditTemplate(t.id) }, icon = VxIcons.Pencil)
                ListRow("Duplicate", onClick = { templateMenu = null; onIntent(ProfileIntent.DuplicateTemplate(t.id)) }, icon = VxIcons.Copy)
                ListRow("Delete", onClick = { templateMenu = null; confirmDeleteTemplate = t }, icon = VxIcons.Trash, titleColor = colors.statusError, showChevron = false)
            }
        }
    }
    confirmDeleteTemplate?.let { t ->
        ConfirmDialog(
            title = "Delete “${t.name}”?",
            message = "Started routines keep their history.",
            confirmLabel = "Delete",
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
    val gradient = Brush.linearGradient(listOf(colors.primary, Color(0xFF7C3AED)))
    val overlap = 36.dp
    VxCard(elevated = true, contentPadding = PaddingValues(0.dp)) {
        Box(Modifier.fillMaxWidth().height(88.dp).background(gradient)) {
            // Soft rings on the band, echoing the Home dial.
            Canvas(Modifier.matchParentSize()) {
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.height * 0.95f, center = Offset(size.width - 28.dp.toPx(), 4.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.07f), radius = size.height * 0.55f, center = Offset(size.width * 0.66f, size.height))
            }
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).size(44.dp).clip(CircleShape)
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
                Text(state.displayName, style = MaterialTheme.typography.titleLarge, color = colors.onSurface, maxLines = 1)
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
            StatPod("Day", if (t != null) "${state.dayNumber}/${t.totalDays}" else "—", Modifier.weight(1f))
            StatPod("Streak", "${state.streakDays}d", Modifier.weight(1f))
            StatPod("This week", state.weekRate?.let { "$it%" } ?: "—", Modifier.weight(1f))
        }
    }
}

@Composable
private fun EditProfileDialog(state: ProfileUiState, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    val colors = LuminaTheme.colors
    var name by remember { mutableStateOf(if (state.displayName == "You") "" else state.displayName) }
    var email by remember { mutableStateOf(state.email) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit profile", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                VxTextField("Name", name, { name = it }, maxChars = 40)
                Spacer(Modifier.height(VxSpace.md))
                VxTextField("Email (optional)", email, { email = it }, keyboardType = KeyboardType.Email, helper = "On this device only")
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, email) }) { Text("Save", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        containerColor = colors.surface
    )
}
