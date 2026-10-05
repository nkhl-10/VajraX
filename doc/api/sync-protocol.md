# VAJRAX Accounts and Sync Protocol

Version 1.0 · 5 October 2026 · API `v1` · Machine-readable companion: [`openapi.yaml`](openapi.yaml)

**Contents:** 1 Revision history · 2 Abbreviations · 3 Conventions · 4 Errors · 5 Sessions · 6 Sync · 7 Payloads · 8 Template library · 9 Service endpoints · 10 Limits and timers · 11 Conformance tests

MUST, MUST NOT, SHOULD and MAY are used as in RFC 2119. Rules carry ids (`R-…`) that the conformance tests in section 11 refer to.

## 1. Revision history

| Version | Date | Change |
|---|---|---|
| 1.0 | 2026-10-05 | First version: accounts, sessions, offline-first sync, template library. |

## 2. Abbreviations

| Term | Meaning | Term | Meaning |
|---|---|---|---|
| API | Application programming interface | LWW | Last writer wins |
| ETag | HTTP entity tag (cache validator) | ms | Milliseconds |
| HTTP(S) | Hypertext Transfer Protocol (Secure) | RFC | Request for Comments (IETF standard) |
| ISO 8601 | Date and time text format | URL | Uniform resource locator |
| JSON | JavaScript Object Notation | UTC | Coordinated Universal Time |
| JWT | JSON Web Token | UTF-8 | Unicode text encoding |
| KiB, MiB | 1,024 and 1,048,576 bytes | UUID | Universally unique identifier |

## 3. Conventions

| Item | Value |
|---|---|
| Base URL | The service's origin, e.g. `https://vajrax.example`. All paths below are relative to it. |
| Body encoding | JSON, UTF-8, `Content-Type: application/json` |
| Dates | `YYYY-MM-DD` (local calendar date of the user) |
| Times of day | `HH:mm`, 24-hour |
| Instants | ISO 8601 with offset, e.g. `2026-10-05T06:01:47Z` |
| Change times (`updatedAt`) | Integer, ms since 1970-01-01T00:00:00Z |
| Account id | UUID text |
| Record id | 1–128 characters from `A–Z a–z 0–9 _ . : \| -` |

