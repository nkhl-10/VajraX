package com.vajrax.domain.sync

/** Online work at app start (session check, sync, template library refresh); never blocks the UI. */
fun interface OnlineStartup {
    suspend fun start()
}
