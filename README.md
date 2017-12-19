#CSHR

## How do I get set up? ##

Important: if your default Java version is 9 prepend each command with path to Java 8, like this: 
`JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_152.jdk/Contents/Home" `

### TEST

`./mvnw test`

### Build specific module (vcm for example):
`./mvnw install -pl vcm -am`

### Run specific module (vcm for example):
`./mvnw spring-boot:run -pl vcm`


### Persistence
By default it uses H2 in-memory database, but if you want to turn on persistence, uncomment the Postgresql comment in the configuration `cshr/vcm/src/main/resources/application.yaml`

Install docker [for mac] on your laptop.

Create docker instance for postgres

`docker run --name cshr-postgres -p 5432:5432 -e POSTGRES_PASSWORD=qwerty -e POSTGRES_DB=cshr -d postgres`

Run the application (or a specific module)

If you want to go into postgres shell, use following command:

`docker run -it --rm --link cshr-postgres:postgres postgres psql -h postgres -U postgres`

If you want to remove the docker image completely: 

`docker stop {first 3 characters of the id in docker ps}` 

`docker rm -v {first 3 characters of the id in docker ps}`
