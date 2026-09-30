# VAJRAX global release: requirements and status

Scope: the offline Android release (`com.vajrax.android`, targetSdk 36, minSdk 26). Every rule below is either met in the build, done in Play Console, or not applicable, with the reason. Status as of 30 Sep 2026.

Legend: **Done** = in the app/build now · **Console** = a Play Console step, nothing to build · **N/A** = does not apply to this release (reason given) · **Open** = still to do before release.

## 1. Rules that apply to this release

| Area | Rule | Status | Evidence / action |
|---|---|---|---|
| Target API | The app MUST target the current Play floor. | Done | `targetSdk = 36`, `compileSdk = 36`. |
| 16 KB pages | Native libraries MUST be 16 KB aligned (apps targeting API 35+). | Done | Only `libandroidx.graphics.path.so`; all LOAD segments align to 0x4000 on 4 ABIs. |
| Permissions | Request only what the core features need. | Done | Declared: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`. `INTERNET` (from the unused sync client) and `FOREGROUND_SERVICE` (WorkManager) are removed with `tools:node="remove"`. `WAKE_LOCK` and `ACCESS_NETWORK_STATE` stay: normal permissions WorkManager needs for widgets. No exact alarms, location, storage or restricted permissions. |
| Privacy policy | MUST be linked in Play Console and readable in the app. | Done / Console | In app: Profile › About › Privacy policy, and linked from the Welcome screen. Public copy: `doc/legal/privacy-policy.md`; host it and paste the URL into App content › Privacy policy. |
| Data safety | The form MUST match what the app does. | Console | Answer: collects no data, shares no data. Basis: no network permission, all data stays in app-private storage. If cloud sync ships, re-add `INTERNET`, update the policy and this form in the same release. |
| User data | Personal data MUST be stored securely and its handling disclosed. | Done | App-private storage only; device backup scope set by `data_extraction_rules.xml` / `backup_rules.xml`; export goes only to a file the user picks (SAF) after a warning. |
| Account deletion | Required only when users can create accounts. | N/A | No accounts. The local profile is on-device data; Profile › Delete all data erases everything. Answer "No" to account creation. |
| Content rating | The IARC questionnaire MUST be completed. | Console | Utility/productivity app: no violence, sexual content, language, drugs, gambling, user interaction, data sharing, location sharing or purchases. Expected result: Everyone / PEGI 3. |
| Target audience | Apps not designed for children MUST NOT target them. | Done / Console | Terms set a minimum age of 13; select 13+ age groups only, so the Families policy does not apply. |
| Health | Health features MUST be declared; wellness content MUST NOT be presented as medical advice. | Done / Console | Health notice in Profile › About; wellness templates show "General wellness ideas, not medical advice." Complete the Health apps declaration as general wellness (habit tracking with fitness, sleep and meditation habits), with no medical function and no Health Connect. |
| Intellectual property | Third-party material MUST carry its licence. | Done | Profile › About › Open-source licenses lists the Apache-2.0 libraries and the full Lucide ISC notice (icons use Lucide geometry). Confirm the VAJRAX name and icon are yours to use. |
| Deceptive behaviour | Content MUST NOT misrepresent its source. | Done | Bundled templates no longer credit invented "@sarah" / "@mike" authors; the section is "Featured templates" (library version 4 reseeds existing installs). |
| Device and network abuse | Background work MUST be proportionate. | Done | Inexact `setAndAllowWhileIdle` reminders, rescheduled only on data change, boot, package update, time or time-zone change. No services. |
| Metadata | Store listing MUST describe the real app. | Console | Screenshots from this build only; don't mention sync, AI or community features. |
| Developer account | Identity and contact MUST be verified and current. | Console | The in-app policy points users to the Play listing's developer contact, so that contact must be monitored. |

## 2. Not applicable to this release

| Area | Why |
|---|---|
| Ads policy | No ads SDK, no ads. Declare "No ads". |
| Payments, subscriptions, taxes, currency | Free app with no purchases, so no Play Billing. |
| Financial services | "Finance" templates are saving habits, not a financial service. |
| User-generated content | Nothing is posted or shared; custom templates stay on the device. |
| AI-generated content | No generative features in the shipped screens. |
| Analytics and consent | No analytics or crash SDK, so there is no consent screen. Adding one later requires opt-in consent plus Data safety and policy updates. |
| Network handling | Offline app with nothing to retry or time out. |
| Fraud and abuse protection | No backend, accounts or sign-ups to protect. |
| Regional features and restrictions | Same features everywhere; nothing is geo-restricted. Release in all countries. |

## 3. Global quality (done in the app)

| Area | Rule | Status | Evidence |
|---|---|---|---|
| RTL | Layout and directional icons MUST mirror in right-to-left locales. | Done | `supportsRtl="true"`; Compose start/end padding; arrow and chevron icons use `autoMirror`. |
| 12/24-hour time | Times MUST follow the device setting. | Done | `TimeFormat.use24Hour` is set at start-up, on resume and on `TIME_SET`; used by Home, Calendar, notifications, widgets and the time picker. |
| Time zones and DST | Reminders MUST fire at local wall-clock time after zone or clock changes. | Done | `TIMEZONE_CHANGED` / `TIME_SET` / boot / update → materialize + reschedule; times are stored as local `HH:mm` and converted per day. |
| Notifications | Ask at opt-in; give a way back after denial. | Done | Permission asked when reminders are turned on; after a denial the snackbar opens system notification settings; three lock-screen privacy levels. |
| Large screens | Apps targeting API 36 cannot lock orientation on screens ≥ 600 dp. | Done | Content is capped at 640 dp and centred; phones stay portrait. |
| Backup and restore | Data SHOULD move to a new phone. | Done | Database and preferences are included in cloud backup and device transfer; reminders are rescheduled on the first launch after restore. |
| Accessibility | 48 dp targets, labels on icon buttons, headings, contrast. | Done / Open | Components use 48 dp minimum targets and content descriptions; legal pages mark headings. Still to check on a device: TalkBack order and 200 % font scale. |

## 4. Open before release

| # | Item | Action |
|---|---|---|
| 1 | Release toolchain | AGP 8.5.2's bundled R8 predates Kotlin 2.4. `assembleRelease` succeeds, with 269 "parsing kotlin metadata" warnings. Upgrade AGP (with its matching R8), rebuild, then smoke-test the minified APK: onboarding, check-in, export, reminders, widgets. |
| 2 | Signing and bundle | Create the upload key, enrol in Play App Signing, ship `bundleRelease` (AAB), and increase `versionCode` on every upload. Upload `mapping.txt` so crash reports can be read. |
| 3 | Device QA matrix | Android 8 (minSdk) and Android 16; a 2 GB RAM phone; a tablet or foldable; dark mode; font scale 200 %; an RTL locale (Arabic); 24-hour time; time-zone change and a DST boundary; notification permission denied, then granted. |
| 4 | Translations | English only. Translation is not required for release. Localising later means moving UI strings to resources; dates currently use English names to match the UI, and weeks start on Monday (ISO) for all users. |
| 5 | Testing track | Personal developer accounts created after 13 Nov 2023 must run a closed test with at least 12 opted-in testers for 14 days before production access. |
| 6 | Rollout | Internal → closed → production staged rollout (for example 5 % → 20 % → 50 % → 100 %). Watch Android vitals (crash and ANR rates by device and country) at each step; halt the rollout to roll back. |
| 7 | Dormant online code | Supabase and AI clients are compiled in but never called, and can't reach the network. Before sync ships: move the project key out of source, confirm row-level security, and add the consent and Data safety changes. |
