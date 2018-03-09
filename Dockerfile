FROM maven:3.5-jdk-8 as dependencies

COPY pom.xml /usr/src/myapp/pom.xml
COPY vcm/pom.xml /usr/src/myapp/vcm/pom.xml

RUN mvn -f /usr/src/myapp/pom.xml clean verify --fail-never

FROM maven:3.5-jdk-8 as build
COPY --from=dependencies /root/.m2 /root/.m2
COPY vcm /usr/src/myapp/vcm
COPY pom.xml /usr/src/myapp
RUN mvn -f /usr/src/myapp/pom.xml clean package

FROM frolvlad/alpine-oraclejdk8:slim

COPY --from=build /usr/src/myapp/vcm/target/vcm-0.0.1.jar /vcm-0.0.1.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/vcm-0.0.1.jar", \
		"--spring.location.service.url=${LOCATION_SERVICE_URL}", \
		"--spring.datasource.url=${DATASOURCE_URL}", \
		"--spring.datasource.username=${DATASOURCE_USERNAME}",\
		"--spring.datasource.password=${DATASOURCE_PASSWORD}"]
