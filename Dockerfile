FROM 	cshrrpg.azurecr.io/maven-3-base as dependencies

ARG	    arg_maven_repo
ENV 	MAVEN_REPO=$arg_maven_repo

COPY 	pom.xml /usr/src/myapp/pom.xml
COPY 	vcm/pom.xml /usr/src/myapp/vcm/pom.xml
COPY 	.settings.xml /usr/share/maven/ref/settings.xml

RUN 	mvn -f /usr/src/myapp/pom.xml -s /usr/share/maven/ref/settings.xml clean verify --fail-never

FROM 	cshrrpg.azurecr.io/maven-3-base as build

ARG	    arg_maven_repo
ENV	    MAVEN_REPO=$arg_maven_repo

COPY 	--from=dependencies /usr/share/maven/ref/ /usr/share/maven/ref/

COPY 	vcm /usr/src/myapp/vcm
COPY 	pom.xml /usr/src/myapp
RUN 	mvn -f /usr/src/myapp/pom.xml -s /usr/share/maven/ref/settings.xml clean package

FROM 	cshrrpg.azurecr.io/java-8-base

COPY 	--from=build --chown=appuser:appuser /usr/src/myapp/vcm/target/vcm-1.0.0.jar /app/vcm-1.0.01.jar

USER 	appuser

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/vcm-1.0.0.jar", \
		"--spring.location.service.url=${LOCATION_SERVICE_URL}", \
		"--spring.location.service.username=${LOCATION_SERVICE_USERNAME}", \
		"--spring.location.service.password=${LOCATION_SERVICE_PASSWORD}", \
		"--spring.datasource.url=${DATASOURCE_URL}", \
		"--spring.datasource.username=${DATASOURCE_USERNAME}", \
		"--spring.datasource.password=${DATASOURCE_PASSWORD}", \
		"--spring.security.search_username=${SEARCH_USERNAME}", \
		"--spring.security.search_password=${SEARCH_PASSWORD}", \
		"--spring.security.crud_username=${CRUD_USERNAME}", \
		"--spring.security.crud_password=${CRUD_PASSWORD}" ]