- **R-HTTP-1** Clients MUST use HTTPS. The server sends `Strict-Transport-Security`.
- **R-HTTP-2** Requests with a body MUST be JSON with `Content-Type: application/json`.
- **R-HTTP-3** Both sides MUST ignore unknown fields. Fields with a default MAY be omitted; absent optional fields are omitted rather than sent as `null`.
- **R-HTTP-4** Calls marked *Auth: access token* MUST send `Authorization: Bearer <accessToken>`.
- **R-HTTP-5** A browser client MUST send `X-VajraX-Client: web` on every `/v1/auth/*` call and MUST let the browser send cookies (same origin). Other clients MUST NOT send this header.
- **R-HTTP-6** A client MAY send `X-Request-Id` (8–128 characters). Every response carries `X-Request-Id` (the client's value or a new one). Support requests SHOULD quote it.
- **R-HTTP-7** Every `4xx`/`5xx` response under `/v1/` has a Problem body (section 4). Clients MUST decide on `code`, never on `title` or `detail`.

## 4. Errors

**Problem body**

| Field | Type | Meaning |
|---|---|---|
| `type` | string | Always `about:blank` |
| `title` | string | One sentence a user can read |
| `status` | integer | Same as the HTTP status |
| `code` | string | Stable machine code (table below) |
| `detail` | string, optional | More text for the user |
| `errors` | object, optional | Field name → message, for `validation_failed` |
| `requestId` | string, optional | Same as the `X-Request-Id` header |

```json
{ "type": "about:blank", "title": "Some changes couldn't be saved.", "status": 422,
  "code": "validation_failed", "errors": { "changes[3].id": "Invalid record id." },
  "requestId": "6c1f0e0e-2f7d-4b8e-9d1c-0d6c9a1d2f11" }
```

| `code` | Status | Sent when | Client action |
|---|---|---|---|
| `validation_failed` | 400, 422 | Body unreadable (400) or fields invalid (422) | Show `errors` next to the fields |
| `invalid_credentials` | 401, 403 | Wrong email or password (401, sign in); wrong current password (403, change password, delete account) | Ask again; don't sign out |
| `email_taken` | 409 | Register with an email that has an account | Offer sign in |
| `unauthorized` | 401 | Access token missing, invalid, expired or ended | Refresh once (5.10), then retry |
| `refresh_invalid` | 401 | Refresh token unknown, expired, revoked or reused | Signed out: clear the session |
| `reset_token_invalid` | 400 | Reset token unknown, used or older than 30 min | Ask for a new link |
| `forbidden` | 403 | Admin call by a non-admin | Stop |
| `not_found` | 404 | Unknown path | Stop |
| `payload_too_large` | 413 | Push over 500 changes or 1 MiB | Send smaller batches |
| `temporarily_locked` | 429 | Account locked after 5 failed sign-ins | Show `title` (contains the wait) |
| `rate_limited` | 429 | Too many calls from one address | Wait, then retry with back-off |
| `sync_cursor_expired` | 410 | Pull `since` older than the purged deletion markers | Pull from 0 (6.6) |
| `server_error` | 500 | Unexpected failure | Retry later with back-off |

- **R-ERR-1** A client MUST treat an unknown `code` by its HTTP status class.
- **R-ERR-2** `weak_password` and `token_expired` are reserved; v1 servers report weak passwords as `validation_failed` and expired access tokens as `unauthorized`.

## 5. Sessions

A session is one access token and one refresh token.

| Token | Form | Lifetime | Kept by the client |
|---|---|---|---|
| Access token | JWT, opaque to clients | 15 min (`accessExpiresIn` = 900) | Memory only |
| Refresh token | 43-character random text | 60 days (`refreshExpiresIn` = 5,184,000), replaced on every refresh | Mobile: encrypted device storage. Browser: an `HttpOnly` cookie the page can't read. |

- **R-SES-1** Clients MUST treat the access token as opaque.
- **R-SES-2** A mobile client MUST store the refresh token encrypted and MUST exclude it from device backups.
- **R-SES-3** A browser client never sees the refresh token: the server sets cookie `vx_refresh` with `HttpOnly; SameSite=Strict; Path=/v1/auth` (`Secure` on HTTPS), and the response's `refreshToken` field is absent.
- **R-SES-4** Every successful refresh returns a new refresh token; the old one stops working. A client MUST replace the stored token atomically.
- **R-SES-5** Presenting a replaced refresh token more than 30 s after its replacement ends the whole session family (every token descended from that sign-in). Within 30 s the duplicate only gets `refresh_invalid`.
- **R-SES-6** A password change, a password reset and "sign out everywhere" end all access tokens of the account at once.

Every session-creating call answers with **AuthResponse**:

| Field | Type | Meaning |
|---|---|---|
| `user` | User | Account (below) |
| `accessToken` | string | For `Authorization: Bearer` |
| `accessExpiresIn` | integer | Seconds until the access token expires |
| `refreshToken` | string, optional | Absent for browser clients |
| `refreshExpiresIn` | integer | Seconds until the refresh token expires |

**User:** `id` (UUID), `email`, `displayName` (0–40 characters), `emailVerified` (boolean, not enforced in v1), `createdAt` (instant).

### 5.1 Register

| Method, path | Auth | Rate limit (per address) | Success |
|---|---|---|---|
| `POST /v1/auth/register` | none | 5 per hour | `201` AuthResponse |

| Field | Type | Rule |
|---|---|---|
| `email` | string | Required, valid address, at most 254 characters |
| `password` | string | 10–128 characters, at least 4 different characters, not containing the email's name part (4+ characters), not a common password, not a straight sequence |
| `displayName` | string | 0–40 characters, default empty |
| `deviceName` | string, optional | Shown in the account's session list; default the `User-Agent` |

- **R-REG-1** Emails compare case-insensitively; one account per email (`409 email_taken`).
- **R-REG-2** Clients SHOULD check length locally and leave the other password rules to the server's `errors.password`.

```json
POST /v1/auth/register
{ "email": "sam@example.com", "password": "quiet morning tea", "displayName": "Sam", "deviceName": "Pixel 8" }
```

### 5.2 Sign in

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `POST /v1/auth/login` | none | 10 per minute | `200` AuthResponse |

Body: `email`, `password`, optional `deviceName`.

- **R-LOG-1** Wrong password and unknown email give the same answer (`401 invalid_credentials`) in the same time.
- **R-LOG-2** After 5 failed sign-ins in a row the account is locked for 1 min, then 2, 4, 8 and at most 15 min per further failure (`429 temporarily_locked`). A successful sign-in resets the count.

### 5.3 Refresh

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `POST /v1/auth/refresh` | refresh token | 10 per minute | `200` AuthResponse |

Body: `{ "refreshToken": "<token>" }` (mobile) or `{}` with the cookie (browser, R-HTTP-5).

### 5.4 Sign out and sign out everywhere

| Method, path | Auth | Body | Success |
|---|---|---|---|
| `POST /v1/auth/logout` | none | `{ "refreshToken": "<token>" }` (mobile) or `{}` + cookie (browser) | `204`; the refresh token ends; browser cookie cleared |
| `POST /v1/auth/logout-all` | access token | none | `204`; every session of the account ends |

- **R-OUT-1** A client MUST delete its stored tokens even when the sign-out call fails (offline).

### 5.5 Profile

| Method, path | Auth | Body | Success |
|---|---|---|---|
| `GET /v1/me` | access token | none | `200` User |
| `PATCH /v1/me` | access token | `{ "displayName": "Sam" }` (0–40 characters) | `200` User |

### 5.6 Change password

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `POST /v1/auth/password/change` | access token | 10 per minute | `200` AuthResponse (a new session for this device) |

Body: `currentPassword`, `newPassword` (rules of 5.1). Wrong current password: `403 invalid_credentials`.

- **R-PWC-1** All other sessions end (R-SES-6). The client MUST replace its tokens with the returned ones.

### 5.7 Forgot and reset password

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `POST /v1/auth/password/forgot` | none | 3 per hour | `202`, always |
| `POST /v1/auth/password/reset` | reset token | 3 per hour | `204` |

Forgot body: `{ "email": "…" }`. Reset body: `{ "token": "…", "newPassword": "…" }`.

- **R-RST-1** `forgot` answers `202` whether or not the email has an account.
- **R-RST-2** The email links to `<base>/reset-password#token=<token>`. The token is in the URL fragment so no server or proxy log records it; a page that reads it MUST remove it from the address bar.
- **R-RST-3** A reset token works once, for 30 min. Asking again ends earlier tokens of the account.
- **R-RST-4** A reset ends every session of the account (R-SES-6).

### 5.8 Delete account

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `DELETE /v1/account` | access token | 10 per minute | `204` |

Body: `{ "password": "…" }`. Wrong password: `403 invalid_credentials`.

- **R-DEL-1** One step removes the account, all its sessions, reset tokens and synced records. The security log keeps its entries for the account (account id, event, time, salted address hash; no email, no data) for 400 days.
- **R-DEL-2** After `204` a client MUST end the session and turn sync off. Data on the device stays unless the user removes it.

### 5.9 Procedure: sign in and keep the session

1. Call register (5.1) or sign in (5.2).
2. Keep `accessToken` in memory; store `refreshToken` (R-SES-2) or rely on the cookie (browser).
3. Call API endpoints with the access token.
4. On `401 unauthorized`, refresh (5.10) and repeat the call once with the new access token.
5. On `401 refresh_invalid`, the session is over: delete tokens and show signed-out.
6. On app start (browser: page load) with a stored session, refresh first to get an access token.

### 5.10 Procedure: refresh

1. If a refresh is already running, wait for its result instead of starting another (one at a time per client).
2. If the failed call used an access token that has since been replaced, retry with the new one; don't refresh again.
3. Send refresh (5.3).
4. On success, store the new refresh token (R-SES-4) and use the new access token.
5. On `refresh_invalid`, follow 5.9 step 5. On network failure, keep the session and try later.

## 6. Sync

The server stores the newest version of each record of an account and hands out changes in the order it accepted them. It never reads payloads. Each device keeps its own copy and works fully offline.

### 6.1 Records

| Field | Type | Meaning |
|---|---|---|
| `entity` | string | Kind: `tracker`, `habit`, `occurrence`, `goal`, `reflection`, `template`, `setting` |
| `id` | string | Record id, unique per account and entity (section 3) |
| `updatedAt` | integer | Change time on the device that made it (ms) |
| `deleted` | boolean | `true` = deletion marker; `payload` absent |
| `schema` | integer | Payload format, `1` in this version |
| `payload` | object | Record content (section 7); at most 32 KiB as UTF-8 JSON |
| `deviceId` | string | Device that made the change (server-filled on records it sends) |
| `version` | integer | Position in the account's change order (server-filled) |

- **R-SYNC-1** A device id MUST be random, at most 64 characters from `A–Z a–z 0–9 _ . : | -`, stable while sync is on, and new each time sync is turned on.
- **R-SYNC-2 (LWW)** A change replaces the stored record only when `(updatedAt, deviceId)` is greater than the stored pair: newer time wins; on equal times the greater device id wins, comparing bytes (so `dev_a` > `dev_B`).
- **R-SYNC-3** The server lowers an `updatedAt` more than 5 min ahead of its own clock to its clock + 5 min.
- **R-SYNC-4** Deletions travel as markers. The server keeps markers for 180 days, then purges them.
- **R-SYNC-5** `version` grows with every accepted change of the account; pushes of one account are serialised so a pull never skips a version.
- **R-SYNC-6** A device MUST NOT send a record whose `schema` it doesn't know and MUST NOT apply a received record with a higher `schema` than it supports (it skips it).

### 6.2 Push

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `POST /v1/sync/push` | access token | 120 per minute (push + pull) | `200` PushResponse |

Request: `deviceId`, `changes` (0–500 changes: `entity`, `id`, `updatedAt`, `deleted`, `schema`, `payload`). Whole body at most 1 MiB.

PushResponse:

| Field | Type | Meaning |
|---|---|---|
| `accepted` | list of `{entity, id, version}` | Stored |
| `rejected` | list of records | Lost to a newer stored record; the stored record is returned |
| `serverTime` | integer | Server clock (ms) |

- **R-PUSH-1** The whole push is validated first; any invalid change fails the call with `422 validation_failed` (`errors` keys `deviceId`, `changes[i].entity|id|updatedAt|payload`) and nothing is stored.
- **R-PUSH-2** A queued change that appears in neither `accepted` nor `rejected` MUST be removed from the queue, not resent.

```json
POST /v1/sync/push
{ "deviceId": "dev_4f1c2a9e0b7d4c6e8a1f3b5",
  "changes": [
    { "entity": "occurrence", "id": "occ_hab_water_2026-10-05", "updatedAt": 1791180000000, "schema": 1,
      "payload": { "habitId": "hab_water", "date": "2026-10-05", "status": "COMPLETE", "completedAt": "2026-10-05T08:12:00+05:30" } },
    { "entity": "goal", "id": "goal_7c2e", "updatedAt": 1791180004000, "deleted": true } ] }

200
{ "accepted": [ { "entity": "occurrence", "id": "occ_hab_water_2026-10-05", "version": 1042 } ],
  "rejected": [ { "entity": "goal", "id": "goal_7c2e", "updatedAt": 1791180009000, "deleted": false, "schema": 1,
                  "payload": { "title": "Read 12 books", "targetValue": 12, "startDate": "2026-10-01", "createdAt": "2026-10-01T09:00:00Z" },
                  "deviceId": "dev_91aa0c5e2b3d4f6a7b8c9d0", "version": 1039 } ],
  "serverTime": 1791180010123 }
```

### 6.3 Pull

| Method, path | Auth | Rate limit | Success |
|---|---|---|---|
| `GET /v1/sync/pull?since=<version>&limit=<n>` | access token | shared with push | `200` PullResponse |

`since` default 0 (everything); `limit` 1–500, default 500.

PullResponse: `changes` (records with `version > since`, ascending), `next` (pass as `since` next time; equals `since` when empty), `hasMore` (more pages follow).

- **R-PULL-1** If `since` is between 1 and the highest purged marker version, the server answers `410 sync_cursor_expired`.
- **R-PULL-2** Records include those this device pushed itself; the device recognises them (6.5 step 2).

### 6.4 Procedure: one sync round

1. If sync is off or no session exists, stop.
2. **Push.** Take up to 200 queued changes (oldest first). For each, read the current local row: present → `payload`, missing → `deleted: true`. Send push (6.2).
3. For each `accepted`: remember `(updatedAt, version)` for the record and remove it from the queue **only if it was not changed again since it was read**.
4. For each `rejected`: apply the returned record (6.5 steps 3–5) and remove the queued change under the same condition as step 3.
5. Remove queued changes that appear in neither list (R-PUSH-2). Repeat from step 2 until the queue is empty (at most 50 rounds).
6. **Pull.** Call pull with the stored cursor. Apply every record (6.5), then store `next` as the cursor **in the same transaction**. Repeat while `hasMore`.
7. On `410 sync_cursor_expired`, pull from 0 and continue at step 6. Unsent local changes still win where they are newer (6.5 step 2).
8. If anything was applied in steps 4 or 6, enforce one active routine (6.7) and refresh the screens.
9. Record the time of the round as "last synced".

- **R-RND-1** Rounds on one device MUST NOT overlap.
- **R-RND-2** Writes made while applying received records MUST NOT be queued for upload.
- **R-RND-3** A failure in one received record (unknown entity, bad payload) MUST skip that record only.

### 6.5 Procedure: apply a received record

1. If the device holds an unsent change for the same `(entity, id)` whose `(changedAt, deviceId)` is greater than the record's `(updatedAt, deviceId)` (R-SYNC-2), keep the local change; stop.
2. If the record has this device's id and the `updatedAt` the device already recorded for it, it is the device's own upload: store its `version`; stop.
3. If `schema` is higher than supported, skip it (R-SYNC-6).
4. `deleted: true` → delete the local row. Otherwise insert or replace the local row from `payload`.
5. Remember `(updatedAt, version)`; remove any older queued change for the record.

### 6.6 What a device sends

| Entity | Local data | Queued when | Id |
|---|---|---|---|
| `tracker` | Routine the user follows | Created, changed, deleted | Random |
| `habit` | Habit of a routine | Created, changed, deleted | Random |
| `occurrence` | One habit on one day | Status other than `PENDING` is written; a non-`PENDING` row is deleted or returned to `PENDING` | `occ_<habitId>_<date>` |
| `goal` | Goal with its linked habit ids | Goal or its links change | Random |
| `reflection` | Weekly or monthly reflection | Created, changed, deleted | Deterministic per period |
| `template` | Template the user made, with its habits | Created, changed, deleted | Starts with `custom_` |
| `setting` | Per-account preference | `onboarding_goals`, `wake_time`, `morning_minutes` change | The setting key |

- **R-CAP-1** Days with status `PENDING` MUST NOT be sent: every device plans them itself, and a planned row must never overwrite a check-in.
- **R-CAP-2** Built-in library templates MUST NOT be sent (only ids starting `custom_`).
- **R-CAP-3** Device-only settings (theme, reminders, widgets, notification text) MUST NOT be sent.
- **R-CAP-4** Repeated edits of one record before a push MUST collapse into one queued change carrying the latest change time.

### 6.7 One active routine

Two offline devices can each start a routine. After any round that applied remote records:

1. List routines with status `ACTIVE`. If there is at most one, stop.
2. Keep the one with the greatest synced `updatedAt`; on a tie, the greatest id.
3. End every other one as a switched routine: delete its future `PENDING` days, archive its habits and the routine with today's date. These changes sync like any edit.

- **R-ONE-1** Every device MUST apply the same choice (step 2 uses only synced values), so all devices converge.

### 6.8 Procedure: turn sync on, sign out, delete

**Turn on (first sign-in on a device):**

1. Pull with `since=0&limit=1` to learn whether the account has records.
2. If both the device and the account have records, ask the user: **merge** (both sets kept; LWW decides per record) or **use the account's data** (this device's copy is replaced).
3. Clear the queue and the remembered versions, set the cursor to 0 and create a new device id (R-SYNC-1).
4. Merge (also when only one side has records): queue every syncable row. Use the account's data: delete this device's syncable rows without queuing deletions.
5. Turn capture on and run a round (6.4).

