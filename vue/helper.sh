#!/bin/bash

start() {
  # this is old, I don't recommend it
  ./local-build.sh -l;
}

web() {
  # this jumps into the interactive nginx shell on the vue pod
  podman exec -it $1 /bin/sh;
}

rebuild-fetch() {
    # use this to build after code changes
    (cd ../fetch-local && exec podman compose down);
    (cd ../fetch-local && exec podman compose up --build -d);
}

stop-fetch() {
    # use this when you're going to bed
    (cd ../fetch-local && exec podman compose down);
}

start-fetch() {
    # use this when you're waking up
    (cd ../fetch-local && exec podman compose up -d);
}

refresh-db() {
  # use this to refresh the database with fake data
  # used while pods are all running
  (cd ../fetch-local && exec ./helper.sh wipe-inventory-db);
  # Give the db a moment to catch its breath
  sleep 5;
  # Then rebuild from podman compose for schema
  (cd ../fetch-local \
    && exec ./helper.sh build-inventory-api);
}

build() {
  # this is old. I don't recommend it
  if [[ "$1" == "local" ]]; then
    ./local-build.sh;
  fi
  if [[ "$1" == "dev" ]]; then
    ./dev-build.sh;
  fi
  if [[ "$1" == "test" ]]; then
    ./test-build.sh;
  fi
}

"$@"
