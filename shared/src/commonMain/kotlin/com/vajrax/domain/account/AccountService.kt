package com.vajrax.domain.account

import com.vajrax.core.coroutines.runCatchingCancellable
import com.vajrax.core.error.AppError
import com.vajrax.core.log.VxLog
import com.vajrax.core.time.AppClock
import com.vajrax.domain.repository.ProfileRepository
import com.vajrax.domain.sync.CloudSync
import com.vajrax.domain.sync.FirstSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** What happens after signing in. */
sealed interface SignInOutcome {
    data object Done : SignInOutcome

    /** This device and the account both hold data: the user picks [FirstSync.MERGE] or [FirstSync.USE_ACCOUNT]. */
    data object ChooseFirstSync : SignInOutcome
}

/**
 * The optional account: sign-in turns on cloud backup and sync, signing out keeps working offline.
 * [isAvailable] is false where the app has no server configured; the account UI is hidden then.
 */
class AccountService(
    private val gateway: AccountGateway?,
    private val sync: CloudSync,
    private val profiles: ProfileRepository,
    private val clock: AppClock,
    private val scope: CoroutineScope
) {
    val isAvailable: Boolean get() = gateway != null

    private val _state = MutableStateFlow<AccountState>(AccountState.SignedOut)
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _notices = MutableSharedFlow<String>(extraBufferCapacity = 4)

    /** One-off sentences for a snackbar (e.g. signed out elsewhere). */
    val notices: SharedFlow<String> = _notices.asSharedFlow()

    private var pending: Account? = null
    private var started = false

    /** App start: shows the stored account at once (works offline), then confirms it with the server. */
    suspend fun start() {
        val g = gateway ?: return sync.start()
        if (!started) {
            started = true
            scope.launch { g.sessionEnded.collect { endedElsewhere() } }
        }
        val cached = g.cachedAccount()
        cached?.let { _state.value = AccountState.SignedIn(it) }
        runCatchingCancellable { g.restore() }
            .onSuccess { account ->
                when {
                    account != null -> _state.value = AccountState.SignedIn(account)
                    cached != null -> endedElsewhere()
                    // No session here (e.g. a database restored from a backup): make sure nothing is captured.
                    else -> sync.disable(removeLocalData = false)
                }
            }
            .onFailure { VxLog.w(TAG, "Couldn't confirm the session (offline?)", it) }
        sync.start()
    }

    suspend fun register(email: String, password: String, displayName: String): SignInOutcome =
        afterSignIn(requireGateway().register(email, password, displayName))

    suspend fun signIn(email: String, password: String): SignInOutcome = afterSignIn(
        requireGateway().signIn(email, password)
    )

    /** Answer to [SignInOutcome.ChooseFirstSync]. */
    suspend fun chooseFirstSync(choice: FirstSync) {
        val account = pending ?: return
        pending = null
        finish(account, choice)
    }

    /** Abandons a pending first-sync choice (the user backed out): signs out again. */
    suspend fun cancelSignIn() {
        if (pending == null) return
        pending = null
        gateway?.signOut()
    }

    suspend fun signOut(removeLocalData: Boolean) {
        gateway?.signOut()
        sync.disable(removeLocalData)
        _state.value = AccountState.SignedOut
    }

    suspend fun signOutEverywhere() {
        requireGateway().signOutEverywhere()
        sync.disable(removeLocalData = false)
        _state.value = AccountState.SignedOut
    }

    /** Deletes the account and everything stored with it on the server. */
    suspend fun deleteAccount(password: String, removeLocalData: Boolean) {
        requireGateway().deleteAccount(password)
        sync.disable(removeLocalData)
        _state.value = AccountState.SignedOut
    }

    suspend fun requestPasswordReset(email: String) = requireGateway().requestPasswordReset(email)

    suspend fun changePassword(current: String, new: String) = requireGateway().changePassword(current, new)

    suspend fun rename(displayName: String) {
        val account = requireGateway().updateName(displayName)
        _state.value = AccountState.SignedIn(account)
        profiles.saveProfile(account.displayName, account.email, clock.nowIso())
    }

    private suspend fun afterSignIn(account: Account): SignInOutcome {
        val local = sync.hasLocalData()
        val remote = runCatchingCancellable { sync.accountHasData() }.getOrDefault(false)
        if (local && remote) {
            pending = account
            return SignInOutcome.ChooseFirstSync
        }
        // Only one side has data (or neither): nothing to decide.
        finish(account, if (remote) FirstSync.USE_ACCOUNT else FirstSync.MERGE)
        return SignInOutcome.Done
    }

    private suspend fun finish(account: Account, choice: FirstSync) {
        val localName = profiles.getProfile()?.displayName.orEmpty()
        sync.enable(choice)
        // The account's name wins; an account without one takes the name already on this phone.
        val named = if (account.displayName.isBlank() && localName.isNotBlank()) {
            runCatchingCancellable { requireGateway().updateName(localName) }.getOrDefault(account)
        } else {
            account
        }
        profiles.saveProfile(named.displayName, named.email, clock.nowIso())
        _state.value = AccountState.SignedIn(named)
    }

    private suspend fun endedElsewhere() {
        if (_state.value !is AccountState.SignedIn) return
        sync.disable(removeLocalData = false)
        _state.value = AccountState.SignedOut
        _notices.tryEmit(SIGNED_OUT_NOTICE)
    }

    private fun requireGateway(): AccountGateway = gateway ?: throw AppError.Offline()

    companion object {
        private const val TAG = "Account"
        const val SIGNED_OUT_NOTICE = "You were signed out. Everything is still on this device."
    }
}
