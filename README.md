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
If you are working with a local development environment and want to run this service and the location service via docker you will need to modify the env properties files.

Open env_dev properties and find:

LOCATION_SERVICE_URL

Modify the hostname so that it is using the ip address of your local machine.  Ensure the port number for the location service url is open on the location-service's docker container.

*Note: This is a temporary step until a lookup service is implemented, ie Eureka*

The location service needs to be running if you wish to perform any location based searches.

## Stopping API

`make stop` or `docker-compose stop api`

## Running Tests
`make test` or `docker-compose run --rm test`

## Liquibase
The cshr database can be built or updated by running:-

mvn liquibase:update

*Note: You must run mvn clean package first*

Setting/passing the LOG_DIR system value/param will specify where log files are written

## Environment variables
The following variables need to be set when starting the container:
*Note: Any default developer values in application.yaml must be overridden for all non developer environments.*

1. **spring.datasource.url** - The url to the database
1. **spring.datasource.username** - The name of the user to connect to the database. 
1. **spring.datasource.password** - The password needed to connect to the database.
1. **spring.jpa.database-platform** - The type of database being used.
1. **spring.jpa.show-sql** - True to log sql otherwise false. Should be false in non developer environments.
1. **spring.jpa.hibernate.ddl-auto** - Determines autostart behaviour for hibernate. Hibernate should not build the databse. Use liquibase for that.
1. **spring.jpa.properties.hibernate.search.default.directory_provider** - Type of directory provider.
1. **spring.jpa.properties.hibernate.search.default.indexBase** - The location where the indices should be written.
1. **spring.location.service.url** - The URL for the location service endpoint.
1. **spring.location.service.username** - The username used to connect to the location service.
1. **spring.location.service.password** - The password used to connect to the location service.
1. **spring.security.search_username** - The name of user for the authenticated search role.
1. **spring.security.search_password** - The password of user for the authenticated search role.
1. **spring.security.crud_username** - The name of user for the authenticated crud role.
1. **spring.security.crud_password** - The password of user for the authenticated crud role.
1. **spring.security.notify_username** - The name of user for connecting to the notify service.
1. **spring.security.notify_password** - The password for connecting to the notify service.
1. **spring.notifyservice.templateid** - The id of the template used for the email to be sent for account verification.
1. **spring.notifyservice.notifyApiKey** - The api key needed to connect to the notify service.
1. **spring.notifyservice.accountEnableURL** - The url placed into the template that is used to confirm verification of the account.
1. **spring.cshrAuthenticationService.secret** - An HS512 key used to sign JWT tokens created when an authenticated user wishes to search for internal jobs. 
1. **server.port** - The port this service is running on.
1. **liquibase.enabled** - The determines if liquibase exucutes on startup. Valid values are true or false.
1. **liquibase.change-log** - The location of the changelog.xml file.

## Indexed Searches
Hibernate Search is being used to provide richer search capability and uses Lucene for indexing.

## Todo
* Pass DB configuration to docker container as env variables and/or docker secrets
* Docker multistage build uses version `0.0.1` of the `war` release. This should be version agnostic.
