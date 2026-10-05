package com.vajrax.ui.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vajrax.domain.sync.FirstSync
import com.vajrax.resources.*
import com.vajrax.ui.designsystem.IconBadge
import com.vajrax.ui.designsystem.PrimaryButton
import com.vajrax.ui.designsystem.VxBottomSheet
import com.vajrax.ui.designsystem.VxCard
import com.vajrax.ui.designsystem.VxHaptic
import com.vajrax.ui.designsystem.VxIcons
import com.vajrax.ui.designsystem.VxTextField
import com.vajrax.ui.designsystem.VxTopBar
import com.vajrax.ui.designsystem.hapticClickable
import com.vajrax.ui.designsystem.rememberHaptics
import com.vajrax.ui.features.legal.LegalDoc
import com.vajrax.ui.theme.LuminaTheme
import com.vajrax.ui.theme.VxShape
import com.vajrax.ui.theme.VxSpace
import org.jetbrains.compose.resources.stringResource

/**
 * Sign in, create an account or reset the password — one screen, three modes, so switching never
 * loses what was typed. [webGate] is the browser's start screen (the web keeps data in the account).
 */
@Composable
fun AccountScreen(
    state: AccountUiState,
    onIntent: (AccountIntent) -> Unit,
    onBack: (() -> Unit)?,
    onOpenLegal: (LegalDoc) -> Unit,
    webGate: Boolean = false
) {
    val colors = LuminaTheme.colors
    val title = when (state.mode) {
        AuthMode.SIGN_IN -> stringResource(Res.string.account_title_sign_in)
        AuthMode.CREATE -> stringResource(Res.string.account_title_create)
        AuthMode.FORGOT -> stringResource(Res.string.account_title_forgot)
    }
    Column(Modifier.fillMaxSize().background(colors.background)) {
        if (onBack != null) {
            VxTopBar(title = title, onBack = onBack)
        } else {
            Spacer(Modifier.statusBarsPadding().height(VxSpace.lg))
        }
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = FORM_MAX_WIDTH).fillMaxWidth().verticalScroll(rememberScrollState())
                    .imePadding().navigationBarsPadding().padding(horizontal = VxSpace.gutter)
            ) {
                Spacer(Modifier.height(VxSpace.lg))
                if (webGate) {
                    IconBadge(VxIcons.Cloud, tint = colors.primary, size = 56.dp, iconSize = 28.dp)
                    Spacer(Modifier.height(VxSpace.lg))
                    Text(
                        stringResource(Res.string.account_web_gate_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(Modifier.height(VxSpace.sm))
                    Text(
                        stringResource(Res.string.account_web_gate_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(VxSpace.xxl))
                }
                if (webGate && state.signedIn) {
                    // The session came back (cookie); its data is downloading.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
                        Spacer(Modifier.width(VxSpace.md))
                        Text(
                            stringResource(Res.string.account_signing_in),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface
                        )
                    }
                } else if (state.mode == AuthMode.FORGOT && state.resetSentTo != null) {
                    ResetSent(state.resetSentTo, onBackToSignIn = { onIntent(AccountIntent.SetMode(AuthMode.SIGN_IN)) })
                } else {
                    AuthForm(state, onIntent, onOpenLegal, showIntro = !webGate)
                }
                Spacer(Modifier.height(VxSpace.xxxl))
            }
        }
    }

    if (state.chooseFirstSync) FirstSyncSheet(busy = state.busy, onIntent = onIntent)
}

