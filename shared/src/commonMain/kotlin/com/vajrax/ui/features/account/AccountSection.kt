@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.vajrax.ui.features.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vajrax.core.time.TimeFormat
import com.vajrax.domain.sync.SyncStatus
import com.vajrax.resources.*
import com.vajrax.ui.designsystem.IconBadge
import com.vajrax.ui.designsystem.ListRow
import com.vajrax.ui.designsystem.PrimaryButton
import com.vajrax.ui.designsystem.RowDivider
import com.vajrax.ui.designsystem.SecondaryButton
import com.vajrax.ui.designsystem.SectionLabel
import com.vajrax.ui.designsystem.TonalAction
import com.vajrax.ui.designsystem.VxBottomSheet
import com.vajrax.ui.designsystem.VxCard
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.designsystem.VxTextField
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxSpace
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

private enum class AccountSheet { SIGN_OUT, DELETE, CHANGE_PASSWORD }

/**
 * Profile's "Backup & sync" section. Signed out it's a quiet invitation (the app works fully
 * without an account); signed in it shows the account, how sync is doing, and account actions,
 * with the irreversible one set apart at the bottom.
 */
@Composable
fun AccountSection(
    state: AccountUiState,
    effects: Flow<AccountEffect>,
    onIntent: (AccountIntent) -> Unit,
    onOpenAccount: (AuthMode) -> Unit
) {
    if (!state.available) return
    var sheet by rememberSaveable { mutableStateOf<AccountSheet?>(null) }
    LaunchedEffect(effects) {
        effects.collect { if (it is AccountEffect.DialogDone) sheet = null }
    }

    SectionLabel(stringResource(Res.string.account_section))
    Spacer(Modifier.height(VxSpace.sm))
    val account = state.account
    if (account == null) {
        Invitation(onCreate = { onOpenAccount(AuthMode.CREATE) }, onSignIn = { onOpenAccount(AuthMode.SIGN_IN) })
    } else {
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = VxSpace.xl, vertical = VxSpace.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(VxIcons.Cloud, tint = LuminaTheme.colors.primary)
                Spacer(Modifier.width(VxSpace.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        account.email,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LuminaTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SyncLine(state.sync)
                }
                if (state.sync !is SyncStatus.Syncing) {
                    TonalAction(stringResource(Res.string.account_sync_now), VxIcons.Refresh, onClick = { onIntent(AccountIntent.SyncNow) })
                }
            }
            RowDivider()
            ListRow(
                stringResource(Res.string.account_change_password),
                onClick = { sheet = AccountSheet.CHANGE_PASSWORD },
                icon = VxIcons.Lock
            )
            RowDivider()
            ListRow(stringResource(Res.string.account_sign_out), onClick = {
                sheet = AccountSheet.SIGN_OUT
            }, icon = VxIcons.LogOut, showChevron = false)
            RowDivider()
            ListRow(
                stringResource(Res.string.account_sign_out_everywhere),
                onClick = { onIntent(AccountIntent.SignOutEverywhere) },
                icon = VxIcons.Smartphone,
                showChevron = false
            )
        }
        Spacer(Modifier.height(VxSpace.sm))
        VxCard(contentPadding = PaddingValues(vertical = VxSpace.xs)) {
            ListRow(
                stringResource(Res.string.account_delete),
                onClick = { sheet = AccountSheet.DELETE },
                icon = VxIcons.Trash,
                titleColor = LuminaTheme.colors.statusError,
                showChevron = false
            )
        }
    }

    when (sheet) {
        AccountSheet.SIGN_OUT -> SignOutSheet(state.busy, onIntent, onDismiss = { sheet = null })
        AccountSheet.DELETE -> DeleteAccountSheet(state, onIntent, onDismiss = {
            sheet = null
            onIntent(AccountIntent.ClearErrors)
        })
        AccountSheet.CHANGE_PASSWORD -> ChangePasswordSheet(state, onIntent, onDismiss = {
            sheet = null
            onIntent(AccountIntent.ClearErrors)
        })
        null -> Unit
    }
}

@Composable
private fun Invitation(onCreate: () -> Unit, onSignIn: () -> Unit) {
    val colors = LuminaTheme.colors
    VxCard {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(VxIcons.Cloud, tint = colors.primary)
            Spacer(Modifier.width(VxSpace.md))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.account_invite_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(VxSpace.xs))
                Text(
                    stringResource(Res.string.account_invite_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(VxSpace.lg))
        PrimaryButton(stringResource(Res.string.account_create), onClick = onCreate, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(VxSpace.sm))
        SecondaryButton(stringResource(Res.string.account_have_account), onClick = onSignIn, modifier = Modifier.fillMaxWidth())
    }
}

