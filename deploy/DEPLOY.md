# Deploying VAJRAX on Google Cloud

One Cloud Run service serves everything on one address:

| Path | What |
|---|---|
| `/` | Website: home, privacy policy, terms, delete account, reset password, support |
| `/app/` | Web app (the habit app in the browser; needs an account) |
| `/v1/*` | API for the Android app and the web app (accounts, sync, template library) |
| `/healthz`, `/readyz` | Liveness, readiness (database reachable) |
| `/internal/jobs/maintenance` | Daily housekeeping, called by Cloud Scheduler with a token |
| `/internal/metrics` | Prometheus metrics, only with `METRICS_TOKEN` |

Data lives in Cloud SQL for PostgreSQL. Secrets live in Secret Manager. Region: `asia-south1` (Mumbai).

```
Android app ─┐
Browser ─────┼─► Cloud Run "vajrax" (container: server/Dockerfile) ─► Cloud SQL "vajrax-db" (PostgreSQL 17)
Scheduler ───┘        ▲ secrets: Secret Manager                         backups 7 days + point-in-time
```

Files in `deploy/`: `gcp-setup.sh` (one-time setup), `cloudbuild.yaml` (test, build, deploy), `.env.example` (every setting).

## 1. Before you start

- A Google Cloud project with billing on, and the `gcloud` CLI signed in as its owner (`gcloud auth login`). Cloud Shell works and already has everything.
- `openssl` and `curl` (both in Cloud Shell).
- This repository checked out at the commit you want to deploy.

Region: to use another region, set `REGION=…` for the setup script and `_REGION=…` for the build, and change "India (Mumbai region)" in `doc/legal/privacy-policy.md` (then rebuild: the in-app text and the website are generated from it).

## 2. One-time setup

```bash
PROJECT_ID=your-project bash deploy/gcp-setup.sh init
```

This enables the APIs, creates the image repository, the database (`vajrax-db`, PostgreSQL 17, `db-g1-small`, daily backups kept 7 days, point-in-time recovery 7 days, deletion protection), the database `vajrax` and user `vajrax`, five secrets with random values, and two service accounts:

| Account | Can |
|---|---|
| `vajrax-run` (the server) | connect to Cloud SQL, read its five secrets. Nothing else. |
| `vajrax-build` (Cloud Build) | push images, deploy the service as `vajrax-run`, write build logs, read uploaded source |

Creating the database takes about 10 minutes. If the command stops with "taking longer than expected", wait and run `init` again; finished steps are skipped.

Secrets (`gcloud secrets list`):

| Secret | Used as | Notes |
|---|---|---|
| `vajrax-jwt-secret` | `JWT_SECRET` | Signs sign-in tokens. A new version signs everyone out. |
| `vajrax-db-password` | `DB_PASSWORD` | Password of database user `vajrax`. |
| `vajrax-jobs-token` | `JOBS_TOKEN` | Cloud Scheduler sends it to run maintenance. |
| `vajrax-metrics-token` | `METRICS_TOKEN` | Bearer token for `/internal/metrics`. |
| `vajrax-smtp-password` | `SMTP_PASSWORD` | `not-set` until email is set up (section 5). |

## 3. First deploy

From the repository root:

```bash
PROJECT_ID=your-project
gcloud builds submit --project "$PROJECT_ID" --config deploy/cloudbuild.yaml \
  --service-account "projects/$PROJECT_ID/serviceAccounts/vajrax-build@$PROJECT_ID.iam.gserviceaccount.com" \
  --substitutions SHORT_SHA="$(git rev-parse --short HEAD)"
```

The build (about 10–20 minutes) runs the server tests, builds the image (server + website + web app), pushes it to Artifact Registry and deploys the service. Database tables are created on the first start. The last log line prints the address, `https://vajrax-<project number>.asia-south1.run.app`.

Then schedule the daily maintenance call (deletes old deletion markers, expired sessions and reset links, audit entries older than 400 days):

```bash
PROJECT_ID=your-project bash deploy/gcp-setup.sh scheduler
```

## 4. Check it

```bash
URL=$(gcloud run services describe vajrax --region asia-south1 --format 'value(status.url)')
curl -s $URL/healthz           # ok
curl -s $URL/readyz            # ready  (database reachable)
curl -s $URL/v1/version        # {"version":"1.0.0","gitSha":"…","minAppVersion":"1.0",…}
```

