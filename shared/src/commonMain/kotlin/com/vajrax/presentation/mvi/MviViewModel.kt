package com.vajrax.presentation.mvi

import com.vajrax.core.log.VxLog
import com.vajrax.domain.usecase.RoutineException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Base MVI ViewModel for unidirectional data flow.
 * State updates are atomic; one-off effects are buffered so emitting never suspends
 * when no screen is currently collecting them.
 */
abstract class MviViewModel<STATE, INTENT, EFFECT>(initialState: STATE) {
    private val errorHandler = CoroutineExceptionHandler { _, throwable -> onUnhandledError(throwable) }
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob() + errorHandler)

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EFFECT>(extraBufferCapacity = 16)
    val effect: SharedFlow<EFFECT> = _effect.asSharedFlow()

    abstract fun sendIntent(intent: INTENT)

    private val _loadError = MutableStateFlow<String?>(null)

    /** Set when the screen's data couldn't be read; the screen shows it with a Retry button. */
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loader: Pair<CoroutineContext, suspend CoroutineScope.() -> Unit>? = null
    private var loadJob: Job? = null

    /**
     * Starts the screen's data pipeline so that a failure becomes [loadError] (instead of an
     * endless loading skeleton) and [retryLoad] can start it again.
     */
    protected fun launchLoad(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) {
        loader = context to block
        startLoad()
    }

    private fun startLoad() {
        val (context, block) = loader ?: return
        loadJob?.cancel()
        _loadError.value = null
        loadJob = viewModelScope.launch(context) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                VxLog.w(tag, "Couldn't load screen data", e)
                _loadError.value = LOAD_ERROR
            }
        }
    }

    /** Retry button on a load error. */
    fun retryLoad() = startLoad()

    private val tag: String get() = this::class.simpleName ?: "ViewModel"

    /**
     * Runs an upstream only while a screen is collecting [uiState] (plus a short grace period so
     * tab switches don't restart it). Hidden tabs then stop recomputing on every database change,
     * which keeps check-ins on the visible screen smooth.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    protected fun <T> Flow<T>.whileVisible(): Flow<T> =
        _uiState.subscriptionCount
            .map { it > 0 }
            .debounce { visible -> if (visible) 0L else 5_000L }
            .distinctUntilChanged()
            .flatMapLatest { visible -> if (visible) this else emptyFlow() }

    protected fun updateState(reducer: STATE.() -> STATE) {
        _uiState.update { it.reducer() }
    }

    protected fun currentState(): STATE = _uiState.value

    protected suspend fun sendEffect(effect: EFFECT) {
        _effect.emit(effect)
    }

    protected fun trySendEffect(effect: EFFECT) {
        _effect.tryEmit(effect)
    }

    /** Last-resort handler so a failed write never crashes the app; screens show [userMessage]. */
    protected open fun onUnhandledError(throwable: Throwable) {
        VxLog.w(tag, "Unhandled error", throwable)
    }

    protected fun userMessage(t: Throwable): String = when (t) {
        is RoutineException -> t.message ?: "Something went wrong."
        is com.vajrax.core.error.AppError -> t.message ?: "Something went wrong. Please try again."
        else -> "Couldn't save that change. Your data is safe — please try again."
    }

    open fun onCleared() {
        viewModelScope.cancel()
    }

    companion object {
        const val LOAD_ERROR = "We couldn't load this. Your data is safe."
    }
}
