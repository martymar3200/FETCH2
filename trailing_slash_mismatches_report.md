# API Trailing Slash Mismatch Report
This report lists all **6** verified occurrences where the frontend calls an API route with or without a trailing slash, but the backend router expects the opposite.

### Why this matters:
1. **POST/PATCH/DELETE Redirect Failures**: FastAPI redirects requests without trailing slashes to their slashed version (or vice versa) via HTTP 307. However, if the redirect changes HTTP/HTTPS protocol in secure environments, it fails with mixed-content block errors. Redirects also drop payloads on non-GET requests if not handled properly.
2. **404/Method Not Allowed Errors**: Directly causes application errors if redirection is failed or disabled.

| # | Frontend Location | Method | Frontend Constructed URL | Backend Expected Path | Backend Location |
| --- | --- | --- | --- | --- | --- |
| 1 | [building-store.js](file:///Users/matthewmartin/workspace/FETCH2/fetch-vue/src/stores/building-store.js#L462) | 462 | POST | ``${inventoryServiceApi.batchUploadLocationManagement}`` | `/batch-upload/location-management` | [batch_upload.py](file:///Users/matthewmartin/workspace/FETCH2/fetch-inventory_service/app/routers/batch_upload.py#L785) |
| 2 | [building-store.js](file:///Users/matthewmartin/workspace/FETCH2/fetch-vue/src/stores/building-store.js#L478) | 478 | PATCH | ``${inventoryServiceApi.batchUploadLocationManagement}`` | `/batch-upload/location-management` | [batch_upload.py](file:///Users/matthewmartin/workspace/FETCH2/fetch-inventory_service/app/routers/batch_upload.py#L201) |
| 3 | [user-store.js](file:///Users/matthewmartin/workspace/FETCH2/fetch-vue/src/stores/user-store.js#L75) | 75 | POST | `'/auth/sso/logout/'` | `/auth/sso/logout` | [auth.py](file:///Users/matthewmartin/workspace/FETCH2/fetch-inventory_service/app/routers/auth.py#L247) |
| 4 | [user-store.js](file:///Users/matthewmartin/workspace/FETCH2/fetch-vue/src/stores/user-store.js#L118) | 118 | PATCH | ``${inventoryServiceApi.users}${id}/`` | `/users/{id}` | [users.py](file:///Users/matthewmartin/workspace/FETCH2/fetch-inventory_service/app/routers/users.py#L114) |
| 5 | [search-store.js](file:///Users/matthewmartin/workspace/FETCH2/fetch-vue/src/stores/search-store.js#L60) | 60 | GET | ``${inventoryServiceApi[jobEndpoint]}${searchInput}`` | `/reporting/exports/{dataset}/` | [reporting.py](file:///Users/matthewmartin/workspace/FETCH2/fetch-inventory_service/app/routers/reporting.py#L2841) |
| 6 | [option-store.js](file:///Users/matthewmartin/workspace/FETCH2/fetch-vue/src/stores/option-store.js#L103) | 103 | GET | ``${inventoryServiceApi[optionType]}${id}`` | `/reporting/exports/{dataset}/` | [reporting.py](file:///Users/matthewmartin/workspace/FETCH2/fetch-inventory_service/app/routers/reporting.py#L2841) |
