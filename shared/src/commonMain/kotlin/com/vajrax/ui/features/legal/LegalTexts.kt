package com.vajrax.ui.features.legal

/**
 * In-app legal documents, bundled so they can be read offline. The privacy policy and terms are
 * generated from the Markdown files in doc/legal, which the website also publishes (LegalTextsMatchPublicCopiesTest
 * fails when they differ). Change [EFFECTIVE_DATE] whenever the substance changes.
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
    const val EFFECTIVE_DATE = "5 October 2026"

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
                "VAJRAX works fully on your device without an account. An account is optional: if you create one, " +
                    "your habit data is also kept on our server so it is backed up and you can use it on your other " +
                    "devices and on the web. There are no ads, analytics or trackers, and we never sell your data."
            )
        ),
        LegalSection(
            "Without an account",
            listOf(
                "Everything you enter stays on your device, in the app's private storage, which other apps cannot " +
                    "read. Nothing is sent to us."
            ),
            listOf(
                "Your name and email, only if you add them",
                "Your goals, wake-up time and preferences",
                "Routines, habits, check-ins, notes, skip reasons, goals and reflections",
                "Reminder, widget and display settings"
            )
        ),
        LegalSection(
            "With an account",
            listOf(
                "When you create an account and while you stay signed in, we store:"
            ),
            listOf(
                "Your email address and the name you choose",
                "Your password only as a one-way Argon2id hash; we never see or store the password itself",
                "Your routines, habits, check-ins, notes, skip reasons, goals, reflections, the templates you make, " +
                    "your wake-up time and the goals you picked during setup",
                "Your sign-in sessions, with the device name they came from, so you can sign out everywhere",
                "A short security log of account events (sign-in, failed sign-in, password change, account deletion) " +
                    "with a salted hash of the network address, never the address itself"
            )
        ),
        LegalSection(
            null,
            listOf(
                "Theme, reminder, widget and notification settings stay on each device and are not uploaded."
            )
        ),
        LegalSection(
            "How we use it",
            listOf(
                "Only to run the service: to sign you in, back up and sync your data between your devices, send " +
                    "password-reset emails, and keep accounts safe from misuse. We do not use your data for advertising " +
                    "or profiling and we do not sell it.",
                "We share it only with the companies that run the service for us, under their data-protection terms: " +
                    "Google Cloud, which hosts the server and database, and the email provider that delivers password-" +
                    "reset emails. We disclose data to authorities only when the law requires it."
            )
        ),
        LegalSection(
            "Where your data is kept",
            listOf(
                "On Google Cloud servers in India (Mumbai region). Data is encrypted in transit (HTTPS) and at rest. " +
                    "Like any website, our hosting provider records technical request logs (time, address and the page or" +
                    " service requested) for security and troubleshooting; they are kept for 30 days.",
                "When the app checks for new templates it sends no personal data."
            )
        ),
        LegalSection(
            "How long we keep it",
            emptyList(),
            listOf(
                "Account data: until you delete your account",
                "Records you delete: a deletion marker is kept for up to 180 days so all your devices remove the " +
                    "record, then it is erased",
                "Sign-in sessions: until they expire (60 days unused) and 30 days after that",
                "Security log: 400 days; after an account is deleted it keeps only an account number, never your " +
                    "email or data",
                "Database backups: up to 7 days, then overwritten"
            )
        ),
        LegalSection(
            "Your choices and rights",
            emptyList(),
            listOf(
                "Use VAJRAX without an account, or sign out at any time; your data stays on the device, and you can " +
                    "choose to remove it",
                "See and change your data in the app at any time; Profile › Export my data saves a full copy as a " +
                    "JSON file",
                "Delete your account in the app (Profile › Delete account) or on the web at /delete-account. This " +
                    "removes the account and everything stored with it from our server at once; backups expire within 7 " +
                    "days",
                "Depending on where you live (for example the EU or UK GDPR, India's Digital Personal Data Protection" +
                    " Act or California's CCPA), you may also have the right to access, correct, port or object to the " +
                    "use of your data. Contact us and we will reply within 30 days"
            )
        ),
        LegalSection(
            "Permissions (Android)",
            emptyList(),
            listOf(
                "Internet and network state: only for the optional account (backup and sync) and for new templates",
                "Notifications: to show the habit reminders you turn on",
                "Run at start-up: to restore your reminders after the phone restarts"
            )
        ),
        LegalSection(
            null,
            listOf(
                "The app does not ask for location, contacts, photos, camera, microphone or your files."
            )
        ),
        LegalSection(
            "Device backup",
            listOf(
                "If backup is on for your phone, Android may include VAJRAX data in your device backup so you can " +
                    "restore it on a new phone. Google handles these backups under your Google account settings; we " +
                    "cannot access them. Your sign-in is not included: sign in again on the new phone."
            )
        ),
        LegalSection(
            "Reminders, widgets and exports",
            listOf(
                "Reminders and widgets can show habit names on the lock screen and home screen. To keep them private," +
                    " choose Generic or No details in Profile › Notifications, and hide names on the widget.",
                "An export file holds your full history and notes. It is saved only where you choose; keep it and " +
                    "share it with care."
            )
        ),
        LegalSection(
            "Security",
            listOf(
                "Passwords are hashed with Argon2id, sign-in tokens are short-lived and renewed securely, repeated " +
                    "failed sign-ins are slowed down, and you can end every session at once with Sign out on all devices."
            )
        ),
        LegalSection(
            "Children",
            listOf(
                "VAJRAX is not directed at children under 13, and you must be at least 13 to create an account."
            )
        ),
        LegalSection(
            "Changes",
            listOf(
                "We will update this policy and its effective date before we change how your data is handled, and " +
                    "tell you in the app about significant changes."
            )
        ),
        LegalSection(
            "Contact",
            listOf(
                "Use the developer contact details on the VAJRAX page in Google Play or on the VAJRAX website's " +
                    "support page."
            )
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
                "Your data belongs to you. Without an account it stays on your device, so we cannot restore it if it " +
                    "is lost; use Export my data or your device backup to keep a copy. With an account, your data is also" +
                    " backed up on our server until you delete it."
            )
        ),
        LegalSection(
            "Your account",
            listOf(
                "An account is optional. Keep your password to yourself and use one you don't use elsewhere. You are " +
                    "responsible for what happens in your account. You can delete it at any time in the app or on the " +
                    "website."
            )
        ),
        LegalSection(
            "Fair use",
            listOf(
                "Don't misuse the service: no attempts to break into accounts or the server, to overload it, or to " +
                    "use it for anything unlawful. We may suspend accounts that do, and we may limit unusually heavy use " +
                    "to keep the service working for everyone."
            )
        ),
        LegalSection(
            "The service",
            listOf(
                "We work to keep backup and sync available, but we can't promise it will always be. We may change the" +
                    " service. If we ever stop it, we will give notice in the app first, and the app keeps working on " +
                    "your device."
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
            "Health notice",
            listOf(
                "VAJRAX helps you plan and track habits. It is not a medical device and does not diagnose, treat or " +
                    "prevent any condition.",
                "Templates that involve exercise, fasting, cold showers, sleep changes or diet are general wellness " +
                    "ideas, not advice for you personally. Talk to a doctor or qualified professional before starting " +
                    "them, especially if you have a health condition, are pregnant or take medication.",
                "Stop any activity that causes pain, dizziness or discomfort. Report figures describe your own check-" +
                    "ins. They are not health measurements."
            )
        ),
        LegalSection(
            "No warranty",
            listOf(
                "VAJRAX is provided as is, without warranties of any kind, to the extent the law allows."
            )
        ),
        LegalSection(
            "Liability",
            listOf(
                "To the extent the law allows, we are not liable for indirect or consequential losses from using the " +
                    "app or the service, including lost data."
            )
        ),
        LegalSection(
            "Your rights",
            listOf(
                "Nothing in these terms limits rights you have under the consumer laws of your country."
            )
        ),
        LegalSection(
            "Changes",
            listOf(
                "These terms may change. The effective date above changes when they do, and we tell you in the app " +
                    "about significant changes."
            )
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
                "OkHttp, Okio · Square, Inc. · $APACHE",
                "Web app: sql.js · sql.js contributors · MIT License; js-joda · js-joda contributors · BSD 3-Clause License"
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
