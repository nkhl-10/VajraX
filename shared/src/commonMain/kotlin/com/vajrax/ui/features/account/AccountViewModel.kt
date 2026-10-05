package com.vajrax.ui.features.account

import com.vajrax.contract.Limits
import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.error.AppError
import com.vajrax.domain.account.Account
import com.vajrax.domain.account.AccountService
import com.vajrax.domain.account.AccountState
import com.vajrax.domain.account.SignInOutcome
import com.vajrax.domain.repository.TrackerRepository
import com.vajrax.domain.sync.CloudSync
import com.vajrax.domain.sync.FirstSync
import com.vajrax.domain.sync.SyncStatus
import com.vajrax.presentation.mvi.MviViewModel
import com.vajrax.resources.Res
import com.vajrax.resources.account_deleted
import com.vajrax.resources.account_err_email
import com.vajrax.resources.account_err_password_empty
import com.vajrax.resources.account_err_password_short
import com.vajrax.resources.account_password_changed
import com.vajrax.resources.account_profile_updated
import com.vajrax.resources.account_signed_in
import com.vajrax.resources.account_signed_out
import com.vajrax.resources.account_signed_out_everywhere
import com.vajrax.resources.account_signed_out_removed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

enum class AuthMode { SIGN_IN, CREATE, FORGOT }

enum class AuthField { NAME, EMAIL, PASSWORD, CURRENT_PASSWORD }

data class AccountUiState(
    /** False where no server is configured: the account UI is hidden. */
    val available: Boolean = false,
    val account: Account? = null,
    val sync: SyncStatus = SyncStatus.Off,
    // Sign-in / create / reset form
    val mode: AuthMode = AuthMode.SIGN_IN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val fieldErrors: Map<AuthField, String> = emptyMap(),
    /** A failure that isn't about one field (offline, server). */
    val formError: String? = null,
    val busy: Boolean = false,
    /** Forgot password: the email the link went to. */
    val resetSentTo: String? = null,
    /** Both this device and the account hold data: ask merge vs use the account. */
    val chooseFirstSync: Boolean = false
) {
    val signedIn: Boolean get() = account != null

    /** Signed in and the first download has finished (or can't happen right now). */
    val syncSettled: Boolean get() = sync is SyncStatus.UpToDate || sync is SyncStatus.Waiting || sync is SyncStatus.Failed
}

sealed interface AccountIntent {
    data class SetMode(val mode: AuthMode) : AccountIntent
    data class Name(val value: String) : AccountIntent
    data class Email(val value: String) : AccountIntent
    data class Password(val value: String) : AccountIntent
    data object Submit : AccountIntent
    data class ChooseFirstSync(val choice: FirstSync) : AccountIntent
    data object CancelFirstSync : AccountIntent
    data object SyncNow : AccountIntent
    data class SignOut(val removeLocalData: Boolean) : AccountIntent
    data object SignOutEverywhere : AccountIntent
    data class DeleteAccount(val password: String, val removeLocalData: Boolean) : AccountIntent
    data class ChangePassword(val current: String, val new: String) : AccountIntent
    data object ClearErrors : AccountIntent
    data class Rename(val name: String) : AccountIntent
}

sealed interface AccountEffect {
    /** Signed in and the first sync ran; [hasRoutine] tells onboarding to go straight Home. */
    data class SignedIn(val hasRoutine: Boolean) : AccountEffect
    data class Message(val text: String) : AccountEffect

    /** A dialog action finished; the dialog can close. */
    data object DialogDone : AccountEffect

    /** This device's data was removed (sign out with removal, or account deletion with removal). */
    data object LocalDataRemoved : AccountEffect
}

/**
 * The optional account: sign in, create, reset password, and the Profile account card (status,
 * sync now, sign out, delete). Errors from the server are shown next to the field they concern.
 */
