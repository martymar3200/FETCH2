# FETCH2 Migration Plan: Existing LC Installation (Staging/Prod)

This document provides instructions for upgrading an existing Library of Congress (LC) FETCH installation to **FETCH2**. This plan assumes your environment already has a working database and a configured SAML Identity Provider (IdP).

## 1. Objectives
- Upgrade existing LC installation to FETCH2 with minimal downtime.
- Preserving existing users and SAML Identity Provider settings.
- Apply 28 new database migrations to support modernized workflows.

## 2. Pre-Migration Checklist
- [ ] **Database Backup**: Perform a full `pg_dump` of your production/staging database.
- [ ] **SAML Assets**: Locate your existing `sp-cert.pem` and `sp-key.pem` (or your equivalent SAML signing certificates).
- [ ] **Infrastructure**: Ensure Python 3.11+ and Node 22+ are available.

## 3. Migration Steps

### Step A: Code Replacement
Since FETCH2 now follows the `fetch-` nomenclature, you can replace your existing folders directly:
1. Stop the current servers.
2. Replace the contents of your existing `fetch-inventory_service/`, `fetch-vue/`, and `fetch-database/` folders with the FETCH2 source code.

### Step B: Certificate Migration
FETCH2 looks for SAML certificates in environment-specific subfolders. 
1. Copy your existing SAML certificates to:
   - `fetch-inventory_service/app/saml/stage/cert.pem`
   - `fetch-inventory_service/app/saml/stage/key.pem`
   *(Replace `stage` with `production` for the production environment).*

### Step C: Environment Configuration
Create a new `.env` file in `fetch-inventory_service/.env`. 
```bash
APP_ENVIRONMENT=stage  # or 'production'
SECRET_KEY=$(openssl rand -hex 32)
DATABASE_URL=postgresql://user:pass@db-host:5432/inventory_service
VUE_HOST=https://your-staging-url.example.com
ALLOWED_ORIGINS=https://your-staging-url.example.com
```

### Step D: Database Upgrade (MANDATORY)
Apply the 28 new migrations. This adds the required security columns to your existing `User` table and creates the new workflow tables.
```bash
cd fetch-inventory_service
poetry install
poetry run alembic upgrade head
```

### Step E: Frontend Build
Build the new Vue 3 / Quasar PWA:
```bash
cd fetch-vue
npm install
npm run build
```

## 4. Post-Migration Verification
1. **SAML Login**: Attempt to log in via SSO. Since your email already exists in the `User` table, FETCH2 will automatically link your session.
2. **Cookie Check**: If login fails, check your browser developer tools to ensure the `fetch_auth_token` cookie is being set as `Secure` and `HttpOnly`.
3. **New Features**: Verify that "Shipping"is now accessible in the sidebar.

## 5. Rollback Plan
1. Restore the database from the backup.
2. Revert to the original LC codebase.
