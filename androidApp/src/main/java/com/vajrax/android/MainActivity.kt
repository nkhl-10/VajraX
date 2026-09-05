package com.vajrax.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vajrax.app.VajraApp
import com.vajrax.data.local.DatabaseDriverFactory
import com.vajrax.di.initKoin
import com.vajrax.platform.WidgetController
import com.vajrax.widget.AndroidWidgetController
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Koin with Android platform drivers and controllers
        if (GlobalContext.getOrNull() == null) {
            initKoin {
                modules(
                    module {
                        single { DatabaseDriverFactory(applicationContext) }
                        single<WidgetController> { AndroidWidgetController(applicationContext) }
                    }
                )
            }
        }

        setContent {
            VajraApp()
        }
    }
}