/** Status in words with an icon, never color alone; refreshes every half minute. */
@Composable
private fun SyncLine(status: SyncStatus) {
    val colors = LuminaTheme.colors
    val now by produceState(Clock.System.now().toEpochMilliseconds()) {
        while (true) {
            delay(TICK_MS)
            value = Clock.System.now().toEpochMilliseconds()
        }
    }
    val (icon, tint: Color, text) = when (status) {
        SyncStatus.Syncing -> Triple(VxIcons.Refresh, colors.onSurfaceVariant, stringResource(Res.string.account_status_syncing))
        is SyncStatus.UpToDate -> Triple(VxIcons.Check, colors.statusSuccess, syncedText(status.lastSyncAt, now))
        is SyncStatus.Waiting -> Triple(
            VxIcons.CloudOff,
            colors.statusWarning,
            if (status.pendingChanges > 0) {
                stringResource(Res.string.account_status_waiting_fmt, status.pendingChanges.toInt())
            } else {
                stringResource(Res.string.account_status_waiting)
            }
        )
        is SyncStatus.Failed -> Triple(VxIcons.AlertCircle, colors.statusError, stringResource(Res.string.account_status_failed))
        SyncStatus.Off -> Triple(VxIcons.Cloud, colors.onSurfaceVariant, stringResource(Res.string.account_status_never))
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Icon(icon as ImageVector, null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(VxSpace.xs))
        Text(text, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun syncedText(lastSyncAt: Long?, now: Long): String {
    if (lastSyncAt == null) return stringResource(Res.string.account_status_never)
    val minutes = ((now - lastSyncAt) / MINUTE_MS).toInt()
    return when {
        minutes < 1 -> stringResource(Res.string.account_status_just_now)
        minutes < HOUR_MINUTES -> stringResource(Res.string.account_status_minutes_fmt, minutes)
        else -> {
            val t = Instant.fromEpochMilliseconds(lastSyncAt).toLocalDateTime(TimeZone.currentSystemDefault())
            val hhmm = "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
            stringResource(Res.string.account_status_at_fmt, TimeFormat.display(hhmm))
        }
    }
}

@Composable
private fun SignOutSheet(busy: Boolean, onIntent: (AccountIntent) -> Unit, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    var removeLocal by rememberSaveable { mutableStateOf(false) }
    VxBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            SheetTitle(stringResource(Res.string.account_sign_out_title))
            Text(
                stringResource(Res.string.account_sign_out_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.lg))
            CheckRow(stringResource(Res.string.account_remove_local), removeLocal) { removeLocal = it }
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton(
                stringResource(Res.string.account_sign_out),
                onClick = { onIntent(AccountIntent.SignOut(removeLocal)) },
                loading = busy,
                destructive = removeLocal,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(VxSpace.sm))
            SecondaryButton(stringResource(Res.string.common_cancel), onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DeleteAccountSheet(state: AccountUiState, onIntent: (AccountIntent) -> Unit, onDismiss: () -> Unit) {
    val colors = LuminaTheme.colors
    var password by rememberSaveable { mutableStateOf("") }
    var removeLocal by rememberSaveable { mutableStateOf(false) }
    VxBottomSheet(onDismiss = onDismiss, hasUnsavedChanges = password.isNotEmpty() && !state.busy) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            SheetTitle(stringResource(Res.string.account_delete_title))
            Text(
                stringResource(Res.string.account_delete_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField(
                label = stringResource(Res.string.account_password),
                value = password,
                onValueChange = {
                    password = it
                    onIntent(AccountIntent.ClearErrors)
                },
                error = state.fieldErrors[AuthField.PASSWORD] ?: state.formError,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                autofill = ContentType.Password
            )
            Spacer(Modifier.height(VxSpace.md))
            CheckRow(stringResource(Res.string.account_remove_local), removeLocal) { removeLocal = it }
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton(
                stringResource(Res.string.account_delete),
                onClick = { onIntent(AccountIntent.DeleteAccount(password, removeLocal)) },
                enabled = password.isNotEmpty(),
                loading = state.busy,
                destructive = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(VxSpace.sm))
            SecondaryButton(stringResource(Res.string.common_cancel), onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ChangePasswordSheet(state: AccountUiState, onIntent: (AccountIntent) -> Unit, onDismiss: () -> Unit) {
    var current by rememberSaveable { mutableStateOf("") }
    var new by rememberSaveable { mutableStateOf("") }
    VxBottomSheet(onDismiss = onDismiss, hasUnsavedChanges = (current + new).isNotEmpty() && !state.busy) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            SheetTitle(stringResource(Res.string.account_change_password))
            VxTextField(
                label = stringResource(Res.string.account_current_password),
                value = current,
                onValueChange = {
                    current = it
                    onIntent(AccountIntent.ClearErrors)
                },
                error = state.fieldErrors[AuthField.CURRENT_PASSWORD],
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                autofill = ContentType.Password
            )
            Spacer(Modifier.height(VxSpace.lg))
            VxTextField(
                label = stringResource(Res.string.account_new_password),
                value = new,
                onValueChange = {
                    new = it
                    onIntent(AccountIntent.ClearErrors)
                },
                helper = stringResource(Res.string.account_password_helper_create),
                error = state.fieldErrors[AuthField.PASSWORD] ?: state.formError,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                autofill = ContentType.NewPassword
            )
            Spacer(Modifier.height(VxSpace.xl))
            PrimaryButton(
                stringResource(Res.string.account_save),
                onClick = { onIntent(AccountIntent.ChangePassword(current, new)) },
                enabled = current.isNotEmpty() && new.isNotEmpty(),
                loading = state.busy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        color = LuminaTheme.colors.onSurface,
        modifier = Modifier.semantics { heading() }.padding(bottom = VxSpace.xs)
    )
}

@Composable
private fun CheckRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(VxSpace.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = LuminaTheme.colors.onSurface)
    }
}

private const val TICK_MS = 30_000L
private const val MINUTE_MS = 60_000L
private const val HOUR_MINUTES = 60
