# Technical Backlog

Technical debt and cleanup items identified during the frontend and backend architecture audits (April 2026). Items are prioritized by severity and grouped by category.

---

## 🔴 Bugs

These are incorrect behaviors that should be fixed in the next sprint.

### BUG-1: `.flex-xl-shrink` sets wrong CSS property

- **File**: [`utility.scss:166`](vue/src/css/base/utility.scss)
- **Priority**: High
- **Effort**: 5 min

**Problem**: The `.flex-xl-shrink` class sets `flex-grow: 1` instead of `flex-shrink: 1`. This is a copy-paste error from the `.flex-xl-grow` class above it.

```diff
 .flex-xl-shrink {
   @media (min-width: $breakpoint-xl-min) {
-    flex-grow: 1 !important;
+    flex-shrink: 1 !important;
   }
 }
```

---

### BUG-2: Typo in global provide key

- **File**: [`MainLayout.vue:165`](vue/src/layouts/MainLayout.vue)
- **Priority**: High
- **Effort**: 30 min (requires updating all inject sites)

**Problem**: The provide key is `'get-uniqure-list-by-key'` (typo: "uniqure") instead of `'get-unique-list-by-key'`.

**Impact**: Renaming requires a simultaneous update of every `inject('get-uniqure-list-by-key')` call in the codebase to avoid a runtime break. Search all `.vue` files for the typo before renaming.

```diff
- provide('get-uniqure-list-by-key', getUniqueListByKey)
+ provide('get-unique-list-by-key', getUniqueListByKey)
```

---

### BUG-3: `eval()` usage in EssentialTable filter logic

- **File**: [`EssentialTable.vue:572`](vue/src/components/EssentialTable.vue)
- **Priority**: High
- **Effort**: 1–2 hours

**Problem**: The table filter logic uses `eval(field)` to convert arrow function strings (e.g., `"row => row.barcode.value"`) into executable functions. This is a security anti-pattern — if filter option `field` values ever originate from user input or an API response, it becomes an XSS vector.

**Current code**:
```javascript
if (field.includes('=>')) {
  const fieldArrowFunc = eval(field)
  return val.includes(fieldArrowFunc(entry))
}
```

**Recommendation**: Refactor `filterOptions` to accept actual function references instead of arrow function strings:

```javascript
// Instead of: { field: 'row => row.barcode.value', ... }
// Use:        { field: (row) => row.barcode.value, ... }
```

Then the filter logic becomes:
```javascript
if (typeof field === 'function') {
  return val.includes(field(entry))
}
```

This requires updating all `filterOptions` definitions across components that pass string-based field accessors.

---

### BUG-4: Delete endpoints return `HTTPException` instead of `Response`

- **File**: [`buildings.py:172`](inventory_service/app/routers/buildings.py) (and likely other routers)
- **Priority**: High
- **Effort**: 1 hour (audit all routers)

**Problem**: Delete endpoints use `return HTTPException(status_code=204, ...)` instead of raising it or returning a proper `Response`. Returning an `HTTPException` object doesn't send a 204 — FastAPI serializes the exception as JSON and returns it with a **200 OK** status.

```diff
- return HTTPException(
-     status_code=204, detail=f"Building ID {id} Deleted Successfully"
- )
+ from fastapi.responses import Response
+ return Response(status_code=204)
```

**Action**:
- [ ] Search all routers for `return HTTPException(status_code=204` and replace with `return Response(status_code=204)`
- [ ] Verify frontend handles 204 (no-content) responses correctly

---

### BUG-5: Unconditional `import debugpy` in production code

- **File**: [`events.py:3`](inventory_service/app/events.py)
- **Priority**: High
- **Effort**: 5 min

**Problem**: `events.py` has `import debugpy` at the top level with no guard. If `debugpy` is not installed in the production Docker image, the entire events module fails to import, which disables all SQLAlchemy ORM event listeners (shelving discrepancy checks, shelf space tracking).

```diff
- import debugpy
```

Simply remove the import — it is not used anywhere in the file.

---

## 🟡 Technical Debt

Items that aren't broken but add maintenance burden, confusion, or inconsistency.

### DEBT-1: Remove legacy `useBackgroundSyncHandler` composable

- **File**: [`useBackgroundSyncHandler.js`](vue/src/composables/useBackgroundSyncHandler.js)
- **Priority**: Medium
- **Effort**: 30 min

