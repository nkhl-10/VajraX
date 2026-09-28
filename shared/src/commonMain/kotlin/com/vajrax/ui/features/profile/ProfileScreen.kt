package com.vajrax.ui.features.profile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vajrax.domain.template.DefaultTemplate
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
    var widgetPicker by remember { mutableStateOf(false) }

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
        Modifier.fillMaxSize().background(colors.background).verticalScroll(rememberScrollState())
            .statusBarsPadding().padding(horizontal = VxSpace.gutter)
    ) {
        Spacer(Modifier.height(VxSpace.lg))
        ScreenTitle("Profile")
        Spacer(Modifier.height(VxSpace.xl))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(colors.primary, Color(0xFF7C3AED)))),
                contentAlignment = Alignment.Center
            ) {
                Text(state.initials, style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
            Spacer(Modifier.width(VxSpace.lg))
            Column(Modifier.weight(1f)) {
                Text(state.displayName, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Text(state.email.ifBlank { "Local profile · stored on this device" }, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(VxSpace.md))
        SecondaryButton("Edit Profile", onClick = { editProfile = true }, modifier = Modifier.widthIn(min = 150.dp))

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
            ListRow(
                "Home-screen widget",
                onClick = {
                    if (platform.canPinWidget()) widgetPicker = true
                    else scope.launch { snackbar.showSnackbar("Long-press your home screen → Widgets → VAJRAX.") }
                },
                value = "Add",
                icon = VxIcons.Smartphone
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
        Spacer(Modifier.height(VxSpace.md))
        Text(
            "Everything is stored only on this device and works offline. VAJRAX ${platform.appVersion}",
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
                Text("Reminders are set per habit. They are silent nudges, never guilt.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(VxSpace.lg))
                SwitchRow(
                    title = "Habit reminders",
                    subtitle = if (state.remindersEnabled) "On for habits with a reminder time" else "Off",
                    checked = state.remindersEnabled,
                    onChange = { enable ->
                        if (enable && !platform.notificationsPermitted()) {
                            platform.requestNotificationPermission { granted ->
                                onIntent(ProfileIntent.SetReminders(granted))
                                if (!granted) scope.launch { snackbar.showSnackbar("Allow notifications in system settings to get reminders.") }
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
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(state.privacy == key, role = Role.RadioButton) { onIntent(ProfileIntent.SetPrivacy(key)) },
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
                            scope.launch { snackbar.showSnackbar("Test reminder sent — check your notifications.") }
                        }
                        if (platform.notificationsPermitted()) send()
                        else platform.requestNotificationPermission { granted ->
                            if (granted) send() else scope.launch { snackbar.showSnackbar("Allow notifications in system settings to get reminders.") }
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

    if (widgetPicker) {
        ModalBottomSheet(onDismissRequest = { widgetPicker = false }, sheetState = rememberModalBottomSheetState(true), containerColor = colors.surface) {
            Column(Modifier.padding(bottom = VxSpace.xxxl)) {
                Text("Add a home-screen widget", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface, modifier = Modifier.padding(horizontal = VxSpace.xl))
                Text(
                    "Check in without opening the app — the same actions as a reminder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = VxSpace.xl)
                )
                Spacer(Modifier.height(VxSpace.md))
                WidgetOption(
                    title = "Today list",
                    body = "Every habit for today. Tap a circle to mark it done (tap again to undo), +1 for counts, Later to snooze the current one.",
                    onClick = {
                        widgetPicker = false
                        platform.requestPinWidget(list = true)
                    }
                )
                WidgetOption(
                    title = "Now card",
                    body = "Just the current habit with Done and Snooze. Fits a small space.",
                    onClick = {
                        widgetPicker = false
                        platform.requestPinWidget(list = false)
                    }
                )
            }
        }
    }

    if (confirmExport) {
        ConfirmDialog(
            title = "Export your data?",
            message = "The file contains your habits, history, notes and reflections. You choose where it is saved; nothing is uploaded.",
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
            message = "This permanently removes your profile, routines, history, goals, reflections and custom templates from this device. It can't be undone — export first if you want a copy.",
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
            message = "Routines already started from it keep their history.",
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
                VxTextField("Email (optional)", email, { email = it }, keyboardType = KeyboardType.Email, helper = "Only stored on this device.")
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
                        Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(value == selected, role = Role.RadioButton) { onSelect(value) },
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

@Composable
private fun WidgetOption(title: String, body: String, onClick: () -> Unit) {
    val colors = LuminaTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(horizontal = VxSpace.xl, vertical = VxSpace.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(VxIcons.Smartphone, colors.primary, size = 40.dp)
        Spacer(Modifier.width(VxSpace.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Icon(VxIcons.Plus, null, tint = colors.primary, modifier = Modifier.size(20.dp))
    }
}
