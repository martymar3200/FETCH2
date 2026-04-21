# FETCH2 Production Deployment Guide

This guide covers deploying FETCH2 to a production environment. For local development setup, see the [root README](README.md) and [fetch-local/README](fetch-local/README.md).

> **Note:** FETCH2 was originally deployed on Kubernetes with Podman, HAProxy, and Ansible. Your deployment infrastructure may differ — the concepts below apply regardless of your specific orchestrator.

---

## Architecture Overview

A production FETCH2 deployment consists of three services:

```
┌──────────────────────────────────────────────────────┐
│                  Load Balancer / Ingress              │
│               (HAProxy, K8s Ingress, etc.)            │
└────────────┬───────────────────────┬─────────────────┘
             │                       │
    ┌────────▼────────┐    ┌────────▼────────┐
    │   Web App (Vue) │    │  Inventory API  │
    │   NGINX + PWA   │    │  FastAPI/Gunicorn│
    │   Port 80/443   │    │   Port 8001      │
    └─────────────────┘    └────────┬─────────┘
                                    │
                           ┌────────▼────────┐
                           │   PostgreSQL    │
                           │   Port 5432     │
                           └─────────────────┘
```

| Service | Description | Source |
|---|---|---|
| **Web App** | Vue 3 / Quasar PWA, built as static files and served by NGINX | `vue/` |
| **Inventory API** | Python / FastAPI backend, served by Gunicorn with Uvicorn workers | `inventory_service/` |
| **PostgreSQL** | Database engine (containerized or managed service) | `database/` |

---

## 1. Environment Variables

### Backend (Inventory API)

The backend is configured via environment variables (or a `.env` file). These are defined in `inventory_service/app/config/config.py` and **must be set at container runtime** — they are NOT baked into the image.

| Variable | Required | Default | Description |
|---|---|---|---|
| `APP_ENVIRONMENT` | Yes | `local` | Deployment environment. Must be one of: `local`, `debug`, `develop`, `test`, `stage`, `production`. Controls SAML config selection, SchemaSpy, and legacy login availability. |
| `SECRET_KEY` | **Yes** | *(none — will crash if missing)* | Secret key for JWT token signing. Use a strong, random string (e.g., `openssl rand -hex 32`). |
| `DATABASE_URL` | Yes | `(Required — e.g. postgresql://user:pass@host:5432/db)` | Full SQLAlchemy connection string for the PostgreSQL database. |
| `MIGRATION_URL` | No | `(Required — e.g. postgresql://user:pass@host:5432/db)` | Database URL used by Alembic for migrations. Only needed if running migrations from outside the container network. |
| `VUE_HOST` | Yes | `https://localhost:8000` | The base URL of the Vue frontend. Used for SAML redirect after login. Set to your production URL (e.g., `https://fetch.example.com`). |
| `ALLOWED_ORIGINS` | Yes | localhost variants | Comma-separated list of allowed CORS origins. Must include your production frontend URL. |
| `ALLOWED_ORIGINS_REGEX` | No | `^https://.*\.example\.com$` | Regex pattern for additional CORS origin matching. |
| `APP_NAME` | No | `FETCH` | Application name displayed in API responses. |
| `TIMEZONE` | No | `America/New_York` | Server timezone for date operations. |
| `IDP_ENTITY_ID` | No | `https://idp.example.net/...` | SAML Identity Provider entity ID (also configured in SAML config files). |
| `IDP_LOGIN_URL` | No | `https://login.example.com/...` | SAML Identity Provider login URL. |
| `ENABLE_ORM_SQL_LOGGING` | No | `false` | Enable SQLAlchemy SQL query logging. Leave `false` in production. |

> [!CAUTION]
> **SECURITY CRITICAL:** The `SECRET_KEY` is absolutely required for JWT security. NEVER reuse a key from development or another environment. If this key is compromised, an attacker can impersonate any user in the system. Generate a fresh key using:
> ```bash
> openssl rand -hex 32
> ```
> Inject it via Kubernetes Secrets, Docker secrets, or your cloud provider's secrets manager — **never** in plain text in version-controlled manifests.

### Frontend (Vue)

The Vue frontend uses build-time environment variables defined in an `.env` file at `vue/env/.env`. These are compiled into the static bundle during the build step, so they must be set **before building the image**.

| Variable | Description | Example (Production) |
|---|---|---|
| `VITE_ENV` | Environment name | `production` |
| `VITE_API_BASE_URI` | Base URL of the Inventory API | `https://api.fetch.example.com/` |
| `VITE_INV_SERVCE_API` | Inventory Service API URL (same as above) | `https://api.fetch.example.com/` |

