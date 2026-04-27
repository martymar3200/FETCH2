# Frontend Development Guide

This guide covers the conventions, patterns, and design system used in the FETCH2 Vue/Quasar PWA frontend. Read this before writing any new components or pages.

---

## Table of Contents

1. [Tech Stack](#1-tech-stack)
2. [Project Structure](#2-project-structure)
3. [Design System & Styling](#3-design-system--styling)
4. [Base Components](#4-base-components)
5. [Composables](#5-composables)
6. [Pinia Store Conventions](#6-pinia-store-conventions)
7. [Routing & Navigation Guards](#7-routing--navigation-guards)
8. [Adding a New Feature (End-to-End)](#8-adding-a-new-feature-end-to-end)
9. [Global Utilities (Provide/Inject)](#9-global-utilities-provideinject)
10. [Environment Configuration](#10-environment-configuration)

---

## 1. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Framework | Vue 3 | Composition API (`<script setup>`) |
| UI Library | Quasar | v2 (Vite mode) |
| State | Pinia | Options API style stores |
| HTTP | Axios | Configured via boot file |
| Build | Quasar CLI (Vite) | PWA mode (`injectManifest`) |
| Styling | SCSS | Quasar variables + custom layers |
| Font | Inter | Google Fonts via CSS |
| Icons | Material Icons + MDI v7 | Via Quasar extras |

---

## 2. Project Structure

```
vue/
├── env/                          # Environment config
│   └── envparser.js
├── src/
│   ├── boot/                     # App initialization plugins
│   │   ├── axios.js              # Axios instance + interceptors
│   │   ├── notify-defaults.js    # Error audio alerts on negative notifications
│   │   ├── htmlToPaper.js        # Print support
│   │   └── vueJsonExcel.js       # Excel export
│   ├── components/               # Reusable components (grouped by domain)
│   │   ├── Base/                 # Design system primitives
│   │   │   └── BaseButton.vue
│   │   ├── Accession/
│   │   ├── Admin/
│   │   ├── Picklist/
│   │   ├── Refile/
│   │   ├── Reports/
│   │   ├── Request/
│   │   ├── Job/                  # Shared job-related components
│   │   ├── EssentialTable.vue    # Core data table wrapper
│   │   ├── NavigationBar.vue     # Header + offline banners
│   │   ├── PopupModal.vue        # Standard modal wrapper
│   │   └── BreadCrumb.vue
│   ├── composables/              # Shared logic (Vue composables)
│   ├── css/                      # Global SCSS design system
│   │   ├── base/                 # Reset, typography, mixins, utilities
│   │   ├── components/           # Quasar component overrides
│   │   ├── app.scss              # Entry point (imports all layers)
│   │   └── quasar.variables.scss # Design tokens (colors, breakpoints)
│   ├── http/
│   │   └── InventoryService.js   # API endpoint registry
│   ├── layouts/
│   │   └── MainLayout.vue        # Root layout + provide/inject globals
│   ├── pages/                    # Route-level page components
│   ├── router/
│   │   └── routes.js             # All route definitions
│   ├── stores/                   # Pinia stores (one per domain)
│   └── utils/
│       └── audio.js              # Error beep audio utility
├── src-pwa/                      # PWA service worker files
│   ├── custom-service-worker.js
│   ├── register-service-worker.js
│   └── manifest.json
└── quasar.config.js              # Build + PWA + dev server config
```

---

## 3. Design System & Styling

### Architecture

The SCSS is organized into layered imports via `app.scss`:

```scss
// app.scss — import order matters
@import "base/base";        // CSS reset
@import "base/mixins";      // Responsive breakpoint mixins
@import "base/typography";  // Heading/body responsive scaling
@import "base/utility";     // Color utilities, layout helpers, status badges

@import "components/form";     // .form-group, .form-group-label
@import "components/qbtn";    // q-btn overrides (.btn-modern, .btn-dashed)
@import "components/qdialog"; // q-dialog z-index fixes
@import "components/qtable";  // q-table styling (shadows, zebra, hover, status badges)
@import "components/table";   // .table-borderless for non-Quasar tables
```

### Design Tokens (`quasar.variables.scss`)

All colors are defined as SCSS variables and available globally in any `.vue` or `.scss` file:

| Token | Value | Usage |
|---|---|---|
| `$primary` | `#0f172a` (Slate 900) | Text, headers, nav background |
| `$secondary` | `#334155` (Slate 700) | Borders, dividers, subtle text |
| `$accent` | `#1d4ed8` (Blue 700) | Primary action buttons, links, active states |
| `$positive` | `#15803d` (Green 700) | Success states, confirmations |
| `$negative` | `#b91c1c` (Red 700) | Errors, destructive actions |
| `$warning` | `#F2C037` | Warnings, offline banner |
| `$sidebar-bg` | `#262626` (Carbon Grey) | Navigation drawer background |
| `$light-page` | `#f8fafc` (Slate 50) | Page background |
| `$color-gray-light` | `#f7f6f6` | Card backgrounds, subtle fills |

**Custom colors** (e.g., `$color-pink`, `$color-brown`, `$color-green-light`) are also available and auto-generate `.bg-*`, `.text-*`, `.text-highlight-*`, and `.border-*` utility classes via the `$color-map` loop in `utility.scss`.

### Responsive Breakpoints

Two breakpoint systems are available:

**Quasar defaults** (used in templates with `$q.screen`):

| Name | Range |
|---|---|
| `xs` | 0–599px |
| `sm` | 600–1023px |
| `md` | 1024–1439px |
| `lg` | 1440–1920px |
| `xl` | 1921px+ |

**Custom `@include respond()` mixin** (used in SCSS):

```scss
// Usage in component <style> blocks
@include respond(phone) { /* ≤767px */ }
@include respond(tablet) { /* ≤960px */ }
@include respond(laptop) { /* ≤1259px */ }
@include respond(desktop) { /* ≥1260px */ }
@include respond(desktop-lg) { /* ≥1500px */ }
```

### Typography

- **Font**: Inter (sans-serif), loaded via CSS
- **Base size**: `1rem` (16px desktop, 14px mobile via `html { font-size: 87.5% }` on small screens)
- **Headings**: Use Quasar's `text-h1` through `text-h6` classes (responsive overrides in `typography.scss`)
- **Custom**: `.text-subcaption` (0.65rem, for fine-print labels)

### Status Badges

Use the `.status-badge` class with modifiers for workflow statuses:

```html
<span class="status-badge status-badge--created">Created</span>
<span class="status-badge status-badge--running">Running</span>
<span class="status-badge status-badge--completed">Completed</span>
<span class="status-badge status-badge--paused">Paused</span>
<span class="status-badge status-failed">Failed</span>
```

Each badge uses a gradient background, rounded pill shape, and a subtle hover lift effect.

### Key Utility Classes

| Class | Effect |
|---|---|
| `.divider` | 1px horizontal separator line |
| `.outline` | Light gray bordered box |
| `.underline` | Bottom border |
| `.link` | Pointer cursor + underline on hover |
| `.text-nowrap` | Prevent text wrapping |
| `.overlay` | Semi-transparent overlay with loading spinner |
| `.flex-grow` / `.flex-shrink` | Responsive flex utilities (`-sm`, `-md`, `-lg`, `-xl`) |
| `.order-1` to `.order-10` | CSS order for flex items |
| `.form-group` / `.form-group-label` | Standard form layout |
| `.bg-accent-1` | Subtle accent-tinted background |

### Rules

> **DO**: Use `$accent` for primary action colors, `$primary` for text, `$negative` for errors.
>
> **DO**: Use the `.status-badge` classes for workflow statuses.
>
> **DO**: Use `<style lang="scss" scoped>` on components. Only use unscoped styles for global overrides.
>
> **DON'T**: Hardcode hex colors. Always reference `$variables`.
>
> **DON'T**: Create new global utility classes without adding them to `utility.scss`.

---

## 4. Base Components

### `BaseButton`

**Always use `BaseButton` instead of raw `<q-btn>`** for action buttons.

```html
<BaseButton variant="primary" label="Save" @click="save" />
<BaseButton variant="danger" label="Delete" @click="remove" />
<BaseButton variant="outline" label="Cancel" @click="cancel" />
<BaseButton variant="ghost" icon="edit" @click="edit" />
```

| Variant | Quasar Color | Visual |
|---|---|---|
| `primary` | `accent` (Blue 700) | Filled button with modern shadow + hover lift |
| `secondary` | `secondary` (Slate 700) | Filled button, subtle |
| `danger` | `negative` (Red 700) | Filled red with modern shadow |
| `outline` | Transparent + border | Outlined with primary text |
| `ghost` | Transparent | Text-only, no border |

All variants have `no-caps` enabled by default (no uppercase text).

### `EssentialTable`

The standard data table wrapper for all list views. Wraps Quasar's `q-table` with:

- Built-in **column rearranging** (drag-and-drop)
- Built-in **filter menu** (checkbox-based filtering)
- Server-side **pagination** support
- Row **selection** mode
- Row **highlighting** by key/value

```html
<EssentialTable
  :table-columns="columns"
  :table-data="rows"
  :enable-pagination="true"
  :pagination-total="totalCount"
  @update-pagination="fetchPage"
  @selected-table-row="onRowClick"
>
  <template #heading-row>
    <h5>My Table Title</h5>
  </template>
  <template #table-td="{ props, colName, value }">
    <span v-if="colName === 'status'" class="status-badge" :class="`status-badge--${value.toLowerCase()}`">
      {{ value }}
    </span>
    <span v-else>{{ value }}</span>
  </template>
</EssentialTable>
```

### `PopupModal`

Standard modal wrapper used across all workflows:

```html
<PopupModal
  title="Confirm Action"
  text="Are you sure?"
  @confirmed="handleConfirm"
/>
```

---

## 5. Composables

All composables live in `src/composables/` and follow the `use*` naming convention.

| Composable | Purpose | Key Exports |
|---|---|---|
| `useBarcodeScanHandler` | Captures barcode scanner keypresses globally | `compiledBarCode` (ref) — the completed barcode string |
| `useScanQueue` | Queues rapid barcode scans and processes sequentially | `enqueue(barcode)`, `isProcessing`, `queueLength` |
| `useOfflineSync` | Offline operation queue + replay engine | `offlineAwareRequest()`, `syncPendingOps()`, `pendingOpsCount` |
| `useIndexDbHandler` | IndexedDB CRUD wrapper | `getDataInIndexDb()`, `addDataToIndexDb()`, `deleteDataInIndexDb()` |
| `usePermissionHandler` | Check user permissions | `checkUserPermission(permString)` → boolean |
| `useCurrentScreenSize` | Reactive screen size label | `currentScreenSize` → `'xs'`, `'sm'`, `'md'`, `'lg'`, `'xl'` |
| `useScrollPosition` | Track scroll position | Scroll-related refs |
| `useFileSystemAccessHandler` | File system API access | File read/write helpers |
| `useBackgroundSyncHandler` | Legacy Workbox background sync | `triggerBackgroundSync()`, `bgSyncData` |

### Barcode Scanning Pattern

Barcode scanning is the most critical frontend pattern. Here's how to use it in a page:

```javascript
// In your page component
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useScanQueue } from '@/composables/useScanQueue.js'

const { compiledBarCode } = useBarcodeScanHandler()
const { enqueue } = useScanQueue(processScan)

// Watch for completed barcode scans
watch(compiledBarCode, (barcode) => {
  if (barcode) {
    enqueue(barcode)  // Queue it — prevents rapid-fire scan issues
  }
})

async function processScan(barcodeValue) {
  // Your scan logic here (API calls, store mutations, etc.)
}
```

**Two scan modes** are available:
- **Debounce mode** (default): Waits for a configurable pause after the last keystroke
- **Enter key mode** (`{ waitForEnterKey: true }`): Waits for Enter key (some scanners send this)

> **Important**: The handler ignores keypresses when an `<input>` or `<textarea>` is focused to prevent double-entry.

---

## 6. Pinia Store Conventions

### Plugin: `$api` and `apiPageSizeDefault`

All stores have access to `this.$api` (the Axios instance) and `this.apiPageSizeDefault` (50) via a Pinia plugin defined in `stores/index.js`:

```javascript
pinia.use(({ store }) => {
  store.$api = api
  store.apiPageSizeDefault = 50
})
```

### Store Pattern

Every domain store follows this structure:

```javascript
import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useOfflineSync } from '@/composables/useOfflineSync.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

export const useExampleStore = defineStore('example-store', {
  state: () => ({
    exampleList: [],
    exampleListTotal: 0,
    exampleItem: { id: null, name: '' },
    originalExampleItem: null  // Used for dirty-checking
  }),

  getters: {
    // Derived computed state
    isComplete: (state) => state.exampleItem.status === 'Complete'
  },

  actions: {
    // Reset
    resetExampleStore() { this.$reset() },

    // GET list (paginated)
    async getExampleList(qParams) {
      const res = await this.$api.get(inventoryServiceApi.examples, {
        params: { size: this.apiPageSizeDefault, ...qParams }
      })
      this.exampleList = res.data.items
      this.exampleListTotal = res.data.total
    },

    // GET single
    async getExample(id) {
      const res = await this.$api.get(`${inventoryServiceApi.examples}${id}`)
      this.exampleItem = res.data
      this.originalExampleItem = { ...this.exampleItem }
    },

    // PATCH (with offline support)
    async patchExample(payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      const res = await offlineAwareRequest({
        method: 'PATCH',
        url: `${inventoryServiceApi.examples}${payload.id}`,
        payload,
        optimisticUpdate: () => {
          if (payload.status) this.exampleItem.status = payload.status
        },
        updateSnapshot: async () => {
          await addDataToIndexDb('exampleStore', 'exampleItem',
            JSON.parse(JSON.stringify(this.exampleItem)))
        }
      })
      if (res.fromServer) {
        this.exampleItem = res.data
        this.originalExampleItem = { ...res.data }
      }
    }
  }
})
```

### Key Conventions

- **API endpoints** are referenced from `InventoryService.js`, never hardcoded
- **`originalExampleItem`** is always set alongside the main item (used for dirty-checking / unsaved changes detection)
- **Offline-capable actions** use `offlineAwareRequest()` with `optimisticUpdate` and `updateSnapshot` callbacks
- **Error handling**: Actions `throw error` — the calling component is responsible for catching and showing `Notify.create()` messages
- **Reset actions**: Every store has a `resetStore()` method that calls `this.$reset()`

### API Endpoint Registry

All API paths are centralized in `src/http/InventoryService.js`:

```javascript
export default {
  accessionJobs: '/accession-jobs/',
  buildings: '/buildings/',
  items: '/items/',
  itemsBarcode: '/items/barcode/',
  shelvingJobs: '/shelving-jobs/',
  picklists: '/pick-lists/',
  refileJobs: '/refile-jobs/',
  // ... ~80 endpoints total
}
```

> **Rule**: Never put API paths directly in store actions or components. Always add them to `InventoryService.js` first.

---

## 7. Routing & Navigation Guards

### Route Definition Pattern

All routes are children of the `MainLayout` component and use lazy imports:

```javascript
{
  name: 'my-feature',
  path: 'my-feature/:jobId?',
  component: () => import('@/pages/MyFeaturePage.vue'),
  meta: {
    requiresAuth: true,
    requiresPerm: 'can_access_my_feature'
  }
}
```

### Route Meta Fields

| Meta Field | Purpose |
|---|---|
| `requiresAuth: true` | Redirects unauthenticated users to login |
| `requiresPerm: 'perm_string'` | Checks `userData.permissions` array. If missing, shows "no permission" alert |

### Navigation Guards

- **Auth guard**: Checked in the global `beforeEach` router hook
- **Permission guard**: Routes with `requiresPerm` are validated against the user's permissions. Failed checks set `globalStore.appRouteGuard` which triggers a notification in `NavigationBar.vue`
- **Sync guard**: When `pendingOpsCount > 0`, navigating away shows a confirmation modal ("You have pending requests. Are you sure you want to leave?")

---

## 8. Adding a New Feature (End-to-End)

Here's the step-by-step recipe for adding a new workflow module (e.g., "Audit"):

### Step 1: Add API Endpoints

```javascript
// src/http/InventoryService.js
auditJobs: '/audit-jobs/',
auditJobsBarcode: '/audit-jobs/barcode/',
```

### Step 2: Create the Pinia Store

```
src/stores/audit-store.js
```

Follow the [store pattern](#store-pattern) above. Include `resetAuditStore()`, list/get/patch actions, and offline support where needed.

### Step 3: Create the Page Component

```
src/pages/AuditPage.vue
```

```html
<template>
  <q-page :style-fn="handlePageOffset" class="q-pa-md">
    <!-- Your page content -->
  </q-page>
</template>

<script setup>
import { inject } from 'vue'
const handlePageOffset = inject('handle-page-offset')
</script>
```

> **Important**: Always use `inject('handle-page-offset')` and pass it to `q-page`'s `:style-fn`. This calculates the correct page height accounting for the nav bar + breadcrumb.

### Step 4: Add the Route

```javascript
// src/router/routes.js (inside the MainLayout children array)
{
  name: 'audit',
  path: 'audit/:jobId?',
  component: () => import('@/pages/AuditPage.vue'),
  meta: {
    requiresAuth: true,
    requiresPerm: 'can_access_audit'
  }
}
```

### Step 5: Add to Navigation

```javascript
// src/components/NavigationBar.vue — essentialLinks computed
{
  title: 'Audit',
  icon: 'fact_check',
  link: '/audit',
  requiresPerm: 'can_access_audit'
}
```

### Step 6: Add Domain Components

```
src/components/Audit/
├── AuditJobList.vue      # Table view using EssentialTable
├── AuditJobDetail.vue    # Single job view
└── AuditScanner.vue      # Barcode scanning component
```

---

## 9. Global Utilities (Provide/Inject)

`MainLayout.vue` provides several utility functions globally via Vue's `provide/inject` system:

| Inject Key | Function | Usage |
|---|---|---|
| `handle-page-offset` | `handlePageOffset()` | Pass to `q-page :style-fn` for correct min-height |
| `format-date-time` | `formatDateTime(isoString)` | Returns `{ date, time, dateTime }` in local format |
| `get-item-location` | `getItemLocation(itemData)` | Formats shelf position as `Module-Aisle-Side-Ladder-Shelf-Position` |
| `current-iso-date` | `currentIsoDate()` | Returns timezone-aware ISO string |
| `render-item-barcode-display` | `renderItemBarcodeDisplay(item)` | Shows `withdrawn_barcode` if present, else `barcode` |
| `handle-csv-download` | `handleCSVDownload(data, name)` | Triggers a CSV file download |
| `get-nested-key-path` | `getNestedKeyPath(obj, 'a.b.c')` | Safe deep property access |
| `get-uniqure-list-by-key` | `getUniqueListByKey(arr, key)` | Deduplicates an array of objects |

**Usage in any child component:**

```javascript
const formatDateTime = inject('format-date-time')
const { date, time } = formatDateTime(item.create_dt)
```

---

## 10. Environment Configuration

### Environment Variables

| Variable | Purpose | Example |
|---|---|---|
| `VITE_ENV` | Environment name | `local`, `dev`, `production` |
| `VITE_BASE_URL` | Public path for the PWA | `/` |
| `VITE_API_BASE_URI` | SSO/auth service URL | `https://localhost:8000/` |
| `VITE_INV_SERVCE_API` | Backend API base URL | `https://localhost:8001/` |

### Axios Configuration

The Axios instance (`boot/axios.js`) is configured with:

- **`baseURL`**: Set to `VITE_INV_SERVCE_API`
- **`withCredentials: true`**: Cookies sent with all requests (SAML session)
- **Request interceptor**: Passthrough (browser handles cookies)
- **Response interceptor**: Auto-logout on 401 (unless `appPendingSync` is true — protects offline queue presentation)
- **Params serializer**: Arrays are expanded (`owner_id: [1,2]` → `owner_id=1&owner_id=2`), nulls are stripped

### Notification Defaults

The `notify-defaults.js` boot file monkey-patches `Notify.create()` to play an **audio alert beep** on any notification with `type: 'negative'` or `color: 'negative'`. This ensures staff on the warehouse floor hear error alerts even when not looking at the screen.

---

## File Reference

| File | Purpose |
|---|---|
| [`quasar.variables.scss`](vue/src/css/quasar.variables.scss) | Design tokens (colors, breakpoints, typography) |
| [`app.scss`](vue/src/css/app.scss) | SCSS entry point (imports all layers) |
| [`utility.scss`](vue/src/css/base/utility.scss) | Auto-generated color classes, layout utilities, status badges |
| [`mixins.scss`](vue/src/css/base/mixins.scss) | `@include respond()` breakpoint mixin |
| [`qbtn.scss`](vue/src/css/components/qbtn.scss) | `.btn-modern`, `.btn-dashed` button styles |
| [`qtable.scss`](vue/src/css/components/qtable.scss) | Table shadows, zebra striping, status badge system |
| [`BaseButton.vue`](vue/src/components/Base/BaseButton.vue) | Variant-based button component |
| [`EssentialTable.vue`](vue/src/components/EssentialTable.vue) | Data table with filter/rearrange/pagination |
| [`NavigationBar.vue`](vue/src/components/NavigationBar.vue) | Header, offline banners, nav drawer, sync trigger |
| [`MainLayout.vue`](vue/src/layouts/MainLayout.vue) | Root layout + global provide/inject utilities |
| [`axios.js`](vue/src/boot/axios.js) | Axios instance + auth interceptors |
| [`InventoryService.js`](vue/src/http/InventoryService.js) | Centralized API endpoint registry |
| [`routes.js`](vue/src/router/routes.js) | Route definitions with auth/permission meta |
| [`index.js`](vue/src/stores/index.js) | Pinia plugin (`$api`, `apiPageSizeDefault`) |
| [`useBarcodeScanHandler.js`](vue/src/composables/useBarcodeScanHandler.js) | Global barcode scanner keypress handler |
| [`useScanQueue.js`](vue/src/composables/useScanQueue.js) | Sequential scan processing queue |
| [`useOfflineSync.js`](vue/src/composables/useOfflineSync.js) | Offline operation queue + replay engine |
| [`usePermissionHandler.js`](vue/src/composables/usePermissionHandler.js) | Permission checking composable |
