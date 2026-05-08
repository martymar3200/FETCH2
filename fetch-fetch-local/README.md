# fetch-local

## About

This is the starting point for working with FETCH2 locally as a developer. After following the below steps, you should have the application running locally with Docker or Podman.

## Prerequisites

1. Install [Homebrew](https://brew.sh/)
2. Install a newer version of git: `$ brew install git`
3. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) or [Podman Desktop](https://podman-desktop.io/)
4. Configure your container runtime to allow 8g of memory, and 200g of disk space.

> **Note:** The `fetch-local` compose file and helper scripts use `docker compose` commands. If you are using Podman, the `podman compose` command is a drop-in replacement — both tools use the same compose file format. The standalone build scripts in `fetch-inventory_service/` and `fetch-vue/` reference `podman` directly for deployed environment builds.

## Getting Started

### 1. Clone the Repository

```bash
git clone <your-repository-url> FETCH2
cd FETCH2
```

### 2. Configure Environment Variables

Before starting the containers, you must create local environment files from the provided templates:

```bash
# Backend configuration
cp fetch-inventory_service/.env.example fetch-inventory_service/.env

# Infrastructure configuration
cp fetch-fetch-local/.env.example fetch-fetch-local/.env

# Frontend configuration
cp fetch-vue/env/.env.example fetch-vue/env/.env.local
```

### 3. Start the Application

```bash
cd fetch-local
docker compose up --build -d
```

> [!IMPORTANT]  
> **Rebuilding the Web App**: If you ever change the values in `fetch-vue/env/.env.local`, you **must** run `docker compose build web` for the changes to take effect, as the Quasar PWA is compiled into static files at build-time.

On first boot, the Inventory Service container will automatically run Alembic database migrations to create the schema. Watch the `inventory-api` container logs for `"Migrating..."` and `"Application startup complete"` to confirm it's ready.

### 4. Seed Test Data

The database starts with only the schema — no sample data. To populate the database with test users, locations, shelves, and other sample data:

```bash
./helper.sh build-inventory-api
```

This rebuilds the API container and runs the data seeding script, which creates:
- Location hierarchy (buildings, modules, aisles, sides, ladders, shelves, shelf positions)
- Three test users: `admin@example.com`, `tester1@example.com`, `tester2@example.com`
- Groups and permissions
- Reference data (media types, size classes, barcode types, etc.)

> **Note:** The full seed generates ~2,400 shelves and associated positions. This can take a few minutes on the first run.

### 5. Open the Application

Navigate to **https://127.0.0.1:8000** in your browser.

> **⚠️ SSL Certificate Warning:** The local environment serves the app over HTTPS. By default, I have generated a self-signed certificate in `fetch-local/.certs/`. Your browser will show a security warning:
> - **Action**: You must click "Advanced" → "Proceed" for **both** the frontend (`https://localhost:8000`) and the API (`https://localhost:8001`) in your browser to allow background requests to succeed.
>
> **Permanent Fix (Recommended)**: Use **`mkcert`** to create a trusted local Certificate Authority:
> ```bash
> brew install mkcert
> mkcert -install
> cd fetch-local/.certs
> mkcert localhost 127.0.0.1 ::1
> ```

### 6. Log In

In local and debug environments, a **legacy login** endpoint is available that does not require an SSO identity provider. Use the login form with one of the seeded user emails:

| Email | Name |
|---|---|
| `admin@example.com` | Admin Istrator |
| `tester1@example.com` | Tester One |
| `tester2@example.com` | Tester Two |

After logging in, users may need to be assigned to **Groups** (via Admin > Groups) to gain access to specific workflows. Permissions are group-based, not user-based.

## Stopping and Restarting

```bash
# Stop all containers
docker compose down

# Start again (without rebuilding)
docker compose up -d

# Start with a full rebuild (after code changes)
docker compose up --build -d
```

## Helper Scripts

Each app repository provides helper scripts under `helper.sh` for common developer tasks. These scripts assume the directory tree created by the install process (all repos side-by-side in the same parent directory).

### fetch-local

| Command | Description |
|---|---|
| `./helper.sh build-inventory-api` | Rebuild the API container and re-seed the database |
| `./helper.sh build-inventory-db` | Rebuild the database container (without wiping data) |
| `./helper.sh wipe-inventory-db` | Rebuild the database container **and wipe all data** |

### Database Reset

To completely reset the database (wipe data, rebuild schema, and re-seed):

```bash
./helper.sh wipe-inventory-db
sleep 5
./helper.sh build-inventory-api
```

Or use the `refresh-db` command from the `fetch-inventory_service/` or `fetch-vue/` helper scripts, which automate this sequence.

## Services

| Service | URL | Notes |
|---|---|---|
| Web App | https://127.0.0.1:8000/ | Vue/Quasar PWA served via NGINX |
| Inventory API | http://127.0.0.1:8001/ | FastAPI backend (OpenAPI docs at `/docs`) |
| PGAdmin | http://127.0.0.1:5050/ | Database management UI |
| PostgreSQL | `localhost:5432` | Connect via PGAdmin or `psql` |

### PGAdmin

Local login user: `admin@fetch.example.com`
Local login pass: `admin`

Once in, right click Servers and select Register -> Server

On the general tab, set name to `local`

On the connection tab:
 - Set hostname to `host.docker.internal`
 - Set username to `postgres`
 - Set password to `postgres`

Leave the port as is, and select option to remember password.

### Postgres

The Postgres Engine is served on port 5432. Interact with it either through pgAdmin, or by jumping into PSQL inside the Postgres container.
