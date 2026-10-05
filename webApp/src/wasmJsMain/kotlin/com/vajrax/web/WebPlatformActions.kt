package com.vajrax.web

import com.vajrax.platform.PlatformActions

/** Browser capabilities: export is a file download; no widgets or reminders on the web. */
object WebPlatformActions : PlatformActions {
    override fun exportFile(fileName: String, content: String, onResult: (Boolean) -> Unit) {
        downloadText(fileName, content)
        onResult(true)
    }

    override fun notificationsPermitted(): Boolean = false

    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) = onResult(false)

    override val appVersion: String = WEB_APP_VERSION

    /** Browser storage here doesn't last, so the web app works from the account. */
    override val requiresAccount: Boolean = true
    override val supportsWidgets: Boolean = false
    override val supportsReminders: Boolean = false
}

const val WEB_APP_VERSION = "1.0"

private fun downloadText(name: String, text: String): Unit = js(
    """{
    const url = URL.createObjectURL(new Blob([text], { type: 'application/json' }));
    const a = document.createElement('a');
    a.href = url; a.download = name; document.body.appendChild(a); a.click(); a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}"""
)
