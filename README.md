#CSHR API

## Prerequisites

* Docker ^17.05
* Docker Compose

## Setup

* Clone this repository 
* Run `make` or `./bin/build.sh` - this will setup `.env` file, create docker bridge network and build docker images. 

## Running API

`make run` or `docker-compose up api`

The api supports Spring Profiles. We have two profiles available 'dev' and 'prod'.

To start the docker container from the command line, indicating the Spring Profile to use:

###dev Spring profile

'SPRING_PROFILES_ACTIVE=dev docker-compose up api'

*Note: The 'dev' profile will give access to full Swagger capability such as the ability to use Swagger UI to try out the api.  This must not be allowed in any environment other than a development one.*

###prod Spring Profile

'SPRING_PROFILES_ACTIVE=prod docker-compose up api'

## Stopping API

`make stop` or `docker-compose stop api`

## Running Tests
`make test` or `docker-compose run --rm test`

## Todo
* Pass DB configuration to docker container as env variables and/or docker secrets
* Docker multistage build uses version `0.0.1` of the `war` release. This should be version agnostic. 
