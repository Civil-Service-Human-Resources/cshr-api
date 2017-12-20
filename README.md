#CSHR API

## Prerequisites

* Docker ^17.05
* Docker Compose

## Setup

* Clone this repository 
* Run `make` or `./bin/build.sh` - this will setup `.env` file, create docker bridge network and build docker images. 

## Running API

`make run` or `docker-compose up api`

## Stopping API

`make stop` or `docker-compose stop api`

## Running Tests
`make test` or `docker-compose run --rm test`

## Todo
* Pass DB configuration to docker container as env variables and/or docker secrets
* Docker multistage build uses version `0.0.1` of the `war` release. This should be version agnostic. 
