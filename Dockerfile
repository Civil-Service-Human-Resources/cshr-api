FROM maven:3.5-jdk-8 as BUILD

COPY vcm /usr/src/myapp/vcm
COPY pom.xml /usr/src/myapp

RUN mvn -f /usr/src/myapp/pom.xml clean package

FROM frolvlad/alpine-oraclejdk8:slim

COPY --from=BUILD /usr/src/myapp/vcm/target/vcm-0.0.1.war /vcm-0.0.1.war

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/vcm-0.0.1.war"]