**Sign out:** turn capture off first, clear queue, record versions and cursor, then delete tokens. Device data stays unless the user chooses to remove it (removal is not queued).

**"Delete all data" while signed in:** clear this device only (not queued) and sign out. Cloud data is removed only by deleting the account (5.8).

### 6.9 When to sync

| Trigger | Delay |
|---|---|
| App start or page load with a session | Immediately |
| A syncable local change | 5 s after the last change (debounced) |
| User taps "Sync now" | Immediately |
| Mobile background job | Every 60 min while a network is available |

## 7. Payloads

All fields use the defaults shown when absent. Enumerations are upper-case text; receivers MUST keep unknown values unchanged.

**`tracker`**

| Field | Type | Default |
|---|---|---|
| `templateId` | string | required |
| `name` | string | absent |
| `startDate` | date | absent |
| `totalDays`, `currentDay`, `progressPercent` | integer | 30, 1, 0 |
| `isActive` | boolean | true |
| `status` | `ACTIVE`, `ARCHIVED` | `ACTIVE` |
| `endedAt` | date | absent |

**`habit`**

| Field | Type | Default |
|---|---|---|
| `trackerId` | string | absent |
| `title` | string | required |
| `targetDurationMinutes`, `minimumDurationMinutes` | integer | required |
| `preferredTime`, `reminderTime` | time | absent |
| `trackingMode` | `MANUAL`, `PASSIVE`, `TIMER`, `HYBRID` | `MANUAL` |
| `isActive`, `reminderEnabled` | boolean | true, false |
| `category`, `icon`, `color` | string | `General`, `check`, `indigo` |
| `habitType` | `BOOLEAN`, `COUNT`, `DURATION`, `VALUE` | `BOOLEAN` |
| `targetValue` | number | 1.0 |
| `unit` | string | absent |
| `scheduleType` | `DAILY`, `WEEKDAYS`, `WEEKLY_TARGET`, `INTERVAL` | `DAILY` |
| `scheduleDays` | ISO weekdays, comma-separated (1 = Monday) | `1,2,3,4,5,6,7` |
| `weeklyTarget`, `intervalDays`, `sortOrder` | integer | 0, 1, 0 |
| `startDate`, `archivedAt` | date | absent |
| `createdAt` | instant | absent |

