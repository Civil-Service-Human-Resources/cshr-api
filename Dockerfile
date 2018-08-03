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

FROM 	cshrrpg.azurecr.io/java-8-base-filebeat

COPY 	--from=build --chown=appuser:appuser /usr/src/myapp/vcm/target/vcm-1.0.0.jar /app/vcm-1.0.0.jar

COPY    --chown=appuser:appuser entrypoint.sh /usr/local/bin/entrypoint.sh

RUN     chmod 755 /usr/local/bin/entrypoint.sh

COPY    --chown=appuser:appuser entrypoint.sh /usr/local/bin/entrypoint.sh

RUN     chmod 755 /usr/local/bin/entrypoint.sh

USER 	appuser

ENTRYPOINT [  "/usr/local/bin/entrypoint.sh" ]
