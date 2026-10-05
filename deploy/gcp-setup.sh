#!/usr/bin/env bash
# One-time Google Cloud setup for VAJRAX: Cloud Run + Cloud SQL (PostgreSQL) + Secret Manager.
# Run it yourself with the gcloud CLI signed in as a project owner (see deploy/DEPLOY.md):
#   PROJECT_ID=my-project bash deploy/gcp-setup.sh init        # APIs, registry, database, secrets, service accounts
#   PROJECT_ID=my-project bash deploy/gcp-setup.sh scheduler   # after the first deploy: daily maintenance call
# Safe to run again: anything that already exists is kept. No secret is printed, and the database password
# never appears on a command line. If the database step times out, wait a few minutes and run init again.
set -euo pipefail

PROJECT_ID="${PROJECT_ID:?Set PROJECT_ID, e.g. PROJECT_ID=my-project bash deploy/gcp-setup.sh init}"
REGION="${REGION:-asia-south1}"            # Mumbai. The privacy policy names this region; change both together.
SERVICE="${SERVICE:-vajrax}"
REPOSITORY="${REPOSITORY:-vajrax}"
SQL_INSTANCE="${SQL_INSTANCE:-vajrax-db}"
SQL_TIER="${SQL_TIER:-db-g1-small}"        # shared core, 1.7 GB, 50 connections; db-custom-1-3840 for more load
DB_NAME="${DB_NAME:-vajrax}"
DB_USER="${DB_USER:-vajrax}"
RUN_SA="vajrax-run@${PROJECT_ID}.iam.gserviceaccount.com"
BUILD_SA="vajrax-build@${PROJECT_ID}.iam.gserviceaccount.com"
SECRETS=(vajrax-jwt-secret vajrax-db-password vajrax-jobs-token vajrax-metrics-token vajrax-smtp-password)

gc() { gcloud --project="$PROJECT_ID" --quiet "$@"; }
exists() { "$@" >/dev/null 2>&1; }
step() { printf '\n== %s\n' "$*"; }

enable_apis() {
  step "Enabling APIs"
  gc services enable run.googleapis.com sqladmin.googleapis.com artifactregistry.googleapis.com \
    cloudbuild.googleapis.com secretmanager.googleapis.com cloudscheduler.googleapis.com iam.googleapis.com
}

create_registry() {
  step "Artifact Registry repository $REPOSITORY ($REGION)"
  exists gc artifacts repositories describe "$REPOSITORY" --location="$REGION" ||
    gc artifacts repositories create "$REPOSITORY" --repository-format=docker --location="$REGION" \
      --description="VAJRAX server images"
}

# Secrets stay in the region (user-managed replication), like the data.
new_secret() {
  local name="$1" value="$2"
  if exists gc secrets describe "$name"; then echo "secret $name: kept"; return; fi
  printf '%s' "$value" | gc secrets create "$name" --replication-policy=user-managed --locations="$REGION" --data-file=-
}

create_secrets() {
  step "Secrets (random values; nothing is printed)"
  new_secret vajrax-jwt-secret "$(openssl rand -hex 32)"
  new_secret vajrax-db-password "$(openssl rand -hex 24)"
  new_secret vajrax-jobs-token "$(openssl rand -hex 24)"
  new_secret vajrax-metrics-token "$(openssl rand -hex 24)"
  # Replace with the real password when email is set up (DEPLOY.md › Email).
  new_secret vajrax-smtp-password "not-set"
}

create_database() {
  step "Cloud SQL instance $SQL_INSTANCE (PostgreSQL 17, $SQL_TIER) — the first run takes about 10 minutes"
  if ! exists gc sql instances describe "$SQL_INSTANCE"; then
    # Backups: daily at 02:30 IST, kept 7 days, point-in-time recovery for 7 days (matches the privacy policy).
    gc sql instances create "$SQL_INSTANCE" \
      --database-version=POSTGRES_17 --edition=enterprise --tier="$SQL_TIER" \
      --region="$REGION" --availability-type=zonal \
      --storage-type=SSD --storage-size=10 --storage-auto-increase \
      --backup-start-time=21:00 --enable-point-in-time-recovery \
      --retained-backups-count=7 --retained-transaction-log-days=7 \
      --deletion-protection
  fi
  exists gc sql databases describe "$DB_NAME" --instance="$SQL_INSTANCE" ||
    gc sql databases create "$DB_NAME" --instance="$SQL_INSTANCE"

  if [ -z "$(gc sql users list --instance="$SQL_INSTANCE" --filter="name=$DB_USER" --format='value(name)')" ]; then
    # The password goes from Secret Manager into the request body (printf is a shell builtin), never onto a command line.
    local password
    password="$(gc secrets versions access latest --secret=vajrax-db-password)"
    printf '{"name":"%s","password":"%s"}' "$DB_USER" "$password" |
      curl -sS --fail-with-body -X POST -H "Content-Type: application/json" \
        -H "Authorization: Bearer $(gcloud auth print-access-token)" --data @- \
        "https://sqladmin.googleapis.com/v1/projects/$PROJECT_ID/instances/$SQL_INSTANCE/users" >/dev/null
    unset password
    echo "database user $DB_USER: created"
  else
    echo "database user $DB_USER: kept"
  fi
}

