package com.vajrax.ui.features.legal

/**
 * In-app legal documents. They are bundled so they can be read offline; the same text is kept in
 * doc/legal/ for the public Privacy Policy URL that Google Play requires. Update both together,
 * and change [EFFECTIVE_DATE] whenever the substance changes.
 */
enum class LegalDoc(val title: String) {
    PRIVACY("Privacy policy"),
    TERMS("Terms of use"),
    HEALTH("Health notice"),
    LICENSES("Open-source licenses");

    companion object {
        fun of(name: String): LegalDoc = entries.firstOrNull { it.name == name } ?: PRIVACY
    }
}

/** [bullets] render as a list under the section's [paragraphs]. */
data class LegalSection(val heading: String?, val paragraphs: List<String>, val bullets: List<String> = emptyList())

object LegalTexts {
    const val EFFECTIVE_DATE = "30 September 2026"

    fun sections(doc: LegalDoc): List<LegalSection> = when (doc) {
        LegalDoc.PRIVACY -> privacy
        LegalDoc.TERMS -> terms
        LegalDoc.HEALTH -> health
        LegalDoc.LICENSES -> licenses
    }

    private val privacy = listOf(
        LegalSection(
            "In short",
            listOf(
                "VAJRAX works offline. Everything you enter stays on your device. There are no accounts, " +
                    "servers, ads, analytics or trackers, and the app has no permission to use the internet, " +
                    "so it cannot send your data anywhere."
            )
        ),
        LegalSection(
            "What the app stores on your device",
            listOf("Android keeps this in the app's private storage, which other apps cannot read."),
            listOf(
                "Your name and email, only if you add them",
                "Your goals, wake-up time and preferences",
                "Routines, habits, check-ins, notes, skip reasons, goals and reflections",
                "Reminder, widget and display settings"
            )
        ),
        LegalSection(
            "What we collect",
            listOf("Nothing. We never receive, sell or share your data, and it is not used for advertising or profiling.")
        ),
        LegalSection(
            "Permissions",
            listOf("The app does not ask for location, contacts, photos, camera, microphone or your files."),
            listOf(
                "Notifications: to show the habit reminders you turn on",
                "Run at start-up: to restore your reminders after the phone restarts"
            )
        ),
        LegalSection(
            "Device backup",
            listOf(
                "If backup is on for your phone, Android may include VAJRAX data in your device backup so you can " +
                    "restore it on a new phone. Google handles these backups under your Google account settings; " +
                    "we cannot access them. You can turn backup off in Android Settings."
            )
        ),
        LegalSection(
            "Reminders, widgets and exports",
            listOf(
                "Reminders and widgets can show habit names on the lock screen and home screen. To keep them " +
                    "private, choose Generic or No details in Profile › Notifications, and hide names on the widget.",
                "An export file holds your full history and notes. It is saved only where you choose; keep it and " +
                    "share it with care."
            )
        ),
        LegalSection(
            "Your choices",
            listOf("You can see and change all of your data in the app at any time."),
            listOf(
                "Profile › Export my data saves a full copy as a JSON file",
                "Profile › Delete all data erases everything from this device",
                "Uninstalling the app also removes its data from the device"
            )
        ),
        LegalSection(
            "Children",
            listOf("VAJRAX is not directed at children under 13. It does not collect data from anyone, including children.")
        ),
        LegalSection(
            "Changes",
            listOf(
                "If a future version adds a feature that sends data off your device, such as cloud sync, this policy " +
                    "will be updated with a new effective date before that version is released."
            )
        ),
        LegalSection(
            "Contact",
            listOf("Use the developer contact details on the VAJRAX page in Google Play.")
        )
    )

    private val terms = listOf(
        LegalSection(
            "Using VAJRAX",
            listOf(
                "You may use VAJRAX for your own personal habit tracking. You must be at least 13 years old, or the " +
                    "minimum age required where you live, to use it."
            )
        ),
        LegalSection(
            "Your data",
            listOf(
                "Your data stays on your device and belongs to you. Because we never receive it, we cannot restore it " +
                    "if it is lost. Use Export my data or your device backup to keep a copy."
            )
        ),
        LegalSection(
            "Templates and health",
            listOf(
                "Templates are general suggestions. Change or skip anything that isn't right for you. VAJRAX is not " +
                    "medical advice; see the Health notice."
            )
        ),
        LegalSection(
            "No warranty",
            listOf("VAJRAX is provided as is, without warranties of any kind, to the extent the law allows.")
        ),
        LegalSection(
            "Liability",
            listOf(
                "To the extent the law allows, we are not liable for indirect or consequential losses from using the " +
                    "app, including lost data."
            )
        ),
        LegalSection(
            "Your rights",
            listOf("Nothing in these terms limits rights you have under the consumer laws of your country.")
        ),
        LegalSection(
            "Changes",
            listOf("These terms may change in a new version of the app. The effective date above changes when they do.")
        )
    )

    private val health = listOf(
        LegalSection(
            null,
            listOf(
                "VAJRAX helps you plan and track habits. It is not a medical device and does not diagnose, treat or " +
                    "prevent any condition.",
                "Templates that involve exercise, fasting, cold showers, sleep changes or diet are general wellness " +
                    "ideas, not advice for you personally. Talk to a doctor or qualified professional before starting " +
                    "them, especially if you have a health condition, are pregnant or take medication.",
                "Stop any activity that causes pain, dizziness or discomfort.",
                "Report figures describe your own check-ins. They are not health measurements."
            )
        )
    )

    private const val APACHE = "Apache License 2.0"

    private val licenses = listOf(
        LegalSection(
            "Libraries",
            listOf(
                "VAJRAX is built with the open-source software below. Libraries marked $APACHE are licensed under " +
                    "the Apache License, Version 2.0 (https://www.apache.org/licenses/LICENSE-2.0) and are " +
                    "distributed on an \"AS IS\" basis, without warranties or conditions of any kind."
            ),
            listOf(
                "Kotlin, kotlinx.coroutines, kotlinx-datetime, kotlinx.serialization · JetBrains s.r.o. · $APACHE",
                "Compose Multiplatform, Navigation · JetBrains s.r.o. and The Android Open Source Project · $APACHE",
                "AndroidX Activity, Core, Glance, WorkManager · The Android Open Source Project · $APACHE",
                "Koin · Kotzilla and Koin contributors · $APACHE",
                "SQLDelight · Square, Inc. · $APACHE",
                "Ktor · JetBrains s.r.o. · $APACHE",
                "OkHttp, Okio · Square, Inc. · $APACHE"
            )
        ),
        LegalSection(
            "Lucide icons · ISC License",
            listOf(
                "Copyright (c) for portions of Lucide are held by Cole Bemis 2013-2022 as part of Feather (MIT). " +
                    "All other copyright (c) for Lucide are held by Lucide Contributors 2022.",
                "Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee " +
                    "is hereby granted, provided that the above copyright notice and this permission notice appear in " +
                    "all copies.",
                "THE SOFTWARE IS PROVIDED \"AS IS\" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS " +
                    "SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE " +
                    "AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES " +
                    "WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, " +
                    "NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE " +
                    "OF THIS SOFTWARE."
            )
        )
    )
}