**Problem**: This composable directly accesses the `workbox-background-sync` IndexedDB and provides a `triggerBackgroundSync()` function that posts a message to the service worker. Its functionality has been fully superseded by `useOfflineSync.js`, which uses a dedicated `offline-operations` IndexedDB store with optimistic updates and sequential replay.

**Usage**: Only referenced in `TestPage.vue`. Not used in any production workflow.

**Action**:
- [ ] Verify no production components import `useBackgroundSyncHandler`
- [ ] Remove the composable file
- [ ] Update `TestPage.vue` to use `useOfflineSync` if the test page is retained

---

### DEBT-2: Remove scaffold/example files

- **Files**:
  - [`example-store.js`](vue/src/stores/example-store.js)
  - [`src/pages/example/`](vue/src/pages/example/)
- **Priority**: Medium
- **Effort**: 15 min

**Problem**: Template/scaffold files from the initial project setup are still shipped in production builds. They add to bundle size and create confusion for new developers who might think they're active features.

**Action**:
- [ ] Delete `example-store.js`
- [ ] Delete the `src/pages/example/` directory
- [ ] Remove any related routes from `routes.js`

---

### DEBT-3: Gate or remove `TestPage.vue`

- **File**: [`TestPage.vue`](vue/src/pages/TestPage.vue)
- **Priority**: Medium
- **Effort**: 15 min

**Problem**: A developer debug/test page with offline testing, IndexedDB inspection, and owner tier caching experiments. It is still routable in production via its route definition.

**Action** (choose one):
- [ ] **Option A**: Remove the route from `routes.js` and delete the page
- [ ] **Option B**: Gate the route behind an environment check:
  ```javascript
  ...(process.env.VITE_ENV !== 'production' ? [{
    name: 'test',
    path: 'test',
    component: () => import('@/pages/TestPage.vue')
  }] : [])
  ```

---

### DEBT-4: Refactor provide/inject globals into composables

- **File**: [`MainLayout.vue`](vue/src/layouts/MainLayout.vue)
- **Priority**: Medium
- **Effort**: 3–4 hours

**Problem**: `MainLayout.vue` provides ~10 utility functions via Vue's `provide/inject` API. These are:
- Invisible to IDE autocomplete and TypeScript
- Not testable in isolation
- Easy to misspell (see BUG-2)
- Tightly coupled to the layout component

**Current provide keys**:
| Key | Function |
|---|---|
| `handle-page-offset` | `handlePageOffset()` |
| `format-date-time` | `formatDateTime()` |
| `get-item-location` | `getItemLocation()` |
| `current-iso-date` | `currentIsoDate()` |
| `render-item-barcode-display` | `renderItemBarcodeDisplay()` |
| `render-withdrawn-tray-barcode` | `renderWithdrawnTrayBarcode()` |
| `render-withdrawn-shelf-barcode` | `renderWithdrawnShelfBarcode()` |
| `render-withdrawn-item-location` | `renderWithdrawnItemLocation()` |
| `handle-csv-download` | `handleCSVDownload()` |
| `get-uniqure-list-by-key` | `getUniqueListByKey()` |
| `get-nested-key-path` | `getNestedKeyPath()` |

**Recommended refactoring**:

| New Composable | Functions to move |
|---|---|
| `useFormatters()` | `formatDateTime`, `currentIsoDate`, `getNestedKeyPath`, `getUniqueListByKey` |
| `useItemDisplay()` | `getItemLocation`, `renderItemBarcodeDisplay`, `renderWithdrawnTrayBarcode`, `renderWithdrawnShelfBarcode`, `renderWithdrawnItemLocation` |
| `useDownloadHandler()` | `handleCSVDownload` |

`handlePageOffset` should remain as a provide/inject since it depends on the layout's DOM refs. All others are pure functions with no DOM dependency.

---

### DEBT-5: Standardize breakpoint system

- **Files**:
  - [`quasar.variables.scss`](vue/src/css/quasar.variables.scss)
  - [`mixins.scss`](vue/src/css/base/mixins.scss)
- **Priority**: Low
- **Effort**: 2–3 hours

**Problem**: Two breakpoint systems exist with **different pixel values**:

| Breakpoint | Quasar (`$breakpoint-*`) | Custom Mixin (`@include respond()`) |
|---|---|---|
| Small/Phone | `xs`: 0–599px | `phone`: 0–767px |
| Medium/Tablet | `sm`: 600–1023px | `tablet`: 0–960px |
| Large/Laptop | `md`: 1024–1439px | `laptop`: 0–1259px |
| Desktop | `lg`: 1440–1920px | `desktop`: 1260px+ |

Developers have to know which system is used in each file, and the differing ranges can cause UI inconsistencies at edge screen widths.

**Recommendation**: Deprecate the custom mixin and migrate to Quasar's `$breakpoint-*-min` / `$breakpoint-*-max` SCSS variables, which are automatically available in all `.vue` and `.scss` files:

```scss
// Instead of: @include respond(phone) { ... }
// Use:        @media (max-width: $breakpoint-xs-max) { ... }
```

---

### DEBT-6: Expand Base component library

- **Directory**: [`vue/src/components/Base/`](vue/src/components/Base/)
- **Priority**: Low
- **Effort**: 4–6 hours (incremental)

**Problem**: Only `BaseButton` exists as a design-system primitive. Quasar's `q-input`, `q-select`, and `q-card` are used directly throughout the app with ad-hoc prop/style configurations. This leads to inconsistent:
- Input label formatting
- Select dropdown sizing
- Card border radius and shadow depth
- Validation error display

**Recommended additions** (prioritized by usage frequency):

| Component | Wraps | Standardizes |
|---|---|---|
| `BaseInput.vue` | `q-input` | Label style, dense mode on mobile, outlined variant, error message display |
| `BaseSelect.vue` | `q-select` | Label style, filter behavior, mobile popup behavior |
| `BaseCard.vue` | `q-card` | Border radius (12px), shadow depth, padding |
| `BaseChip.vue` | `q-chip` | Status-colored chip variants |

Each component should use `v-bind="$attrs"` pass-through (like `BaseButton` does) so all native Quasar props remain available.

---

### DEBT-7: Rename misspelled `middlware.py`

- **File**: [`middlware.py`](inventory_service/app/middlware.py)
- **Priority**: Medium
- **Effort**: 15 min

**Problem**: The middleware filename is misspelled as `middlware.py` (missing "e"). Every import in the codebase references the misspelled name (`from app.middlware import JWTMiddleware`). This is confusing for new developers.

**Action**:
- [ ] Rename `middlware.py` → `middleware.py`
- [ ] Update all imports (search for `from app.middlware`)
- [ ] Verify the app starts correctly after renaming

---

### DEBT-8: Remove redundant session factory functions

- **File**: [`session.py:75-87`](inventory_service/app/database/session.py)
- **Priority**: Medium
- **Effort**: 30 min

**Problem**: Three session factory functions are identical one-liners that all return `sa_hybrid_session_local()`:

```python
def get_sqlalchemy_session_thread_safe():
    return sa_hybrid_session_local()

def get_sqlalchemy_session_for_item_migration():
    return sa_hybrid_session_local()

def get_sqlalchemy_session_for_storage_migration():
    return sa_hybrid_session_local()
```

These are legacy artifacts from the data migration scripts. They add confusion since developers may think they have distinct behavior.

**Action**:
- [ ] Search for all call sites of these three functions
- [ ] Replace with a single `get_sqlalchemy_session_thread_safe()` (or inline `sa_hybrid_session_local()`)
- [ ] Remove the two redundant functions

---

### DEBT-9: Remove debug/backup files from app directory

- **Files**:
  - [`filter_params_backup.py`](inventory_service/app/filter_params_backup.py)
  - [`reproduce_500.py`](inventory_service/app/reproduce_500.py)
  - [`profiling.py`](inventory_service/app/profiling.py)
  - [`memory_monitor.py`](inventory_service/app/memory_monitor.py)
- **Priority**: Medium
- **Effort**: 15 min

**Problem**: Debug scripts and backup files are shipped in the production app directory. `filter_params_backup.py` is a stale copy of `filter_params.py`. `reproduce_500.py` is a one-off diagnostic script. `profiling.py` and `memory_monitor.py` are dev-only tools.

**Action**:
- [ ] Delete `filter_params_backup.py` and `reproduce_500.py`
- [ ] Move `profiling.py` and `memory_monitor.py` to a `dev/` directory, or gate them behind `APP_ENVIRONMENT` checks