**`occurrence`** (id `occ_<habitId>_<date>`)

| Field | Type | Default |
|---|---|---|
| `habitId` | string | required |
| `date` | date | required |
| `scheduledTime` | time | absent |
| `status` | `ONGOING`, `COMPLETE`, `MINIMUM`, `SKIPPED`, `MISSED`, `SNOOZED` (never `PENDING`, R-CAP-1) | required |
| `completedAt` | instant | absent |
| `durationMinutes` | integer | absent |
| `value` | number | absent |
| `note`, `skipReason` | string | absent |

**`goal`**

| Field | Type | Default |
|---|---|---|
| `title` | string | required |
| `description` | string | absent |
| `targetType` | `COMPLETIONS`, `RATE` | `COMPLETIONS` |
| `targetValue` | number | required |
| `startDate` | date | required |
| `endDate` | date | absent |
| `status` | string | `ACTIVE` |
| `createdAt` | instant | required |
| `habitIds` | list of habit ids | empty |

**`reflection`**

| Field | Type | Default |
|---|---|---|
| `periodType` | `WEEK`, `MONTH` | required |
| `periodStart` | date | required |
| `achievements`, `obstacles`, `nextActions` | string | empty |
| `createdAt`, `updatedAt` | instant | required |

**`template`** (id starts with `custom_`)