Create your environment file before building:
```bash
cd vue/env
cp .env.example .env
# Edit .env with your production values
```

---

## 2. Container Images

Each service has per-environment Containerfiles. For production, use:

| Service | Containerfile | Base Image |
|---|---|---|
| Inventory API | `inventory_service/images/api.prod.Containerfile` | `python:3.11.4-slim` → Gunicorn |
| Web App | `vue/images/web.prod.Containerfile` | `node:22-alpine` (build) → `nginx:1.27.2-alpine` (serve) |
| Database | `database/pgadmin/images/` | PostgreSQL official image |

### Building Images

```bash
# Build the Inventory API image
cd inventory_service
podman build -f images/api.prod.Containerfile -t fetch-inventory-api:latest .

# Build the Web App image
# IMPORTANT: Create vue/env/.env with production values BEFORE building
cd vue
podman build -f images/web.prod.Containerfile -t fetch-web-app:latest .
```

### Pushing to a Registry

```bash
# Tag and push to your container registry
podman tag fetch-inventory-api:latest registry.example.com/fetch/inventory-api:latest
podman push registry.example.com/fetch/inventory-api:latest

podman tag fetch-web-app:latest registry.example.com/fetch/web-app:latest
podman push registry.example.com/fetch/web-app:latest
```

### Production Image Details

**Inventory API (`api.prod.Containerfile`):**
- Multi-stage build: Poetry exports `requirements.txt`, then installs in a clean image
- Runs Gunicorn with 5 Uvicorn workers (tuned for `2 * CPU + 1`)
- Secrets are NOT baked in — injected at runtime
- Alembic migrations run automatically on container startup (with advisory locking for multi-replica safety)

> **⚠️ Gunicorn Workers:** The default worker count of 5 is tuned for a 2-core server using the formula `2 * CPU_CORES + 1`. If your production server has different specifications, adjust the `--workers` value in the Containerfile CMD accordingly. For a 4-core machine, use 9 workers; for an 8-core machine, use 17.

> **⚠️ SAML Signing:** The `xmlsec` build dependencies are commented out in the Containerfile by default. If your Identity Provider requires signed SAML assertions (common in enterprise/government environments), you must uncomment the `xmlsec` build dependencies in `api.prod.Containerfile` AND the corresponding `pip install` line. See the comments in the Containerfile for details.

**Web App (`web.prod.Containerfile`):**
- Multi-stage build: Node builds the Quasar PWA, then copies static files to NGINX
- NGINX serves the compiled PWA with TLS (self-signed by default)
- TLS certificates are generated at **runtime** via an entrypoint script — if you mount real certificates, the self-signed generation is skipped automatically
- NGINX config at `vue/nginx/production.conf` enforces TLS 1.2+, HSTS, and security headers
- Custom CA certificates can be added by uncommenting the `ADD certificates/` lines in the Containerfile

---

## 3. Database Setup

### Option A: Managed PostgreSQL (Recommended for Production)

Use your cloud provider's managed PostgreSQL service (e.g., AWS RDS, Azure Database, GCP Cloud SQL).

1. Create a PostgreSQL instance (version 14+)
2. Create a database named `inventory_service`
3. Create a dedicated user with full privileges on that database
4. Set `DATABASE_URL` on the API container:
   ```
   DATABASE_URL=postgresql://fetch_user:strong_password@db-host.example.com:5432/inventory_service
   ```

### Option B: Containerized PostgreSQL

Use the database container image from `database/` for simpler deployments:

```bash
cd database
podman build -f pgadmin/images/postgres.local.Containerfile -t fetch-postgres:latest .
podman run -d --name fetch-db \
  -e POSTGRES_PASSWORD=strong_password \
  -e POSTGRES_DB=inventory_service \
  -p 5432:5432 \
  -v fetch-db-data:/var/lib/postgresql/data \
  fetch-postgres:latest
```

> **⚠️ Important:** For containerized PostgreSQL, always mount a persistent volume for `/var/lib/postgresql/data`. Without this, data is lost when the container is recreated.

### Schema Initialization

The Inventory API automatically runs Alembic migrations on startup. The first boot against an empty database will create all tables and indexes. No manual migration step is required.

> **Multi-Replica Safety:** When running multiple API replicas, migrations are protected by a PostgreSQL advisory lock. Only the first replica to start will run migrations — the others will detect the lock and skip. This prevents race conditions where multiple containers try to alter the schema simultaneously.

### Bootstrapping the Initial Admin User

