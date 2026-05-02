# FETCH2 Migration Plan: Dev & Test Environments

This document provides the step-by-step instructions to migrate the **Dev** and **Test** environments from the original Library of Congress (LC) FETCH codebase to the modernized **FETCH2** architecture.

## 1. Objectives
- Update code to FETCH2 while maintaining **Legacy Login** (no SAML required).
- Apply 28 new database migrations to support modern features (Shipping, Pick Lists, ILS).
- Maintain the standardized `fetch-` nomenclature for repository consistency.

## 2. Pre-Migration Checklist
- [ ] Take a full PostgreSQL dump of the current database.
- [ ] Confirm Python 3.11+ and Node 22+ are installed on the target environment.
- [ ] Ensure **Poetry** is installed (`pip install poetry`).

## 3. Migration Steps

### Step A: Folder Alignment
Since FETCH2 has been updated to use the `fetch-` prefix, its folder names now match your original LC environments exactly. No folder renaming is required if your existing folders are already named:
- `fetch-inventory_service`
- `fetch-vue`
- `fetch-database`
- `fetch-fetch-local`

### Step B: Code Update
1. Clear out the contents of your existing folders (preserving your `.git` directory).
2. Copy the FETCH2 code into the respective folders.

### Step C: Environment Configuration
Create a new `.env` file in `fetch-inventory_service/.env`. 

**For Dev Environment:**
```bash
APP_ENVIRONMENT=develop
SECRET_KEY=generate_a_random_32_char_string
DATABASE_URL=postgresql://user:pass@localhost:5432/inventory_service
VUE_HOST=http://your-dev-url:8000
ALLOWED_ORIGINS=http://your-dev-url:8000
```
*(Repeat for Test, changing `APP_ENVIRONMENT=test` and the URLs).*

### Step D: Database Upgrade
Apply the 28 new migrations to the existing database:
```bash
cd fetch-inventory_service
poetry install
poetry run alembic upgrade head
```

### Step E: Frontend Build
1. Configure `fetch-vue/env/.env` with your API URLs.
2. Build the Quasar PWA:
```bash
cd fetch-vue
npm install
npm run build
```

## 4. Verification
1. **Check API Health**: Visit `http://your-dev-url:8001/status`.
2. **Test Legacy Login**: Navigate to the login page. You should be able to enter an email address to log in without a password.
3. **Verify New Modules**: Confirm that "Pick Lists" and "Shipping" appear in the sidebar.

## 5. Rollback Plan
If the migration fails:
1. Restore the database from the dump taken in Step 2.
2. Restore the original LC code.
