# FETCH2

**FETCH2** is an inventory management system for tracking and managing physical collections in high-density storage facilities. It provides a complete workflow for accessioning, verifying, shelving, retrieving, shipping, refiling, and withdrawing items — with barcode scanning, location tracking, and integration with Integrated Library Systems (ILS) like FOLIO.

> This project was originally forked from the [Library of Congress open source FETCH system](https://github.com/LibraryOfCongress?q=FETCH&type=all&language=&sort=) and has been significantly extended with new workflows, an ILS integration framework, offline-capable PWA support, a modernized CSS architecture, and a comprehensive backend test suite.

---

## Repository Structure

```
FETCH2/
├── fetch-automation/         E2E UI tests (Java, Cucumber, Selenium) ⚠️ needs updating
├── fetch-build/              Infrastructure & deployment (Ansible, Prometheus, K8s)
├── fetch-database/           Container images for PostgreSQL
├── fetch-fetch-local/        🚀 Local development entry point (Docker Compose)
├── fetch-inventory_service/  Backend API (Python, FastAPI, SQLAlchemy, Alembic)
└── fetch-vue/                Frontend PWA (Vue 3, Quasar, Pinia, Vite)
```

Each folder has its own `README.md` with detailed documentation. **Start with `fetch-fetch-local/`** for local development setup.

---

## Quick Start

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) or [Podman Desktop](https://podman-desktop.io/)
- [Homebrew](https://brew.sh/) (macOS)
- Git

### 1. Configure Environment Variables

The application requires environment variables to manage secrets and connections. **You must perform these steps before running the system for the first time:**

**Backend (`fetch-inventory_service`):**
```bash
cd fetch-inventory_service
cp .env.example .env
```

**Frontend (`fetch-vue`):**
```bash
cd fetch-vue/env
cp .env.example .env.local
```

**Local Infrastructure (`fetch-fetch-local`):**
```bash
cd fetch-fetch-local
cp .env.example .env
```

> [!IMPORTANT]  
> **Rebuild Required**: Because the Vue frontend is compiled at build-time, any changes to `.env.local` will NOT take effect until you rebuild the container:
> ```bash
> docker-compose build web
> docker-compose up -d web
> ```

> [!WARNING]  
> **Security Notice:** The default credentials in `.env.example` files are for **local development only**. In production, all secrets must be generated uniquely and injected securely (e.g., via K8s Secrets).

### 2. Setup SSL (Optional but Recommended)
To avoid "Insecure Connection" warnings in your browser, install `mkcert` and generate trusted certificates:
```bash
brew install mkcert
mkcert -install
cd fetch-fetch-local/.certs
mkcert localhost 127.0.0.1 ::1
```

### 3. Build and Run

```bash
git clone <your-repository-url> FETCH2
cd FETCH2/fetch-fetch-local
docker compose up --build
```

> **First run:** The Inventory Service container will automatically run database migrations (Alembic) to create the schema. This may take a moment on the first boot.

### 4. Seed Test Data

After the containers are running, seed the database with sample data (buildings, locations, shelves, and test users):

```bash
./helper.sh build-inventory-api
```

This rebuilds the API container and seeds the database with fake data, including three test users.

### 5. Open the Application

Navigate to **https://127.0.0.1:8000** in your browser.

> **⚠️ Self-Signed Certificate:** The local environment uses a self-signed TLS certificate. Your browser will show a security warning — this is expected. Accept the warning to proceed:
> - **Chrome:** Click "Advanced" → "Proceed to 127.0.0.1"
> - **Firefox:** Click "Advanced" → "Accept the Risk and Continue"
> - **Safari:** Click "Show Details" → "visit this website"
>
> Optionally, on macOS, you can add the generated cert from `fetch-fetch-local/.certs/` to your Keychain to suppress the warning permanently.

### 6. Log In

In local/debug environments, a **legacy login** is available (no SSO identity provider needed). Use the login form with any of the seeded test user emails:

| Email | Name | Notes |
|---|---|---|
| `admin@example.com` | Admin Istrator | Add to admin group for full access |
| `tester1@example.com` | Tester One | General test user |
| `tester2@example.com` | Tester Two | General test user |

> **Note:** User permissions are managed through **Groups** in the Admin panel. After logging in, you may need to assign users to groups (Admin > Groups) to grant access to specific workflows.

---

## Services (Local Development)

| Service | URL | Description |
|---|---|---|
| Web App | https://127.0.0.1:8000 | Vue/Quasar PWA served via NGINX |
| Inventory API | http://127.0.0.1:8001 | FastAPI backend (OpenAPI docs at `/docs`) |
| PGAdmin | http://127.0.0.1:5050 | Database management UI |
| PostgreSQL | localhost:5432 | Direct database access |

---

## Key Workflows

FETCH2 supports the complete lifecycle of physical inventory management:

- **Accession** — Intake new items with barcode scanning and container assignment
- **Verification** — Quality check accessioned items against their metadata
- **Shelving** — Assign containers to shelf locations (includes Direct-to-Shelve and Shelve-by-List modes)
- **Retrieval / Picklist** — Generate and execute pick lists for requested items
- **Shipping** — Manage outbound shipments with manifest generation
- **Refile** — Return items to storage after use, with optional ILS check-in sync
- **Withdrawal** — Permanently remove items from inventory

---

## Technology Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3, Quasar Framework, Pinia, Vite |
| Backend | Python, FastAPI, SQLAlchemy 2.0, Alembic |
| Database | PostgreSQL |
| Auth | SAML SSO (OneLogin) + Legacy login (non-prod) |
| ILS Integration | FOLIO (via adapter pattern, OAuth 2.0) |
| Containerization | Docker / Podman |
| Testing | pytest (backend), Cucumber/Selenium (E2E, stale) |

---

## Documentation

| Folder | README | Description |
|---|---|---|
| *(root)* | [CONFIGURATION.md](fetch-inventory_service/CONFIGURATION.md) | **System configuration guide — start here before production use** |
| *(root)* | [DEPLOYMENT.md](DEPLOYMENT.md) | **Production deployment guide — env vars, SSO, TLS, K8s** |
| `fetch-fetch-local/` | [README](fetch-fetch-local/README.md) | Local dev setup, compose commands, helper scripts |
| `fetch-inventory_service/` | [README](fetch-inventory_service/README.md) | API development, Poetry, migrations, auth, ILS, testing |
| `fetch-vue/` | [README](fetch-vue/README.md) | Frontend setup, PWA config, linting, permissions |
| `fetch-database/` | [README](fetch-database/README.md) | Database container management |
| `fetch-build/` | [README](fetch-build/README.md) | Infrastructure and deployment tooling |
| `fetch-automation/` | [README](fetch-automation/README.md) | E2E UI test suite (needs updating) |

---

## Testing

### Backend (Active)
The backend has a comprehensive pytest suite covering models, location hierarchies, all 7 job workflows, and RBAC security:

```bash
cd fetch-inventory_service
pytest
```

See [fetch-inventory_service/tests/README.md](fetch-inventory_service/tests/README.md) for details.

### E2E UI (Stale)
The Selenium/Cucumber tests in `fetch-automation/` have not been updated since the initial project import and need a refresh pass before use.