Open `$URL/` (website) and `$URL/app/` (web app): create an account, check a habit in, reload the page; the check-in is still there.

Let an account manage the public template library (optional):

```bash
REGION=asia-south1; IMAGE=$REGION-docker.pkg.dev/$PROJECT_ID/vajrax/server:latest
gcloud run jobs deploy vajrax-admin --image "$IMAGE" --region $REGION \
  --service-account vajrax-run@$PROJECT_ID.iam.gserviceaccount.com \
  --set-env-vars "APP_ENV=production,PUBLIC_BASE_URL=$URL,DB_INSTANCE=$PROJECT_ID:$REGION:vajrax-db,DB_URL=jdbc:postgresql:///vajrax,DB_USER=vajrax" \
  --set-secrets JWT_SECRET=vajrax-jwt-secret:latest,DB_PASSWORD=vajrax-db-password:latest \
  --args grant-admin,you@example.com
gcloud run jobs execute vajrax-admin --region $REGION --wait
```

## 5. Email (password reset)

Without email, "Forgot password" can't deliver its link (the request still answers normally, so nothing reveals who has an account). Use any SMTP provider (Google Workspace SMTP relay, Amazon SES, Brevo, Mailgun, …) with a sender on your domain and SPF/DKIM set up.

```bash
printf '%s' 'the-smtp-password' | gcloud secrets versions add vajrax-smtp-password --data-file=-
gcloud builds submit … --substitutions SHORT_SHA=$(git rev-parse --short HEAD),_SMTP_HOST=smtp.example.com,_SMTP_USER=apikey,_SMTP_FROM='VAJRAX <no-reply@your-domain>'
```

`_SMTP_PORT` defaults to 587 with STARTTLS. Test: "Forgot password?" on the app's sign-in screen, then follow the email link (it opens `/reset-password` on `PUBLIC_BASE_URL`).

## 6. Custom domain

Cloud Run domain mappings are not offered in every region; for `asia-south1` put a global external Application Load Balancer in front (serverless network endpoint group → the `vajrax` service, Google-managed certificate for your domain). Then:

1. Point the domain's DNS `A`/`AAAA` records at the load balancer address and wait for the certificate to become active.
2. Redeploy with the new address and the load balancer's proxy hop:
   `--substitutions …,_PUBLIC_BASE_URL=https://your-domain,_FORWARDED_SKIP_LAST=1`
   `PUBLIC_BASE_URL` goes into reset links and decides the cookie's `Secure` flag. `FORWARDED_SKIP_LAST=1` makes rate limits count per visitor instead of per load balancer.
3. Optional: `gcloud run services update vajrax --region asia-south1 --ingress internal-and-cloud-load-balancing` so the `run.app` address stops answering. The scheduler then has to call the domain:
   `SERVICE_URL=https://your-domain PROJECT_ID=your-project bash deploy/gcp-setup.sh scheduler`

## 7. Release checklist (Play Store, app, website)

- **Android app address.** In `local.properties` set `vajrax.api.url=https://your-domain` (or the `run.app` address) before `assembleRelease`; an empty value builds an offline-only app.
- **Play Console › App content › Data safety › Data deletion:** `https://your-domain/delete-account`.
- **Play Console › Privacy policy:** `https://your-domain/privacy-policy`. Terms: `https://your-domain/terms-of-use`.
- **Website placeholders** (search for `SET at release`):
  - `web/site/index.html` — "Get it for Android" → the Play listing URL.
  - `web/site/support.html` — a monitored support email (Play requires one).
- **Answers** in `doc/release/global-release.md` (Data safety rows) must match the deployed server.
- Forced update: raise `_MIN_APP_VERSION` when an old app version must stop syncing.

## 8. Updates and rollback

- Update: commit, then run the same `gcloud builds submit` command. Cloud Run switches traffic when the new revision is healthy; database migrations (`server/src/main/resources/db/migration`) run on start and only move forward.
- Automatic deploys: Cloud Build › Triggers › connect the repository, configuration `deploy/cloudbuild.yaml`, service account `vajrax-build`. Triggered builds fill `SHORT_SHA` themselves.
- Rollback:
  ```bash
  gcloud run revisions list --service vajrax --region asia-south1
  gcloud run services update-traffic vajrax --region asia-south1 --to-revisions vajrax-00012-abc=100
  ```
  A revision older than a migration still works only if the migration added things (new tables or columns) rather than changing them. Write migrations that way.

