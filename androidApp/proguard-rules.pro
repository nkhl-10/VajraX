# ==========================================
# VAJRAX: Production R8 / ProGuard Rules
# ==========================================

# 1. Preserve Line Numbers & Attributes for Crashlytics / Debugging
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# 2. SQLDelight
-keep class com.vajrax.data.local.** { *; }
-keep class app.cash.sqldelight.** { *; }

# 3. Koin Dependency Injection
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module { *; }

# 4. Kotlinx Coroutines & Serialization
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# 5. Ktor Networking
-keep class io.ktor.** { *; }

# 6. Compose Multiplatform & Jetpack Glance Widgets
-keep class androidx.compose.** { *; }
-keep class com.vajrax.ui.** { *; }
-keep class com.vajrax.android.widget.** { *; }