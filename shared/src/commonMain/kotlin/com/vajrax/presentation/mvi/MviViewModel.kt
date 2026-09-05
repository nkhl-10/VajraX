package com.vajrax.presentation.mvi

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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

/**
 * Phase 8: Base MVI ViewModel for Unidirectional Data Flow (UDF).
 * Enforces @Immutable UI state, single source of truth, and structured coroutine lifecycles.
 */
abstract class MviViewModel<STATE, INTENT, EFFECT>(initialState: STATE) {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<EFFECT>()
    val effect: SharedFlow<EFFECT> = _effect.asSharedFlow()

    abstract fun sendIntent(intent: INTENT)

    protected fun updateState(reducer: STATE.() -> STATE) {
        _uiState.value = _uiState.value.reducer()
    }

    protected suspend fun sendEffect(effect: EFFECT) {
        _effect.emit(effect)
    }

    open fun onCleared() {
        viewModelScope.cancel()
    }
}
