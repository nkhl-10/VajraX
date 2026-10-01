package com.vajrax.core.log

/**
 * Minimal logging seam shared by every layer. A platform plugs in a [sink] (Android: logcat in
 * debug builds); without one, nothing is written. Messages never include habit names or notes.
 */
object VxLog {
    @kotlin.concurrent.Volatile
    var sink: ((tag: String, message: String, error: Throwable?) -> Unit)? = null

    fun w(tag: String, message: String, error: Throwable? = null) {
        sink?.invoke(tag, message, error)
    }
}
