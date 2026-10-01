package com.vajrax.ui.designsystem

import org.jetbrains.compose.resources.stringResource
import com.vajrax.resources.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.vajrax.ui.theme.LuminaTheme

/**
 * The app's bottom sheet. A swipe down, back press or tap outside first steps back out of a
 * sub-step ([onStepBack] returns true when it handled it), then asks before throwing away
 * [hasUnsavedChanges]. Buttons inside the sheet close it directly through [onDismiss].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VxBottomSheet(
    onDismiss: () -> Unit,
    hasUnsavedChanges: Boolean = false,
    onStepBack: (() -> Boolean)? = null,
    containerColor: Color = LuminaTheme.colors.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    var askDiscard by remember { mutableStateOf(false) }
    val dirty by rememberUpdatedState(hasUnsavedChanges)
    val stepBack by rememberUpdatedState(onStepBack)
    // The only remaining use of the older sheet-state API; every sheet goes through here.
    @Suppress("DEPRECATION")
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            when {
                target != SheetValue.Hidden -> true
                stepBack?.invoke() == true -> false
                dirty -> {
                    askDiscard = true
                    false
                }
                else -> true
            }
        }
    )
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = containerColor, content = content)
    if (askDiscard) {
        ConfirmDialog(
            title = stringResource(Res.string.common_discard_changes),
            message = stringResource(Res.string.common_what_you_changed_here_won),
            confirmLabel = stringResource(Res.string.common_discard),
            destructive = true,
            dismissLabel = stringResource(Res.string.common_keep_editing),
            onConfirm = {
                askDiscard = false
                onDismiss()
            },
            onDismiss = { askDiscard = false }
        )
    }
}
