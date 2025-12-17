#!/bin/bash

# Do not use. Use scripts in fetch-local
# This may change to fit devops needs (e.g kubectl)
# This will also not build on local podman network
build-inventory-db () {
    podman stop inventory-database;

    podman container rm -f inventory-database;

    (cd inventory && podman build --file images/inventory.db.local.Containerfile --tag inventory-postgres-image .);

    podman run -d --name inventory-database -p 5432:5432 \
    -v inventory_service_data:/var/lib/postgresql/data \
    inventory-postgres-image
}

# Do not use. Use scripts in fetch-local
# This may change to fit devops needs (e.g. kubectl)
# This will also not build on local podman network
wipe-inventory-db () {
    podman stop inventory-database;

    # podman container rm -f inventory-database;
    # removes both container and volume
    podman rm -v [inventory-database];

    (cd inventory && podman build --file images/inventory.db.local.Containerfile --tag inventory-postgres-image .);

    podman run -d --name inventory-database -p 5432:5432 \
    -v inventory_service_data:/var/lib/postgresql/data \
    inventory-postgres-image
}

"$@"