class AccountViewModel(
    private val account: AccountService,
    private val sync: CloudSync,
    private val trackers: TrackerRepository
) : MviViewModel<AccountUiState, AccountIntent, AccountEffect>(AccountUiState(available = account.isAvailable)) {

    init {
        viewModelScope.launch {
            combine(account.state, sync.status) { a, s -> a to s }.collect { (a, s) ->
                updateState { copy(account = (a as? AccountState.SignedIn)?.account, sync = s) }
            }
        }
        viewModelScope.launch { account.notices.collect { sendEffect(AccountEffect.Message(it)) } }
    }

    override fun sendIntent(intent: AccountIntent) {
        when (intent) {
            is AccountIntent.SetMode, is AccountIntent.Name, is AccountIntent.Email, is AccountIntent.Password,
            AccountIntent.ClearErrors -> editForm(intent)
            AccountIntent.Submit -> submit()
            else -> act(intent)
        }
    }

    private fun editForm(intent: AccountIntent) = updateState {
        when (intent) {
            is AccountIntent.SetMode ->
                copy(mode = intent.mode, fieldErrors = emptyMap(), formError = null, resetSentTo = null, password = "")
            is AccountIntent.Name -> copy(name = intent.value.take(Limits.DISPLAY_NAME_MAX), fieldErrors = fieldErrors - AuthField.NAME)
            is AccountIntent.Email -> copy(email = intent.value.take(Limits.EMAIL_MAX), fieldErrors = fieldErrors - AuthField.EMAIL)
            is AccountIntent.Password ->
                copy(password = intent.value.take(Limits.PASSWORD_MAX), fieldErrors = fieldErrors - AuthField.PASSWORD)
            else -> copy(fieldErrors = emptyMap(), formError = null)
        }
    }

    @Suppress("CyclomaticComplexMethod") // one branch per account action
    private fun act(intent: AccountIntent) {
        when (intent) {
            is AccountIntent.ChooseFirstSync -> run("first-sync") {
                updateState { copy(chooseFirstSync = false) }
                account.chooseFirstSync(intent.choice)
                signedIn()
            }
            AccountIntent.CancelFirstSync -> run("cancel") {
                updateState { copy(chooseFirstSync = false) }
                account.cancelSignIn()
            }
            AccountIntent.SyncNow -> viewModelScope.launch { sync.syncNow() }
            is AccountIntent.SignOut -> run("sign-out") {
                account.signOut(intent.removeLocalData)
                if (intent.removeLocalData) sendEffect(AccountEffect.LocalDataRemoved)
                done(getString(if (intent.removeLocalData) Res.string.account_signed_out_removed else Res.string.account_signed_out))
            }
            AccountIntent.SignOutEverywhere -> run("sign-out-everywhere") {
                account.signOutEverywhere()
                done(getString(Res.string.account_signed_out_everywhere))
            }
            is AccountIntent.DeleteAccount -> run("delete") {
                account.deleteAccount(intent.password, intent.removeLocalData)
                if (intent.removeLocalData) sendEffect(AccountEffect.LocalDataRemoved)
                done(getString(Res.string.account_deleted))
            }
            is AccountIntent.ChangePassword -> run("change-password", passwordField = AuthField.CURRENT_PASSWORD) {
                account.changePassword(intent.current, intent.new)
                done(getString(Res.string.account_password_changed))
            }
            is AccountIntent.Rename -> run("rename") {
                account.rename(intent.name.trim())
                done(getString(Res.string.account_profile_updated))
            }
            else -> Unit
        }
    }

    private fun submit() {
        val s = currentState()
        val errors = buildMap {
            if (!EMAIL.matches(s.email.trim())) put(AuthField.EMAIL, Res.string.account_err_email)
            when {
                s.mode == AuthMode.FORGOT -> Unit
                s.password.isEmpty() -> put(AuthField.PASSWORD, Res.string.account_err_password_empty)
                s.mode == AuthMode.CREATE && s.password.length < Limits.PASSWORD_MIN -> put(
                    AuthField.PASSWORD,
                    Res.string.account_err_password_short
                )
            }
        }
        if (errors.isNotEmpty()) {
            viewModelScope.launch {
                val texts = errors.mapValues { (_, res) -> getString(res) }
                updateState { copy(fieldErrors = texts) }
            }
            return
        }
        run("submit") {
            when (s.mode) {
                AuthMode.FORGOT -> {
                    account.requestPasswordReset(s.email.trim())
                    updateState { copy(resetSentTo = s.email.trim()) }
                }
                AuthMode.SIGN_IN -> outcome(account.signIn(s.email, s.password))
                AuthMode.CREATE -> outcome(account.register(s.email, s.password, s.name))
            }
        }
    }

    private suspend fun outcome(result: SignInOutcome) {
        when (result) {
            SignInOutcome.Done -> signedIn()
            SignInOutcome.ChooseFirstSync -> updateState { copy(chooseFirstSync = true) }
        }
    }

    private suspend fun signedIn() {
        updateState { copy(password = "", name = "", fieldErrors = emptyMap(), formError = null) }
        sendEffect(AccountEffect.Message(getString(Res.string.account_signed_in)))
        sendEffect(AccountEffect.SignedIn(hasRoutine = trackers.getActiveTracker() != null))
    }

    private suspend fun done(message: String) {
        sendEffect(AccountEffect.DialogDone)
        sendEffect(AccountEffect.Message(message))
    }

    /** Runs an account call with a busy state; server problems land on the matching field. */
    private fun run(tag: String, passwordField: AuthField = AuthField.PASSWORD, block: suspend () -> Unit) {
        if (currentState().busy) return
        viewModelScope.launch {
            updateState { copy(busy = true, formError = null) }
            runCatchingCancellable { block() }.onFailure { e ->
                val fields = (e as? AppError.Rejected)?.fieldErrors.orEmpty().mapNotNull { (key, text) ->
                    when (key) {
                        "email" -> AuthField.EMAIL to text
                        "password" -> AuthField.PASSWORD to text
                        "newPassword" -> AuthField.PASSWORD to text
                        "displayName" -> AuthField.NAME to text
                        else -> null
                    }
                }.toMap()
                val wrongPassword = (e as? AppError.Rejected)?.code == WRONG_PASSWORD && tag != "submit"
                updateState {
                    copy(
                        fieldErrors = if (wrongPassword) mapOf(passwordField to userMessage(e)) else fields,
                        formError = if (fields.isEmpty() && !wrongPassword) userMessage(e) else null
                    )
                }
            }
            updateState { copy(busy = false) }
        }
    }

    private companion object {
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        const val WRONG_PASSWORD = "invalid_credentials"
    }
}
