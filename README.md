# CSHR API

## Prerequisites

* Docker ^17.05
* Docker Compose

## Setup

* Clone this repository 
* Run `make` or `./bin/build.sh` - this will setup `.env` file, create docker bridge network and build docker images. 

## Running API

`make run` or `docker-compose up api`

The api supports Spring Profiles. We have two profiles available 'dev' and 'prod'.

### Setting the spring profile
Add the following in your env_dev properties file:

`SPRING_PROFILES_ACTIVE=dev`

*Note: The 'dev' profile will give access to full Swagger capability such as the ability to use Swagger UI to try out the api.  This must not be allowed in any environment other than a development one.*

For production add the following in your env properties file:

`SPRING_PROFILES_ACTIVE=prod`

*Note: If you are running the env_dev properties file the build.sh copies this to env properties if env properties does not exist. Docker will use the env properties file in its work so ensure this version of the file has the profile you require.*

### Setting the location service url
If you are working with a local development environment and want to run this serivce and the location service via docker you will need to modify the env properties files.

Open env_dev properties and find:

LOCATION_SERVICE_URL

Modify the hostname so that it is using the ip address of your local machine.  Ensure the port number for the location service url is open on the location-service's docker container.

*Note: This is a temporary step until a lookup service is implemented, ie Eureka*

## Stopping API

`make stop` or `docker-compose stop api`

## Running Tests
`make test` or `docker-compose run --rm test`

## Todo
* Pass DB configuration to docker container as env variables and/or docker secrets
* Docker multistage build uses version `0.0.1` of the `war` release. This should be version agnostic. 