---

### DEBT-10: Migrate remaining SQLAlchemy v1 query patterns to v2

- **File**: [`utilities.py`](inventory_service/app/utilities.py)
- **Priority**: Low
- **Effort**: 3–4 hours

**Problem**: The codebase was migrated from SQLModel/SQLAlchemy v1 to SQLAlchemy v2, but `utilities.py` still contains many v1-style queries:

```python
# v1 style (still present in utilities.py)
session.query(Item).filter(Item.barcode_id == barcode_id).first()

# v2 style (used in routers)
session.execute(select(Item).where(Item.barcode_id == barcode_id)).scalars().first()
```

This inconsistency makes the codebase harder to maintain and prevents a clean deprecation of v1 patterns.

**Action**:
- [ ] Audit `utilities.py` for all `session.query()` calls
- [ ] Refactor to use `session.execute(select(...))` pattern
- [ ] Test batch upload and refile queue functionality after migration

---

### DEBT-11: Register `MethodNotAllowed` exception handler

- **Files**:
  - [`exceptions.py`](inventory_service/app/config/exceptions.py)
  - [`main.py`](inventory_service/app/main.py)
- **Priority**: Low
- **Effort**: 5 min

**Problem**: The `MethodNotAllowed` exception class and its handler function `method_not_allowed_exception_handler` are defined in `exceptions.py`, but the handler is **never registered** in `main.py`. If a 405 is raised using this custom class, it falls through to the generic `unhandled_exception_handler` and returns a 500 instead.

```diff
 # main.py — add this line with the other exception handler registrations:
+ app.exception_handler(MethodNotAllowed)(method_not_allowed_exception_handler)
```

---

## 🟢 Nice-to-Have

Low-priority improvements for long-term code health.

### NICE-1: Standardize status badge naming convention

- **File**: [`qtable.scss`](vue/src/css/components/qtable.scss)
- **Priority**: Low
- **Effort**: 1 hour

**Problem**: Two naming conventions are used for status badge modifiers:
- **BEM**: `.status-badge--created`, `.status-badge--running`, `.status-badge--completed`
- **Flat**: `.status-new`, `.status-in-progress`, `.status-failed`, `.status-cancelled`

**Action**: Standardize all to BEM format (`.status-badge--{status}`) and update all template references.

---

### NICE-2: Replace Notify monkey-patch with composable

- **File**: [`notify-defaults.js`](vue/src/boot/notify-defaults.js)
- **Priority**: Low
- **Effort**: 1 hour

**Problem**: The boot file overrides `Notify.create()` globally to inject audio alert behavior. This monkey-patching pattern makes the behavior invisible and can break if Quasar changes the `Notify` API internally.

**Alternative**: Create a `useNotify()` composable or a thin wrapper function:

```javascript
// composables/useNotify.js
import { Notify } from 'quasar'
import { audioAlert } from '@/utils/audio'

export function useNotify() {
  function notify(opts) {
    if (opts.type === 'negative' || opts.color === 'negative') {
      audioAlert()
    }
    return Notify.create(opts)
  }
  return { notify }
}
```

---

### NICE-3: Consider Pinia Composition API for new stores

- **All files in**: [`vue/src/stores/`](vue/src/stores/)
- **Priority**: Low
- **Effort**: N/A (applies to new stores only)

**Problem**: All Pinia stores use the Options API pattern (`state/getters/actions`) while all `.vue` components use the Composition API (`<script setup>`). This isn't a bug, but it's a stylistic inconsistency.

**Recommendation**: For **new** stores, consider using Pinia's `setup()` syntax to align with the component style. **Do not** refactor existing stores — they work correctly and the cost outweighs the benefit.

```javascript
// New store using setup syntax
export const useNewStore = defineStore('new-store', () => {
  const items = ref([])
  const total = ref(0)

  async function fetchItems(params) {
    const res = await api.get('/new-items/', { params })
    items.value = res.data.items
    total.value = res.data.total
  }

  return { items, total, fetchItems }
})
```

---

## Summary

| Priority | Count | Estimated Total Effort |
|---|---|---|
| 🔴 Bug | 5 | ~4 hours |
| 🟡 Technical Debt | 11 | ~18 hours |
| 🟢 Nice-to-Have | 3 | ~3 hours |
| **Total** | **19** | **~25 hours** |
