# Database

## About

This directory contains container images and basic scripts for managing containerized PostgreSQL deployments used by the FETCH2 application.

### Inventory Service Database
The application schema and data-seeding for local, dev, or test environments **are not managed here**. Those are managed by the Inventory Service itself via Alembic migrations. If a data volume is wiped (supported here), the Inventory Service will rebuild the schema when it is built and deployed, provided the database is running.

### Keycloak Database
Keycloak database configuration is not currently in use. This section is reserved for future identity provider integration if needed.

## Build

### Inventory Service Local Development
The `fetch-local` build makes use of the images in this directory via a compose file. Simply running the build from `fetch-local` will be sufficient for day-to-day use.

Rebuilding the database container and refreshing (wiping) the database can be achieved with the helper scripts also in `fetch-local`, or from Inventory or Vue apps which also use scripts in `fetch-local`. Using the scripts in Inventory or Vue apps is the best choice, as those will also take care of rebuilding the schema.

### Inventory Service Deployed Builds
Deployed database provisioning is managed via Terraform and Kubernetes configs located in the `inventory_service/terraform/` and `inventory_service/k8s/` directories.

### PGAdmin
A PGAdmin container image is included for local database management. See the `fetch-local` README for connection details.

## Volume Disaster Recovery
Volume recovery procedures should be established as part of your deployment environment's backup strategy. For local development, wiping and rebuilding the database volume via `fetch-local` helper scripts is the standard recovery path.