In production you will **not** use the fake data seeder (`seed-fake-data`), as it injects thousands of fake library items into the database. Furthermore, you should **avoid** manually manipulating the raw `fixtures/**/*.json` files to create your facility data, as maintaining raw JSON containing complex foreign key relationships is extremely error-prone.

Instead, you will use the **Production Bootstrap Tool**. This script safely injects a single System Administrator account into the database so you can log in, and then you use the secure frontend Admin UI to configure your facility setup.

1. Ensure your API container is running.
2. From the `inventory_service/` directory on the host, run the bootstrap command with the email address of the person who will be the administrator (must match their SAML SSO email):
   ```bash
   ./helper.sh bootstrap-production admin@example.com
   ```
3. Log into the FETCH2 frontend via SSO.
4. Navigate to the **Admin Dashboard**.
5. Use the UI to securely construct your "basic seed data":
   - Navigate to **Location Explorer** to map out your physical facility (Buildings -> Modules -> Aisles -> Shelves).
   - Navigate to **Manage Owners** and **Manage List Configurations** (Size Classes, Media Types, Request Types) to define the specific rules and container formats your facility uses.

---

## 4. Authentication (SAML SSO)

In production (`APP_ENVIRONMENT=production`), the legacy login endpoint is **disabled**. All authentication goes through SAML SSO.

### SAML Configuration Files

SAML is configured per environment in `inventory_service/app/saml/config/`:

| File | Used When |
|---|---|
| `local_saml_config.json` | `APP_ENVIRONMENT` = `local` or `debug` |
| `dev_saml_config.json` | `APP_ENVIRONMENT` = `develop` |
| `test_saml_config.json` | `APP_ENVIRONMENT` = `test` |
| `stage_saml_config.json` | `APP_ENVIRONMENT` = `stage` |
| `prod_saml_config.json` | `APP_ENVIRONMENT` = `production` |

### Configuring Your Identity Provider

Edit the appropriate SAML config file and update the following values:

#### Service Provider (SP) — Your FETCH2 Instance
```json
{
  "sp": {
    "entityId": "https://your-fetch-api.example.com/",
    "assertionConsumerService": {
      "url": "https://your-fetch-api.example.com/auth/sso/acs",
      "binding": "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
    },
    "singleLogoutService": {
      "url": "https://your-fetch-api.example.com/auth/sso/logout",
      "binding": "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
    },
    "x509cert": "",
    "privateKey": ""
  }
}
```

#### Identity Provider (IdP) — Your Organization's SSO
```json
{
  "idp": {
    "entityId": "https://your-idp.example.com/entity-id",
    "singleSignOnService": {
      "url": "https://your-idp.example.com/saml2/login",
      "binding": "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
    },
    "singleLogoutService": {
      "url": "https://your-idp.example.com/saml2/logout",
      "binding": "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"
    },
    "x509cert": "YOUR_IDP_SIGNING_CERTIFICATE_HERE"
  }
}
```

### Required SAML Attributes

The SAML response from your IdP **must** include the following attribute claims:

| Attribute | SAML Claim URI |
|---|---|
| Email | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress` |
| First Name | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname` |
| Last Name | `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname` |

### SP Certificates

For production SAML, you should generate or obtain a signing certificate for the Service Provider:

```bash
# Generate SP signing cert and key
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -subj "/C=US/ST=DC/L=Washington/O=YourOrg/CN=fetch-sp" \
  -keyout sp-key.pem -out sp-cert.pem
```

Place these in `inventory_service/app/saml/production/` and the startup code will load them automatically.

### IdP Registration

Register FETCH2 as a Service Provider with your Identity Provider using:
- **Entity ID**: Your SP entity ID (e.g., `https://your-fetch-api.example.com/`)
- **ACS URL**: `https://your-fetch-api.example.com/auth/sso/acs`
- **SP Metadata**: Available at `https://your-fetch-api.example.com/auth/sso/metadata` once the API is running

---

## 5. TLS / HTTPS

### Web App (NGINX)

The production NGINX config (`vue/nginx/production.conf`) is pre-configured for TLS with:
- TLS 1.2 and 1.3 only
- Strong cipher suite
- HSTS headers
- X-Frame-Options DENY

By default, the Web App container generates a **self-signed certificate** at startup if no real certificate is mounted. For production, mount real certificates to bypass the self-signed generation:

1. Obtain a certificate from your CA (or use Let's Encrypt)
2. Mount the certificate and key into the container:
   ```yaml
   volumes:
     - /path/to/your/cert.crt:/etc/ssl/certs/server.crt:ro
     - /path/to/your/cert.key:/etc/ssl/private/server.key:ro
   ```
3. The entrypoint script will detect the mounted certificate and skip self-signed generation
4. Update `server_name` in `vue/nginx/production.conf` to your domain (default accepts any hostname)

### Inventory API

The API itself runs on HTTP (port 8001) behind the NGINX reverse proxy or load balancer. TLS termination should happen at the load balancer or ingress layer.

---

## 6. Kubernetes Deployment

Pre-built K8s manifests are available in `inventory_service/k8s/` and `vue/k8s/`. Ansible playbooks for cluster provisioning are in `build/ansible/k8s/`.

### Namespace

All FETCH2 resources deploy to the `fetch` namespace:
```bash
kubectl create namespace fetch
```

### Container Registry Credentials

If your registry requires authentication:
```bash
kubectl create secret docker-registry gitlab-api-2023 \
  --namespace=fetch \
  --docker-server=registry.example.com \
  --docker-username=your-user \
  --docker-password=your-token
```

### Deploying the API

1. Update the image reference in `inventory_service/k8s/deployment-prod.yml`:
   ```yaml
   image: registry.example.com/fetch/inventory-api:latest
   ```
2. Create a Kubernetes Secret for the API environment:
   ```bash
   kubectl create secret generic fetch-api-secrets \
     --namespace=fetch \
     --from-literal=SECRET_KEY=$(openssl rand -hex 32) \
     --from-literal=DATABASE_URL=postgresql://user:pass@db-host:5432/inventory_service \
     --from-literal=APP_ENVIRONMENT=production \
     --from-literal=VUE_HOST=https://fetch.example.com \
     --from-literal=ALLOWED_ORIGINS=https://fetch.example.com
   ```
3. Add the secret reference to your deployment spec:
   ```yaml
   envFrom:
     - secretRef:
         name: fetch-api-secrets
   ```
4. Apply:
   ```bash
   kubectl apply -f inventory_service/k8s/deployment-prod.yml
   kubectl apply -f inventory_service/k8s/service.yml
   ```

### Deploying the Web App

Follow the same pattern for `vue/k8s/deployment-prod.yml`.

### Production Resource Allocation

The production API deployment is configured with:
- **Requests:** 1 CPU core, 1 GiB memory
- **Limits:** 2 CPU cores, 4 GiB memory
- **Replicas:** 1 (increase as needed)

Adjust based on your expected load.

---

## 7. Infrastructure Tooling

The `build/` directory contains additional deployment tooling:

| Path | Purpose |
|---|---|
| `build/ansible/core/` | Base server provisioning (system tools, monitoring agents) |
| `build/ansible/haproxy/` | HAProxy load balancer setup |
| `build/ansible/k8s/` | Kubernetes cluster provisioning |
| `build/ansible/inventory/` | Host inventory files per environment (`dev.ini`, `test.ini`, `stage.ini`, `prod.ini`) |
| `build/docker/prometheus-grafana/` | Monitoring stack (Prometheus + Grafana) |
| `build/tools/sonarqube/` | SonarQube code quality server |

> These configurations are inherited from the original deployment and may need customization for your infrastructure.

---

## 8. Post-Deployment Checklist

After deploying all services:

- [ ] Verify the API is responding: `curl https://your-api.example.com/`
- [ ] Verify the web app loads: open `https://fetch.example.com` in a browser
- [ ] Test SSO login flow end-to-end
- [ ] Create an Admin group and assign the first user (see [CONFIGURATION.md](CONFIGURATION.md))
- [ ] Configure reference data (Owners, Media Types, Size Classes, etc.)
- [ ] Build out your location hierarchy (Building → Module → Aisle → Shelf)
- [ ] (Optional) Configure ILS integration via Admin > ILS Integrations
- [ ] Verify HTTPS is working with valid certificates
- [ ] Confirm database backups are scheduled
- [ ] Review CORS settings in `ALLOWED_ORIGINS`

---

## 9. Backup & Recovery

### Database Backups

The `inventory_service/helper.sh` includes a `extract-data-migration` function that creates compressed PostgreSQL dumps:

```bash
# From the inventory_service directory
./helper.sh extract-data-migration
```

This produces a compressed `.dump.xz` file suitable for `pg_restore`.

To restore from a backup:
```bash
./helper.sh pg-restore <filename.dump.xz>
```

For production, configure automated backups via your managed database service or a cron job running `pg_dump`.

### Recommended Backup Schedule

| Backup Type | Frequency | Retention |
|---|---|---|
| Full database dump | Daily | 30 days |
| Point-in-time recovery (WAL) | Continuous | 7 days |
| Pre-upgrade snapshot | Before each deployment | Until verified |
