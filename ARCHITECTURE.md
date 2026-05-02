# FETCH2 Architecture

This document provides a comprehensive visual reference for the FETCH2 platform architecture. All diagrams are rendered using [Mermaid](https://mermaid.js.org/) and will display natively on GitHub, GitLab, and in VS Code with the Mermaid extension.

---

## Table of Contents

1. [High-Level System Overview](#1-high-level-system-overview)
2. [SAML SSO Authentication Flow](#2-saml-sso-authentication-flow)
3. [Backend API Architecture](#3-backend-api-architecture)
4. [Frontend Architecture](#4-frontend-architecture)
5. [Data Model](#5-data-model)

---

## 1. High-Level System Overview

The production deployment consists of three core services behind a load balancer, with two external integrations (Identity Provider for SSO and FOLIO for ILS operations).

```mermaid
graph TB
    subgraph External["External Systems"]
        IDP["Identity Provider<br/>(OneLogin / SAML 2.0)"]
        FOLIO["FOLIO ILS<br/>(OAuth 2.0)"]
    end

    subgraph Ingress["Load Balancer / Ingress"]
        LB["HAProxy / K8s Ingress<br/>TLS Termination"]
    end

    subgraph Frontend["Web App Container"]
        NGINX["NGINX<br/>TLS 1.2+ / HSTS<br/>Port 443"]
        VUE["Vue 3 / Quasar PWA<br/>Static Files"]
        NGINX --> VUE
    end

    subgraph Backend["Inventory API Container"]
        GUNICORN["Gunicorn<br/>Uvicorn Workers<br/>Port 8001"]
        FASTAPI["FastAPI Application"]
        ALEMBIC["Alembic Migrations<br/>(auto on startup)"]
        GUNICORN --> FASTAPI
        FASTAPI -.-> ALEMBIC
    end

    subgraph Data["Data Layer"]
        PG[("PostgreSQL<br/>Port 5432")]
    end

    USER((User / Browser)) -->|HTTPS :443| LB
    LB -->|Static Assets| NGINX
    LB -->|API Requests /api| GUNICORN
    VUE -->|"Axios (withCredentials)<br/>REST API"| GUNICORN
    FASTAPI -->|SQLAlchemy 2.0| PG
    ALEMBIC -->|DDL / Migrations| PG
    FASTAPI <-->|"SAML 2.0<br/>AuthnRequest / ACS"| IDP
    FASTAPI <-->|"OAuth 2.0<br/>REST API"| FOLIO
    IDP -->|"SAML Response<br/>(POST to /auth/sso/acs)"| FASTAPI

    style External fill:#2d2d3f,stroke:#7c3aed,color:#e2e8f0
    style Ingress fill:#1e293b,stroke:#3b82f6,color:#e2e8f0
    style Frontend fill:#1e293b,stroke:#10b981,color:#e2e8f0
    style Backend fill:#1e293b,stroke:#f59e0b,color:#e2e8f0
    style Data fill:#1e293b,stroke:#ef4444,color:#e2e8f0
```

| Service | Technology | Port | Source |
|---|---|---|---|
| **Web App** | Vue 3 / Quasar PWA → NGINX | 443 (TLS) | `fetch-vue/` |
| **Inventory API** | Python FastAPI → Gunicorn/Uvicorn | 8001 | `fetch-inventory_service/` |
| **PostgreSQL** | PostgreSQL 14+ | 5432 | `fetch-database/` |
| **Identity Provider** | SAML 2.0 (OneLogin) | External | Configured in `app/saml/config/` |
| **FOLIO ILS** | OAuth 2.0 REST API | External | `app/ils/folio_adapter.py` |

---

## 2. SAML SSO Authentication Flow

This sequence diagram traces the complete login lifecycle, from an unauthenticated browser request through SAML negotiation, JWT issuance, and session management.

```mermaid
sequenceDiagram
    autonumber
    participant B as Browser
    participant VUE as Vue Frontend<br/>(Pinia userStore)
    participant NGINX as NGINX
    participant API as FastAPI<br/>(auth router)
    participant MW as JWTMiddleware
    participant DB as PostgreSQL
    participant IDP as Identity Provider<br/>(OneLogin)

    Note over B,IDP: SSO Login Flow (Production)

    B->>NGINX: GET https://fetch.example.com/
    NGINX->>B: Serve Vue PWA (static files)
    B->>VUE: App mounts, checks localStorage
    VUE->>API: GET /auth/me (no cookie)
    API-->>VUE: 401 Unauthorized

    Note over VUE,API: Redirect to SSO

    VUE->>API: GET /auth/sso/login?preserve_route=/shelving
    API->>API: load_saml_settings() per APP_ENVIRONMENT
    API->>API: Build SAML AuthnRequest
    API-->>B: 302 Redirect to IdP login URL

    B->>IDP: User authenticates (credentials / MFA)
    IDP->>IDP: Validate user, fetch-build SAML Assertion
    IDP->>API: POST /auth/sso/acs (SAML Response + RelayState)

    Note over API,DB: Process SAML Response

    API->>API: process_response(), validate assertion
    API->>API: Extract email, first_name, last_name from claims
    API->>DB: SELECT user WHERE email = ?
    alt User exists
        DB-->>API: User record
    else New user
        API->>DB: INSERT new User
        DB-->>API: Created user record
    end

    API->>API: generate_token() → JWT (HS256, 15min exp)
    API->>DB: UPDATE user.fetch_auth_token, fetch_auth_expiration
    API-->>B: 303 Redirect to VUE_HOST/{RelayState}<br/>Set-Cookie: fetch_auth_token (HttpOnly, Secure, SameSite=Lax)

    Note over B,MW: Authenticated Session

    B->>VUE: App loads at /shelving
    VUE->>API: GET /auth/me (cookie auto-attached)
    MW->>MW: Extract token from cookie
    MW->>MW: jwt.decode() → validate signature + expiry
    MW->>DB: Verify user.fetch_auth_expiration
    MW->>DB: Refresh expiration (sliding window +15min)
    MW->>API: Attach audit_info to request.state
    API-->>VUE: 200 { user_id, email, permissions[] }
    VUE->>VUE: Hydrate userStore, save to localStorage

    Note over B,MW: Subsequent API Requests

    VUE->>API: GET /items/?page=1 (cookie auto-attached)
    MW->>MW: Validate JWT, refresh sliding window
    MW-->>B: Re-issue cookie with refreshed JWT
    API-->>VUE: 200 { items[] }

    Note over B,API: Legacy Login (Non-Production Only)

    rect rgb(60, 60, 80)
        B->>VUE: Submit email via login form
        VUE->>API: POST /auth/legacy/login { email }
        API->>DB: SELECT user WHERE email = ?
        API->>API: generate_token() → JWT
        API-->>VUE: 200 + Set-Cookie: fetch_auth_token
        VUE->>API: GET /auth/me
        API-->>VUE: 200 { user profile + permissions }
    end
```

### Key Security Features

- **HttpOnly Cookies**: The JWT is never exposed to JavaScript — immune to XSS token theft
- **Sliding Window**: Each successful request refreshes the 15-minute expiration (NIST IA-11)
- **Dual Token Validation**: Both JWT signature AND fetch-database `fetch_auth_expiration` must be valid
- **Environment Gating**: Legacy login is disabled when `APP_ENVIRONMENT=production`
- **Sanitized Logging**: Authorization headers, cookies, and sensitive query params are redacted from logs (NIST AU-9)

---

## 3. Backend API Architecture

The FastAPI application is organized into distinct layers. Every request flows through middleware → router → business logic → fetch-database.

```mermaid
graph TB
    subgraph MW["Middleware Layer"]
        CORS["CORSMiddleware<br/>Origin validation"]
        JWT["JWTMiddleware<br/>Token validation<br/>Sliding window refresh<br/>Security logging"]
    end

    subgraph AUTH["Auth & RBAC Layer"]
        DEP["get_current_user_with_permissions<br/>(FastAPI Dependency)"]
        PERM["RequiresPermission<br/>(Per-route guard)"]
        DEP --> PERM
    end

    subgraph ROUTERS["Router Layer (52 modules)"]
        direction LR
        subgraph JOBS["Workflow Routers"]
            R_ACC["accession_jobs"]
            R_VER["verification_jobs"]
            R_SHV["shelving_jobs"]
            R_PKL["pick_lists"]
            R_SHP["shipping_jobs"]
            R_REF["refile_jobs"]
            R_WDR["withdraw_jobs"]
        end
        subgraph ENTITY["Entity Routers"]
            R_ITM["items"]
            R_TRY["trays"]
            R_NTI["non_tray_items"]
            R_SHF["shelves"]
            R_BLD["fetch-buildings"]
            R_REQ["requests"]
        end
        subgraph ADMIN["Admin Routers"]
            R_USR["users"]
            R_GRP["groups"]
            R_PRM["permissions"]
            R_RPT["reporting"]
            R_ILS["ils_configurations"]
            R_BAT["batch_upload"]
        end
        subgraph SSO["Auth Router"]
            R_AUTH["auth<br/>/sso/login<br/>/sso/acs<br/>/sso/metadata<br/>/sso/logout<br/>/legacy/login<br/>/me"]
        end
    end

    subgraph BIZ["Business Logic Layer"]
        TASKS["tasks.py<br/>Background Tasks<br/>(Accession, Verification,<br/>Shelving, Refile)"]
        UTILS["utilities.py<br/>Barcode ops, location<br/>calculations, audit"]
        SORT["sorting.py<br/>Location-aware<br/>query ordering"]
        FILTER["filter_params.py<br/>Query parameter<br/>parsing & validation"]
    end

    subgraph ILS["ILS Integration Layer"]
        FACTORY["ILSAdapterFactory"]
        IFACE["ILSAdapter Interface"]
        FOLIO_A["FOLIOAdapter<br/>(OAuth 2.0)"]
        MOCK_A["MockAdapter<br/>(Testing)"]
        ILS_TASKS["ils/tasks.py<br/>Background sync"]
        FACTORY --> IFACE
        IFACE --> FOLIO_A
        IFACE --> MOCK_A
        FACTORY --> ILS_TASKS
    end

    subgraph MODELS["SQLAlchemy 2.0 Model Layer (62 models)"]
        direction LR
        M_LOC["Location Models<br/>Building, Module, Aisle,<br/>Side, Ladder, Shelf,<br/>ShelfPosition"]
        M_INV["Inventory Models<br/>Item, Tray, NonTrayItem,<br/>Barcode, ConveyanceBin"]
        M_JOB["Job Models<br/>AccessionJob, VerificationJob,<br/>ShelvingJob, PickList,<br/>RefileJob, WithdrawJob,<br/>ShippingJob, ShippingBin"]
        M_AUTH["Auth Models<br/>User, Group, Permission,<br/>UserGroup, GroupPermission"]
        M_CFG["Config Models<br/>Owner, MediaType, SizeClass,<br/>ContainerType, ShelfType,<br/>ILSConfiguration"]
    end

    subgraph DB_LAYER["Database Session Layer"]
        ENGINE["SQLAlchemy Engine<br/>(connection pooling)"]
        SESSION["get_session()<br/>(FastAPI DI generator)"]
        SMANAGER["session_manager()<br/>(context manager)"]
        ENGINE --> SESSION
        ENGINE --> SMANAGER
    end

    subgraph MIGRATION["Migration Layer"]
        ALEMB["Alembic<br/>Auto-upgrade on startup<br/>Advisory lock (pg_try_advisory_lock)"]
    end

    PG[("PostgreSQL")]

    CORS --> JWT
    JWT --> ROUTERS
    ROUTERS --> AUTH
    ROUTERS --> BIZ
    ROUTERS --> ILS
    BIZ --> MODELS
    ILS --> MODELS
    MODELS --> DB_LAYER
    DB_LAYER --> PG
    ALEMB --> PG

    style MW fill:#1e293b,stroke:#f59e0b,color:#e2e8f0
    style AUTH fill:#1e293b,stroke:#ef4444,color:#e2e8f0
    style ROUTERS fill:#1e293b,stroke:#3b82f6,color:#e2e8f0
    style BIZ fill:#1e293b,stroke:#8b5cf6,color:#e2e8f0
    style ILS fill:#1e293b,stroke:#06b6d4,color:#e2e8f0
    style MODELS fill:#1e293b,stroke:#10b981,color:#e2e8f0
    style DB_LAYER fill:#1e293b,stroke:#f97316,color:#e2e8f0
    style MIGRATION fill:#1e293b,stroke:#6366f1,color:#e2e8f0
```

---

## 4. Frontend Architecture

The Vue 3 / Quasar PWA is organized into pages, components, Pinia stores, and a centralized HTTP client. The Vue Router enforces permission-based access control on every route.

```mermaid
graph TB
    subgraph BROWSER["Browser"]
        PWA["Service Worker<br/>(Offline PWA)"]
    end

    subgraph APP["Vue 3 Application"]
        subgraph LAYOUT["Layout Layer"]
            ML["MainLayout.fetch-vue<br/>(NavigationBar, sidebar)"]
        end

        subgraph PAGES["Page Layer (16 pages)"]
            direction LR
            P_IDX["IndexPage"]
            P_ACC["AccessionPage"]
            P_VER["VerificationPage"]
            P_SHV["ShelvingPage"]
            P_PKL["PicklistPage"]
            P_SHP["ShippingPage"]
            P_REF["RefilePage"]
            P_WDR["WithdrawalPage"]
            P_REQ["RequestPage"]
            P_ADM["AdminPage"]
            P_RMG["RecordManagementPage"]
            P_SCH["SearchPage"]
            P_RPT["ReportsPage"]
            P_UST["UserSettings"]
        end

        subgraph STORES["Pinia Store Layer (22 stores)"]
            direction LR
            S_USR["userStore<br/>(auth, permissions,<br/>login/logout)"]
            S_ACC["accessionStore"]
            S_VER["verificationStore"]
            S_SHV["shelvingStore"]
            S_PKL["picklistStore"]
            S_SHP["shippingStore"]
            S_REF["refileStore"]
            S_WDR["withdrawalStore"]
            S_BLD["fetch-buildingStore"]
            S_SCH["searchStore"]
            S_OPT["optionStore<br/>(dropdowns, lookups)"]
            S_RPT["reportsStore"]
            S_REQ["requestStore"]
            S_GRP["groupStore"]
            S_RMG["recordMgmtStore"]
            S_ILS["ilsConfigStore"]
        end

        subgraph ROUTER["Vue Router"]
            RG["Route Guards<br/>meta.requiresAuth<br/>meta.requiresPerm"]
        end

        subgraph HTTP["HTTP Layer"]
            AXIOS["Axios Instance<br/>(withCredentials: true)"]
            IS["InventoryService.js<br/>(79 endpoint mappings)"]
            AXIOS --> IS
        end
    end

    subgraph API_EXT["FastAPI Backend"]
        API_EP["REST API<br/>Port 8001"]
    end

    BROWSER --> APP
    ML --> PAGES
    PAGES --> STORES
    PAGES --> ROUTER
    STORES --> HTTP
    RG -->|"permission check<br/>vs userStore"| S_USR
    HTTP -->|"REST + HttpOnly Cookie<br/>(auto-attached)"| API_EP
    S_USR -->|"login redirect"| API_EP

    style BROWSER fill:#1e293b,stroke:#64748b,color:#e2e8f0
    style APP fill:#1e293b,stroke:#10b981,color:#e2e8f0
    style LAYOUT fill:#1e3a3a,stroke:#10b981,color:#e2e8f0
    style PAGES fill:#1e3a3a,stroke:#3b82f6,color:#e2e8f0
    style STORES fill:#1e3a3a,stroke:#f59e0b,color:#e2e8f0
    style ROUTER fill:#1e3a3a,stroke:#ef4444,color:#e2e8f0
    style HTTP fill:#1e3a3a,stroke:#8b5cf6,color:#e2e8f0
    style API_EXT fill:#1e293b,stroke:#f97316,color:#e2e8f0
```

### Frontend → Backend Endpoint Map

The `InventoryService.js` file defines 79 endpoint constants used by all Pinia stores. Key categories:

| Category | Endpoints | Example |
|---|---|---|
| **Auth** | `authSsoLogin`, `authSsoLogout`, `authLegacyLogin` | `/auth/sso/login/` |
| **Workflow Jobs** | `accessionJobs`, `verificationJobs`, `shelvingJobs`, `pickLists`, `shippingJobs`, `refileJobs`, `withdrawJobs` | `/accession-jobs/workflow/` |
| **Inventory** | `items`, `trays`, `nonTrayItems`, `shelves` | `/items/barcode/` |
| **Admin** | `users`, `groups`, `permissions`, `fetch-buildings` | `/groups/` |
| **Reporting** | 12 reporting endpoints | `/reporting/open-locations/` |
| **ILS** | `ilsConfigurations`, `ilsSyncErrors` | `/ils-configurations/` |

---

## 5. Data Model

The FETCH2 fetch-database contains 62+ tables. To keep the ER diagram readable, the model is organized into **five domain groups**, each shown as a separate diagram. Cross-domain foreign keys are noted in the relationship labels.

### 5a. Location Hierarchy

The physical facility is modeled as a strict tree: **Building → Module → Aisle → Side → Ladder → Shelf → ShelfPosition**. Each level enforces uniqueness within its parent.

```mermaid
erDiagram
    Building ||--o{ Module : "has many"
    Module ||--o{ Aisle : "has many"
    Aisle ||--o{ Side : "has many"
    Side }o--|| SideOrientation : "has one"
    Side ||--o{ Ladder : "has many"
    Ladder ||--o{ Shelf : "has many"
    Shelf ||--o{ ShelfPosition : "has many"
    Shelf }o--|| ShelfType : "typed by"
    Shelf }o--o| Owner : "owned by"
    Shelf }o--o| ContainerType : "container type"
    Shelf }o--o| Barcode : "identified by"

    Building {
        smallint id PK
        varchar name
    }
    Module {
        int id PK
        int fetch-building_id FK
        varchar module_number
    }
    Aisle {
        int id PK
        int module_id FK
        smallint aisle_number
        smallint sort_priority
    }
    SideOrientation {
        int id PK
        varchar name
    }
    Side {
        int id PK
        int aisle_id FK
        int side_orientation_id FK
    }
    Ladder {
        int id PK
        int side_id FK
        smallint ladder_number
        smallint sort_priority
    }
    Shelf {
        int id PK
        int ladder_id FK
        int shelf_type_id FK
        int owner_id FK
        int container_type_id FK
        uuid barcode_id FK
        smallint shelf_number
        smallint available_space
        numeric height
        numeric width
        numeric depth
    }
    ShelfPosition {
        int id PK
        int shelf_id FK
        smallint position_number
    }
    ShelfType {
        int id PK
        varchar name
    }
```

### 5b. Inventory Objects

Items live in Trays (which occupy ShelfPositions). Non-Tray Items occupy ShelfPositions directly. All three entities are identified by Barcodes and classified by Owner, SizeClass, MediaType, and ContainerType.

```mermaid
erDiagram
    Barcode ||--o| Item : "identifies"
    Barcode ||--o| Tray : "identifies"
    Barcode ||--o| NonTrayItem : "identifies"
    Barcode }o--|| BarcodeType : "typed by"

    Tray ||--o{ Item : "contains"
    Tray }o--o| ShelfPosition : "placed at"
    NonTrayItem }o--o| ShelfPosition : "placed at"

    Item }o--o| Owner : "owned by"
    Item }o--o| SizeClass : "classified"
    Item }o--o| MediaType : "media type"
    Item }o--o| ContainerType : "container type"
    Item }o--o| Subcollection : "subcollection"

    Tray }o--o| Owner : "owned by"
    Tray }o--o| SizeClass : "classified"
    Tray }o--o| MediaType : "media type"
    Tray }o--o| ConveyanceBin : "staged in"

    NonTrayItem }o--o| Owner : "owned by"
    NonTrayItem }o--o| SizeClass : "classified"
    NonTrayItem }o--o| MediaType : "media type"

    Barcode {
        uuid id PK
        varchar value UK
        bool withdrawn
        int type_id FK
    }
    BarcodeType {
        int id PK
        varchar name
    }
    Item {
        bigint id PK
        uuid barcode_id FK
        uuid withdrawn_barcode_id FK
        enum status
        int tray_id FK
        int owner_id FK
        int size_class_id FK
        int media_type_id FK
        int container_type_id FK
        int subcollection_id FK
        int shipping_bin_id FK
        varchar title
        bool scanned_for_accession
        bool scanned_for_verification
        bool scanned_for_shipping
        enum ils_sync_state
    }
    Tray {
        bigint id PK
        uuid barcode_id FK
        uuid withdrawn_barcode_id FK
        int shelf_position_id FK
        int owner_id FK
        int size_class_id FK
        int media_type_id FK
        int conveyance_bin_id FK
        bool scanned_for_accession
        bool scanned_for_verification
        bool scanned_for_shelving
    }
    NonTrayItem {
        bigint id PK
        uuid barcode_id FK
        uuid withdrawn_barcode_id FK
        enum status
        int shelf_position_id FK
        int owner_id FK
        int size_class_id FK
        int media_type_id FK
        int shipping_bin_id FK
        bool scanned_for_accession
        bool scanned_for_verification
        bool scanned_for_shelving
        enum ils_sync_state
    }
    ConveyanceBin {
        int id PK
        varchar barcode
    }
    Subcollection {
        int id PK
        varchar name
    }
```

### 5c. Workflow Jobs

The seven core workflows each have a Job model. Jobs are assigned to Users and track status, run time, and transitions. Many-to-many link tables connect Items/Trays to Refile and Withdraw jobs.

```mermaid
erDiagram
    User ||--o{ AccessionJob : "assigned to"
    User ||--o{ VerificationJob : "assigned to"
    User ||--o{ ShelvingJob : "assigned to"
    User ||--o{ PickList : "assigned to"
    User ||--o{ ShippingJob : "assigned to"
    User ||--o{ RefileJob : "assigned to"
    User ||--o{ WithdrawJob : "assigned to"

    AccessionJob ||--o{ Tray : "accessions"
    AccessionJob ||--o{ Item : "accessions"
    AccessionJob ||--o{ NonTrayItem : "accessions"
    AccessionJob ||--o{ VerificationJob : "creates"
    AccessionJob }o--|| Workflow : "uses workflow"

    VerificationJob ||--o{ Tray : "verifies"
    VerificationJob ||--o{ Item : "verifies"
    VerificationJob ||--o{ NonTrayItem : "verifies"

    ShelvingJob ||--o{ Tray : "shelves"
    ShelvingJob ||--o{ NonTrayItem : "shelves"
    ShelvingJob }o--|| Building : "in fetch-building"
    ShelvingJob ||--o{ ShelvingJobContainer : "contains"
    ShelvingJob ||--o{ ShelvingJobDiscrepancy : "logs"

    Request }o--o| Item : "requests"
    Request }o--o| NonTrayItem : "requests"
    Request }o--o| PickList : "grouped into"

    PickList ||--o{ Request : "fulfills"
    PickList }o--o| Building : "in fetch-building"
    PickList ||--o{ WithdrawJob : "triggers"

    ShippingJob ||--o{ ShippingBin : "contains"
    ShippingBin ||--o{ Item : "ships"
    ShippingBin ||--o{ NonTrayItem : "ships"
    ShippingBin }o--o| DeliveryLocation : "delivers to"

    RefileJob }o--o{ Item : "refiles (M2M)"
    RefileJob }o--o{ NonTrayItem : "refiles (M2M)"

    WithdrawJob }o--o{ Item : "withdraws (M2M)"
    WithdrawJob }o--o{ NonTrayItem : "withdraws (M2M)"
    WithdrawJob }o--o{ Tray : "withdraws (M2M)"

    AccessionJob {
        bigint id PK
        int workflow_id FK
        int assigned_user_id FK
        int created_by_id FK
        int owner_id FK
        int media_type_id FK
        int size_class_id FK
        int container_type_id FK
        bool trayed
        enum status
        interval run_time
    }
    VerificationJob {
        bigint id PK
        int accession_job_id FK
        int assigned_user_id FK
        int created_by_id FK
        enum status
        interval run_time
    }
    ShelvingJob {
        int id PK
        int fetch-building_id FK
        int assigned_user_id FK
        int created_by_id FK
        enum status
        enum origin
        enum mode
        interval run_time
    }
    PickList {
        bigint id PK
        int fetch-building_id FK
        int assigned_user_id FK
        int created_by_id FK
        enum status
        interval run_time
    }
    ShippingJob {
        bigint id PK
        int assigned_user_id FK
        int created_by_id FK
        enum status
        interval run_time
    }
    ShippingBin {
        bigint id PK
        int shipping_job_id FK
        int delivery_location_id FK
        varchar barcode
        enum status
    }
    RefileJob {
        smallint id PK
        int assigned_user_id FK
        int created_by_id FK
        enum status
        interval run_time
    }
    WithdrawJob {
        bigint id PK
        int assigned_user_id FK
        int created_by_id FK
        int pick_list_id FK
        enum status
        interval run_time
    }
    Request {
        bigint id PK
        int item_id FK
        int non_tray_item_id FK
        int pick_list_id FK
        int fetch-building_id FK
        int request_type_id FK
        int delivery_location_id FK
        int priority_id FK
        int requested_by_id FK
        enum status
        bool fulfilled
    }
    Workflow {
        int id PK
        varchar name
    }
```

### 5d. Authentication & RBAC

Users belong to Groups (many-to-many). Groups hold Permissions (many-to-many). Route-level access control checks the user's flattened permission set.

```mermaid
erDiagram
    User }o--o{ Group : "belongs to (M2M via user_groups)"
    Group }o--o{ Permission : "has (M2M via group_permissions)"
    User }o--o| Building : "default fetch-building"

    User {
        int id PK
        varchar first_name
        varchar last_name
        varchar email UK
        varchar fetch_auth_token
        timestamp fetch_auth_expiration
        int default_fetch-building_id FK
    }
    Group {
        smallint id PK
        varchar name UK
    }
    Permission {
        int id PK
        varchar name UK
    }
    UserGroup {
        int id PK
        int user_id FK
        int group_id FK
    }
    GroupPermission {
        int id PK
        int group_id FK
        int permission_id FK
    }
```

### 5e. Configuration & ILS Integration

Lookup tables that parameterize the system, plus the ILS adapter configuration that enables FOLIO integration per-Owner.

```mermaid
erDiagram
    Owner }o--o| OwnerTier : "tier"
    Owner }o--o| Owner : "parent (self-ref)"
    Owner }o--o| ILSConfiguration : "ILS config"
    Owner ||--o{ OwnerDeliveryLocation : "delivery locations"
    OwnerDeliveryLocation }o--|| DeliveryLocation : "location"
    ILSConfiguration ||--o{ ILSSyncError : "logs errors"

    Owner {
        smallint id PK
        int parent_owner_id FK
        uuid ils_configuration_id FK
        varchar name
        int owner_tier_id FK
    }
    OwnerTier {
        int id PK
        varchar name
    }
    ILSConfiguration {
        uuid id PK
        varchar name UK
        enum adapter_type
        varchar base_url
        varchar tenant_id
        varchar auth_client_id
        varchar auth_client_secret
        bool is_active
        bool enable_accession_hook
        bool enable_shelving_hook
        bool enable_refile_hook
        bool enable_requests_hook
        bool enable_jit_metadata_hook
        bool enable_picklist_hook
    }
    ILSSyncError {
        int id PK
        uuid ils_configuration_id FK
        varchar error_type
        text error_message
    }
    SizeClass {
        int id PK
        varchar name
    }
    MediaType {
        int id PK
        varchar name
    }
    ContainerType {
        int id PK
        varchar name
    }
    DeliveryLocation {
        int id PK
        varchar name
    }
    Priority {
        int id PK
        varchar name
    }
    RequestType {
        int id PK
        varchar name
    }
    OwnerDeliveryLocation {
        int id PK
        int owner_id FK
        int delivery_location_id FK
    }
    SystemSetting {
        int id PK
        varchar key UK
        varchar value
    }
    AuditTrail {
        bigint id PK
        varchar table_name
        varchar action
        jsonb old_values
        jsonb new_values
    }
    BatchUpload {
        bigint id PK
        int user_id FK
        varchar status
    }
```

### 5f. Many-to-Many Link Tables

These association tables power the M2M relationships between workflow jobs and inventory objects.

```mermaid
erDiagram
    RefileItemTable {
        bigint id PK
        int item_id FK
        smallint refile_job_id FK
    }
    RefileNonTrayItemTable {
        bigint id PK
        int non_tray_item_id FK
        smallint refile_job_id FK
    }
    ItemWithdrawalTable {
        bigint id PK
        int item_id FK
        bigint withdraw_job_id FK
    }
    NonTrayItemWithdrawalTable {
        bigint id PK
        int non_tray_item_id FK
        bigint withdraw_job_id FK
    }
    TrayWithdrawalTable {
        bigint id PK
        int tray_id FK
        bigint withdraw_job_id FK
    }
```

---

## Cross-Reference: Domain Connections

The diagram below shows how the five domain groups connect to each other at a high level.

```mermaid
graph LR
    LOC["🏢 Location Hierarchy<br/>(Diagram 5a)"]
    INV["📦 Inventory Objects<br/>(Diagram 5b)"]
    JOB["⚙️ Workflow Jobs<br/>(Diagram 5c)"]
    RBAC["🔐 Auth & RBAC<br/>(Diagram 5d)"]
    CFG["⚙️ Configuration & ILS<br/>(Diagram 5e)"]

    INV -->|"ShelfPosition<br/>(placement)"| LOC
    JOB -->|"assigned_user_id<br/>created_by_id"| RBAC
    JOB -->|"Items, Trays,<br/>NonTrayItems"| INV
    JOB -->|"fetch-building_id"| LOC
    INV -->|"owner_id, size_class_id,<br/>media_type_id"| CFG
    LOC -->|"owner_id,<br/>shelf_type_id"| CFG
    CFG -->|"ILS hooks per Owner"| JOB

    style LOC fill:#1e293b,stroke:#10b981,color:#e2e8f0
    style INV fill:#1e293b,stroke:#3b82f6,color:#e2e8f0
    style JOB fill:#1e293b,stroke:#f59e0b,color:#e2e8f0
    style RBAC fill:#1e293b,stroke:#ef4444,color:#e2e8f0
    style CFG fill:#1e293b,stroke:#8b5cf6,color:#e2e8f0
```

