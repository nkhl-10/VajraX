package com.vajrax.android

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.vajrax.android.reminders.ReminderReceiver
import com.vajrax.android.widget.NowWidgetReceiver
import com.vajrax.android.widget.TodayWidgetReceiver
import com.vajrax.app.VajraApp
import com.vajrax.core.time.TimeFormat
import com.vajrax.platform.PlatformActions

class MainActivity : ComponentActivity() {

    private var pendingExport: String? = null
    private var exportCallback: ((Boolean) -> Unit)? = null
    private var permissionCallback: ((Boolean) -> Unit)? = null
    private var barsDark: Boolean? = null

    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val content = pendingExport
        val ok = if (uri != null && content != null) {
            runCatching {
                contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) } != null
            }.getOrDefault(false)
        } else false
        pendingExport = null
        exportCallback?.invoke(ok)
        exportCallback = null
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionCallback?.invoke(granted)
        permissionCallback = null
    }

    private val platform = object : PlatformActions {
        override fun exportFile(fileName: String, content: String, onResult: (Boolean) -> Unit) {
            pendingExport = content
            exportCallback = onResult
            createDocument.launch(fileName)
        }

        override fun notificationsPermitted(): Boolean =
            Build.VERSION.SDK_INT < 33 ||
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        override fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
            if (notificationsPermitted()) {
                onResult(true)
                return
            }
            permissionCallback = onResult
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        override fun setSystemBarsDark(dark: Boolean) {
            if (barsDark == dark) return
            barsDark = dark
            val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }

        override val supportsBackdropBlur: Boolean get() = Build.VERSION.SDK_INT >= 31

        override fun canPinWidget(): Boolean =
            AppWidgetManager.getInstance(this@MainActivity).isRequestPinAppWidgetSupported

        override fun requestPinWidget(list: Boolean) {
            val manager = AppWidgetManager.getInstance(this@MainActivity)
            if (manager.isRequestPinAppWidgetSupported) {
                val provider = if (list) TodayWidgetReceiver::class.java else NowWidgetReceiver::class.java
                manager.requestPinAppWidget(ComponentName(this@MainActivity, provider), null, null)
            }
        }

        override fun openNotificationSettings() {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            runCatching { startActivity(intent) }.onFailure {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.fromParts("package", packageName, null)))
            }
        }

        override fun sendTestReminder() {
            sendBroadcast(Intent(this@MainActivity, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_TEST))
        }

        override val appVersion: String
            get() = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "1.0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { VajraApp(platform) }
        if (savedInstanceState == null) handleShortcut(intent)
    }

    override fun onResume() {
        super.onResume()
        // The 12/24-hour setting can change while the app is in the background.
        TimeFormat.use24Hour = DateFormat.is24HourFormat(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcut(intent)
    }

    /** App-icon long-press shortcuts (res/xml/shortcuts.xml): add a home-screen widget. */
    private fun handleShortcut(intent: Intent?) {
        val list = when (intent?.action) {
            ACTION_ADD_WIDGET_TODAY -> true
            ACTION_ADD_WIDGET_NOW -> false
            else -> return
        }
        if (platform.canPinWidget()) {
            platform.requestPinWidget(list)
        } else {
            Toast.makeText(this, "Long-press your home screen → Widgets → VAJRAX", Toast.LENGTH_LONG).show()
        }
        // Consume the action so a configuration change doesn't ask again.
        intent.action = Intent.ACTION_MAIN
    }

    companion object {
        const val ACTION_ADD_WIDGET_TODAY = "com.vajrax.android.action.ADD_WIDGET_TODAY"
        const val ACTION_ADD_WIDGET_NOW = "com.vajrax.android.action.ADD_WIDGET_NOW"
    }
}