## 9. Backups and restore

- Daily backups (02:30 IST) kept 7 days, point-in-time recovery for the last 7 days. The privacy policy says backups expire within 7 days; keep these in step.
- Restore to a point in time as a new instance, check it, then switch `_SQL_INSTANCE`:
  ```bash
  gcloud sql instances clone vajrax-db vajrax-db-restore --point-in-time '2026-10-05T08:00:00Z'
  ```
- Account deletions are permanent in the live database at once; they leave the backups as those expire.

## 10. Logs and monitoring

- Logs: Cloud Run › vajrax › Logs, or Logs Explorer with `resource.type="cloud_run_revision" resource.labels.service_name="vajrax"`. Lines are JSON with `severity`. Passwords and tokens are never logged; in production neither is the content of emails.
- Request id: every response has `X-Request-Id`; errors carry it as `requestId` for support.
- Alerts worth adding: uptime check on `/readyz`, 5xx rate on the service, Cloud SQL CPU and storage.
- Metrics: `curl -H "Authorization: Bearer $(gcloud secrets versions access latest --secret vajrax-metrics-token)" $URL/internal/metrics`.

## 11. Cost (rough, check the pricing calculator)

| Item | Setting | Order of cost |
|---|---|---|
| Cloud SQL | `db-g1-small`, 10 GB SSD, backups | the main fixed cost, tens of USD a month |
| Cloud Run | scales to zero, 1 vCPU / 1 GiB | inside the free tier at low traffic |
| Cloud Build | `E2_HIGHCPU_8`, 10–20 min per deploy | cents per deploy |
| Artifact Registry | ~180 MB per image | add a cleanup policy to keep the last 10 |
| Secret Manager, Scheduler | 5 secrets, 1 job | within or near the free tier |

To keep one instance warm (no cold start of a few seconds): `_MIN_INSTANCES=1` (billed all the time). Memory can go down to 512Mi with `_MEMORY=512Mi` if the metrics show room.

## 12. Settings

Every setting with its default is in `deploy/.env.example`. `cloudbuild.yaml` sets the production ones; change them with `--substitutions`:

| Substitution | Default | Sets |
|---|---|---|
| `_REGION` | `asia-south1` | region of the service, image and database |
| `_SERVICE`, `_REPOSITORY`, `_SQL_INSTANCE` | `vajrax`, `vajrax`, `vajrax-db` | resource names |
| `_PUBLIC_BASE_URL` | the `run.app` address | `PUBLIC_BASE_URL` |
| `_SMTP_HOST`, `_SMTP_PORT`, `_SMTP_USER`, `_SMTP_FROM` | none, 587, none, `VAJRAX <no-reply@example.com>` | password-reset email |
| `_MIN_APP_VERSION` | `1.0` | forced app update |
| `_FORWARDED_SKIP_LAST` | `0` | `1` behind a load balancer |
| `_MIN_INSTANCES`, `_MAX_INSTANCES` | `0`, `8` | scaling (instances × 5 connections < database limit) |
| `_MEMORY` | `1Gi` | memory per instance |

## 13. Run the same image elsewhere

```bash
docker build -f server/Dockerfile -t vajrax-server .           # from the repository root
cp deploy/.env.example deploy/.env                             # fill DB_*, JWT_SECRET, PUBLIC_BASE_URL
docker run -p 8080:8080 --env-file deploy/.env vajrax-server
```

Without Docker, `./gradlew :server:buildImage` builds the same image with Jib into `server/build/jib-image.tar`, and `./gradlew :server:runDev` runs the server on an embedded PostgreSQL at `http://localhost:8080`.

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| Service does not start, log says `JWT_SECRET must be at least 32 bytes` | Secret missing or short; add a version: `openssl rand -hex 32 \| gcloud secrets versions add vajrax-jwt-secret --data-file=-` |
| `/readyz` answers 503 | Database not reachable: instance stopped, wrong `_SQL_INSTANCE`/`_REGION`, or `vajrax-run` lacks `roles/cloudsql.client` |
| `FATAL: remaining connection slots are reserved` | Too many instances for the database: lower `_MAX_INSTANCES` or use a larger tier |
| Build fails at `test` | Run `./gradlew :server:test` locally; the build deploys only green code |
| Everyone gets "Too many attempts" at once behind a load balancer | Set `_FORWARDED_SKIP_LAST=1` |
