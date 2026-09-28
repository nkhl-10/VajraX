package com.vajrax.presentation.mvi

import com.vajrax.domain.usecase.RoutineException
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
    protected open fun onUnhandledError(throwable: Throwable) {}

    protected fun userMessage(t: Throwable): String = when (t) {
        is RoutineException -> t.message ?: "Something went wrong."
        else -> "Couldn't save that change. Your data is safe — please try again."
    }

    open fun onCleared() {
        viewModelScope.cancel()
    }
}