| Field | Type | Default |
|---|---|---|
| `title`, `description` | string | required |
| `category`, `frequency` | string | `General`, `Daily` |
| `author`, `recommendedFor` | string | absent |
| `durationDays` | integer | 30 |
| `isDraft` | boolean | false |
| `updatedAt` | instant | absent |
| `habits` | list of template habits | empty |

**Template habit**

| Field | Type | Default |
|---|---|---|
| `name` | string | required |
| `startTime` | time | required |
| `durationMinutes` | integer | required |
| `trackingMode`, `habitType`, `scheduleType` | as in `habit` | `MANUAL`, `BOOLEAN`, `DAILY` |
| `target`, `repeatDays` | string | empty |
| `reminderEnabled` | boolean | true |
| `sortOrder`, `weeklyTarget`, `intervalDays` | integer | 0, 0, 1 |
| `category`, `icon`, `color`, `unit` | string | absent |
| `targetValue` | number | 1.0 |

**`setting`** (id = key): `{ "value": "<text>" }`. Keys: `onboarding_goals`, `wake_time` (`HH:mm`), `morning_minutes` (integer as text).

## 8. Template library

| Method, path | Auth | Success |
|---|---|---|
| `GET /v1/templates?since=<version>` | none | `200` TemplateLibrary, or `304` when unchanged |
| `PUT /v1/admin/templates/{id}?order=<n>` | access token, admin | `204` (draft change) |
| `DELETE /v1/admin/templates/{id}` | access token, admin | `204` (draft change) |
| `POST /v1/admin/templates/publish` | access token, admin | `200 { "version": "<new version>" }` |

