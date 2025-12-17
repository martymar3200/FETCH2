#!/usr/bin/env bash
set -x

IMAGE_LOCATION=images/web.test.Containerfile
IMAGE_TAG=fetch-web-image
CONTAINER_NAME=fetch-web
INTERNAL_PORT=80
HOST_PORT=80

export IS_SERVE_LOCAL_NODE_VERSION=false;

while getopts "l" opt; do
    case $opt in
        l) # -l flag detected
            IS_SERVE_LOCAL_NODE_VERSION=true
            printf "$0 serving on local node server with hot reloading\n"
            shift
            break
    esac
done

if $IS_SERVE_LOCAL_NODE_VERSION
then
    # Stop any local vue container (taking up port 8080)
    CONTAINER_IDS=$(podman ps -q --filter name="^${CONTAINER_NAME}$")
    if [ "$CONTAINER_IDS" != "" ]; then
        podman stop $CONTAINER_IDS
    fi

    which python
    # if execution stops here, you have no python executable
    # (which caused an error when I ran npm i)
    # I already had a python3 executable, so I created a symlink:
    # sudo ln -sfn /usr/local/bin/python3 /usr/local/bin/python
    # and ran this script again. now it works

    which npm
    # if execution stops here, you have no npm executable
    # brew reinstall node
    # and run this script again

    npm i

    npm run dev:local
fi

if ! $IS_SERVE_LOCAL_NODE_VERSION
then
    # kill any local node.js dev server (taking up port 8080)
    killall node || echo "node server not running, starting build"


    # stop the container if there is one running
    CONTAINER_IDS=$(podman ps -q --filter name="^${CONTAINER_NAME}$")
    if [ "$CONTAINER_IDS" != "" ]; then
        podman stop $CONTAINER_IDS
    fi

    # delete the container if there is one
    CONTAINER_IDS=$(podman container ls -aq --filter name="^${CONTAINER_NAME}$")
    if [ "$CONTAINER_IDS" != "" ]; then
        podman container rm -f $CONTAINER_IDS
    fi

    # delete the image if there is one
    IMAGE_IDS=$(podman image ls -aq --filter "reference=${IMAGE_TAG}")
    if [ "$IMAGE_IDS" != "" ]; then
        podman image rm -f $IMAGE_IDS
    fi

    # Test if port is clear
    nc -vz -w 2 localhost ${HOST_PORT}

    podman build \
    --file ${IMAGE_LOCATION} --tag ${IMAGE_TAG} .

    # run the fetch-web container
    podman run --restart=always -it -d \
        -p${HOST_PORT}:${INTERNAL_PORT} \
        --name ${CONTAINER_NAME} \
        ${IMAGE_TAG}
fi