@Composable
private fun ColumnScope.AuthForm(
    state: AccountUiState,
    onIntent: (AccountIntent) -> Unit,
    onOpenLegal: (LegalDoc) -> Unit,
    showIntro: Boolean
) {
    val colors = LuminaTheme.colors
    val focus = LocalFocusManager.current
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val mode = state.mode
    if (showIntro) {
        Text(
            when (mode) {
                AuthMode.SIGN_IN -> stringResource(Res.string.account_intro_sign_in)
                AuthMode.CREATE -> stringResource(Res.string.account_intro_create)
                AuthMode.FORGOT -> stringResource(Res.string.account_intro_forgot)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(VxSpace.xxl))
    }
    if (mode == AuthMode.CREATE) {
        VxTextField(
            label = stringResource(Res.string.account_name),
            value = state.name,
            onValueChange = { onIntent(AccountIntent.Name(it)) },
            helper = stringResource(Res.string.account_name_helper),
            error = state.fieldErrors[AuthField.NAME],
            imeAction = ImeAction.Next,
            onImeAction = { focus.moveFocus(FocusDirection.Down) },
            autofill = ContentType.PersonFullName
        )
        Spacer(Modifier.height(VxSpace.lg))
    }
    VxTextField(
        label = stringResource(Res.string.account_email),
        value = state.email,
        onValueChange = { onIntent(AccountIntent.Email(it)) },
        error = state.fieldErrors[AuthField.EMAIL],
        keyboardType = KeyboardType.Email,
        imeAction = if (mode == AuthMode.FORGOT) ImeAction.Done else ImeAction.Next,
        onImeAction = { if (mode == AuthMode.FORGOT) onIntent(AccountIntent.Submit) else focus.moveFocus(FocusDirection.Down) },
        autofill = ContentType.EmailAddress + ContentType.Username
    )
    if (mode != AuthMode.FORGOT) {
        Spacer(Modifier.height(VxSpace.lg))
        VxTextField(
            label = stringResource(Res.string.account_password),
            value = state.password,
            onValueChange = { onIntent(AccountIntent.Password(it)) },
            helper = if (mode == AuthMode.CREATE) stringResource(Res.string.account_password_helper_create) else null,
            error = state.fieldErrors[AuthField.PASSWORD],
            keyboardType = KeyboardType.Password,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = { PasswordToggle(showPassword) { showPassword = !showPassword } },
            imeAction = ImeAction.Done,
            onImeAction = { onIntent(AccountIntent.Submit) },
            autofill = if (mode == AuthMode.CREATE) ContentType.NewPassword else ContentType.Password
        )
    }
    if (mode == AuthMode.SIGN_IN) {
        TextButton(onClick = { onIntent(AccountIntent.SetMode(AuthMode.FORGOT)) }, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(Res.string.account_forgot_link), color = colors.primary)
        }
    } else {
        Spacer(Modifier.height(VxSpace.xl))
    }
    state.formError?.let {
        Row(Modifier.fillMaxWidth().padding(bottom = VxSpace.md).semantics { liveRegion = LiveRegionMode.Polite }) {
            Icon(VxIcons.AlertCircle, null, tint = colors.statusError, modifier = Modifier.padding(end = VxSpace.sm))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.statusError)
        }
    }
    PrimaryButton(
        text = when (mode) {
            AuthMode.SIGN_IN -> stringResource(Res.string.account_sign_in)
            AuthMode.CREATE -> stringResource(Res.string.account_create)
            AuthMode.FORGOT -> stringResource(Res.string.account_send_link)
        },
        onClick = { onIntent(AccountIntent.Submit) },
        loading = state.busy,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(VxSpace.md))
    val switchTo = if (mode == AuthMode.SIGN_IN) AuthMode.CREATE else AuthMode.SIGN_IN
    TextButton(onClick = { onIntent(AccountIntent.SetMode(switchTo)) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(
            stringResource(if (mode == AuthMode.SIGN_IN) Res.string.account_switch_to_create else Res.string.account_switch_to_sign_in),
            color = colors.primary
        )
    }
    if (mode == AuthMode.CREATE) {
        Spacer(Modifier.height(VxSpace.sm))
        Text(
            stringResource(Res.string.account_legal_notice),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(VxSpace.sm)) {
            TextButton(onClick = { onOpenLegal(LegalDoc.TERMS) }) { Text(LegalDoc.TERMS.title, color = colors.primary) }
            TextButton(onClick = { onOpenLegal(LegalDoc.PRIVACY) }) { Text(LegalDoc.PRIVACY.title, color = colors.primary) }
        }
    }
}

@Composable
private fun PasswordToggle(visible: Boolean, onToggle: () -> Unit) {
    val haptics = rememberHaptics()
    IconButton(onClick = {
        haptics(VxHaptic.Tap)
        onToggle()
    }) {
        Icon(
            if (visible) VxIcons.EyeOff else VxIcons.Eye,
            contentDescription = stringResource(if (visible) Res.string.account_hide_password else Res.string.account_show_password),
            tint = LuminaTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ResetSent(email: String, onBackToSignIn: () -> Unit) {
    val colors = LuminaTheme.colors
    VxCard {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
            IconBadge(VxIcons.Check, tint = colors.statusSuccess)
            Spacer(Modifier.padding(start = VxSpace.md))
            Text(
                stringResource(Res.string.account_reset_sent_fmt, email),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface
            )
        }
    }
    Spacer(Modifier.height(VxSpace.xl))
    PrimaryButton(stringResource(Res.string.account_back_to_sign_in), onClick = onBackToSignIn, modifier = Modifier.fillMaxWidth())
}

/** First sign-in on a device that already has habits, into an account that has some too. */
@Composable
private fun FirstSyncSheet(busy: Boolean, onIntent: (AccountIntent) -> Unit) {
    val colors = LuminaTheme.colors
    VxBottomSheet(onDismiss = { if (!busy) onIntent(AccountIntent.CancelFirstSync) }) {
        Column(Modifier.padding(horizontal = VxSpace.xxl).padding(bottom = VxSpace.xxxl)) {
            Text(
                stringResource(Res.string.account_first_sync_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(VxSpace.xs))
            Text(
                stringResource(Res.string.account_first_sync_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(VxSpace.xl))
            ChoiceRow(
                title = stringResource(Res.string.account_first_sync_merge),
                hint = stringResource(Res.string.account_first_sync_merge_hint),
                icon = VxIcons.Copy,
                enabled = !busy,
                onClick = { onIntent(AccountIntent.ChooseFirstSync(FirstSync.MERGE)) }
            )
            Spacer(Modifier.height(VxSpace.md))
            ChoiceRow(
                title = stringResource(Res.string.account_first_sync_account),
                hint = stringResource(Res.string.account_first_sync_account_hint),
                icon = VxIcons.Cloud,
                enabled = !busy,
                onClick = { onIntent(AccountIntent.ChooseFirstSync(FirstSync.USE_ACCOUNT)) }
            )
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = LuminaTheme.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).clip(VxShape.control)
            .background(colors.surfaceDim)
            .then(if (enabled) Modifier.hapticClickable(kind = VxHaptic.Confirm, onClick = onClick) else Modifier)
            .padding(VxSpace.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, tint = colors.primary)
        Spacer(Modifier.padding(start = VxSpace.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Icon(VxIcons.ChevronRight, null, tint = colors.onSurfaceVariant)
    }
}

private val FORM_MAX_WIDTH = 480.dp