TemplateLibrary: `version` (text), `templates` (list: `id`, `title`, `description`, `category`, `frequency` default `Daily`, `author`, `isCommunity` default false, `durationDays` default 30, `recommendedFor`, `habits` as template habits).

- **R-LIB-1** Responses carry `ETag: "lib-<version>"` and `Cache-Control: public, max-age=3600`. A client SHOULD send `If-None-Match` or `since=<version it has>`; both give `304` when nothing changed.
- **R-LIB-2** A client MUST ship a built-in copy and keep working with it when the call fails.
- **R-LIB-3** Admin changes are invisible until publish, which gives a new version.
- **R-LIB-4** A non-admin gets `403 forbidden` on admin calls. The `PUT` body is one library template whose `id` matches the path.

## 9. Service endpoints

| Method, path | Answer |
|---|---|
| `GET /v1/version` | `200 { "version", "gitSha", "minAppVersion", "serverTime" }` |
| `GET /healthz` | `200 ok` while the process runs |
| `GET /readyz` | `200 ready` when the database answers within 2 s, else `503` |

- **R-VER-1** A client older than `minAppVersion` (dot-separated numbers) SHOULD stop syncing and ask the user to update.

Known limitation: the 1.0 Android and browser clients don't read `minAppVersion` yet.

## 10. Limits and timers

