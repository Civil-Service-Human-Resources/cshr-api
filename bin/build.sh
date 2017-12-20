#!/bin/sh

set -e

if [ ! -f ".env" ]; then
    cp .env_dev .env
fi

NETWORK_NAME="cshr-net"

docker network ls | grep "$NETWORK_NAME" > /dev/null 2>&1

if [ "$?" = "1" ]; then
    docker network create --driver bridge "$NETWORK_NAME"
fi

docker-compose build api
