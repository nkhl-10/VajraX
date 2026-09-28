package com.vajrax.android

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.vajrax.android.reminders.ReminderReceiver
import com.vajrax.android.widget.NowWidgetReceiver
import com.vajrax.android.widget.TodayWidgetReceiver
import com.vajrax.app.VajraApp
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

        override fun canPinWidget(): Boolean =
            AppWidgetManager.getInstance(this@MainActivity).isRequestPinAppWidgetSupported

        override fun requestPinWidget(list: Boolean) {
            val manager = AppWidgetManager.getInstance(this@MainActivity)
            if (manager.isRequestPinAppWidgetSupported) {
                val provider = if (list) TodayWidgetReceiver::class.java else NowWidgetReceiver::class.java
                manager.requestPinAppWidget(ComponentName(this@MainActivity, provider), null, null)
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
    }
}