| Item | Value | Rule |
|---|---|---|
| Email length | ≤ 254 | 5.1 |
| Password length | 10–128 | 5.1 |
| Display name | ≤ 40 | 5.1 |
| Record id | ≤ 128 | 3 |
| Device id | ≤ 64 | R-SYNC-1 |
| Changes per push | ≤ 500 (reference client sends 200) | 6.2 |
| Push body | ≤ 1 MiB | 6.2 |
| Payload | ≤ 32 KiB | 6.1 |
| Records per pull | 1–500 | 6.3 |
| Access token | 15 min | 5 |
| Refresh token | 60 days, rotated on use | 5 |
| Refresh reuse grace | 30 s | R-SES-5 |
| Reset token | 30 min, single use | R-RST-3 |
| Account lock | after 5 failures: 1, 2, 4, 8, 15 min | R-LOG-2 |
| Clock lead accepted | 5 min | R-SYNC-3 |
| Deletion markers kept | 180 days | R-SYNC-4 |
| Sync debounce | 5 s | 6.9 |
| Background sync | 60 min | 6.9 |
| Rate limits per address | register 5/h · sign-in, refresh, sign-out, password change, delete 10/min · forgot + reset 3/h · push + pull 120/min | 5, 6 |
| Library cache | 3,600 s | R-LIB-1 |
| Readiness timeout | 2 s | 9 |