create_accounts() {
  step "Service accounts"
  local created=0
  exists gc iam service-accounts describe "$RUN_SA" ||
    { gc iam service-accounts create vajrax-run --display-name="VAJRAX server (Cloud Run)"; created=1; }
  exists gc iam service-accounts describe "$BUILD_SA" ||
    { gc iam service-accounts create vajrax-build --display-name="VAJRAX build and deploy (Cloud Build)"; created=1; }
  # New accounts take a moment before IAM accepts them.
  [ "$created" = 0 ] || sleep 20

  step "Permissions: server = Cloud SQL client + its own secrets only"
  gc projects add-iam-policy-binding "$PROJECT_ID" --member="serviceAccount:$RUN_SA" \
    --role=roles/cloudsql.client --condition=None >/dev/null
  for s in "${SECRETS[@]}"; do
    gc secrets add-iam-policy-binding "$s" --member="serviceAccount:$RUN_SA" \
      --role=roles/secretmanager.secretAccessor >/dev/null
  done

  step "Permissions: build = push images, deploy as vajrax-run, write build logs, read the uploaded source"
  gc artifacts repositories add-iam-policy-binding "$REPOSITORY" --location="$REGION" \
    --member="serviceAccount:$BUILD_SA" --role=roles/artifactregistry.writer >/dev/null
  for role in roles/run.admin roles/logging.logWriter roles/storage.objectViewer; do
    gc projects add-iam-policy-binding "$PROJECT_ID" --member="serviceAccount:$BUILD_SA" \
      --role="$role" --condition=None >/dev/null
  done
  gc iam service-accounts add-iam-policy-binding "$RUN_SA" --member="serviceAccount:$BUILD_SA" \
    --role=roles/iam.serviceAccountUser >/dev/null
}

create_scheduler() {
  step "Daily maintenance call (Cloud Scheduler → POST /internal/jobs/maintenance, 03:30 IST)"
  local url token action headers
  # SERVICE_URL=https://your-domain when the run.app address is closed to outside traffic (DEPLOY.md › Custom domain).
  url="${SERVICE_URL:-$(gc run services describe "$SERVICE" --region="$REGION" --format='value(status.url)')}"
  [ -n "$url" ] || { echo "Deploy the service first (DEPLOY.md › First deploy)."; exit 1; }
  token="$(gc secrets versions access latest --secret=vajrax-jobs-token)"
  if exists gc scheduler jobs describe vajrax-maintenance --location="$REGION"; then
    action=update; headers=--update-headers
  else
    action=create; headers=--headers
  fi
  gc scheduler jobs "$action" http vajrax-maintenance --location="$REGION" \
    --schedule="30 3 * * *" --time-zone="Asia/Kolkata" \
    --uri="$url/internal/jobs/maintenance" --http-method=POST \
    "$headers=Authorization=Bearer $token" --attempt-deadline=120s >/dev/null
  unset token
  echo "vajrax-maintenance: ${action}d for $url"
}

case "${1:-}" in
  init)
    gc config set project "$PROJECT_ID" >/dev/null 2>&1 || true
    enable_apis
    create_registry
    create_secrets
    create_database
    create_accounts
    step "Done. Next: deploy (DEPLOY.md › First deploy):"
    echo "  gcloud builds submit --project $PROJECT_ID --config deploy/cloudbuild.yaml \\"
    echo "    --service-account projects/$PROJECT_ID/serviceAccounts/$BUILD_SA \\"
    echo "    --substitutions SHORT_SHA=\$(git rev-parse --short HEAD)"
    ;;
  scheduler)
    create_scheduler
    ;;
  *)
    echo "usage: PROJECT_ID=<project> bash deploy/gcp-setup.sh init|scheduler" >&2
    exit 2
    ;;
esac