## 11. Conformance tests

| Id | Steps | Expected | Rules |
|---|---|---|---|
| CT-1 | Register, then register again with the same email in other letter case | `201`, then `409 email_taken` | R-REG-1 |
| CT-2 | Sign in with a wrong password and with an unknown email | Both `401 invalid_credentials` | R-LOG-1 |
| CT-3 | Fail sign-in 5 times, then use the right password | `429 temporarily_locked` | R-LOG-2 |
| CT-4 | Refresh with token A (gets B); refresh with A after 31 s; refresh with B | `200`; `401 refresh_invalid`; `401 refresh_invalid` (family ended) | R-SES-4, R-SES-5 |
| CT-5 | Browser client signs in with `X-VajraX-Client: web` | No `refreshToken` in the body; `vx_refresh` cookie `HttpOnly`, `SameSite=Strict`, `Path=/v1/auth` | R-SES-3 |
| CT-6 | Sign out everywhere, then call `GET /v1/me` with the old access token | `401 unauthorized` | R-SES-6 |
| CT-7 | Forgot with an unknown email and with a known one | Both `202`; only the known one gets an email, its link uses `#token=` | R-RST-1, R-RST-2 |
| CT-8 | Reset twice with the same token | `204`, then `400 reset_token_invalid` | R-RST-3 |
| CT-9 | Device A pushes record X at t=100; device B pushes X at t=90 | B's change in `rejected` with A's record | R-SYNC-2 |
| CT-10 | Two pushes of X with the same `updatedAt` from `dev_a` and `dev_b` | `dev_b` wins | R-SYNC-2 |
| CT-11 | Push with `updatedAt` = now + 1 h, then pull | Stored `updatedAt` ≤ server time + 5 min | R-SYNC-3 |
| CT-12 | Push 501 changes | `413 payload_too_large` | 6.2 |
| CT-13 | Push one valid and one invalid change | `422`, nothing stored | R-PUSH-1 |
| CT-14 | Account A pushes; account B pulls from 0 | B receives none of A's records | 6 |
| CT-15 | Pull with `limit=2` over 5 records | Pages of 2, 2, 1; `hasMore` true, true, false; versions ascending | 6.3 |
| CT-16 | Delete X, age the marker past 180 days, run maintenance, pull with an old cursor | `410 sync_cursor_expired`; pull from 0 succeeds | R-SYNC-4, R-PULL-1 |
| CT-17 | Device plans today's habits (all `PENDING`) and syncs | No `occurrence` sent | R-CAP-1 |
| CT-18 | Two devices each start a routine offline, then both sync twice | Both show the same single active routine | R-ONE-1 |
| CT-19 | Delete the account, then sign in | `204`; then `401 invalid_credentials` | R-DEL-1 |
| CT-20 | `GET /v1/templates` with `If-None-Match` from the previous answer | `304` | R-LIB-1 |
