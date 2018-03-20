#cd ../../../../../../rpg-candidate-interface-ui
#npm start &
cd ../../../../../../
mvn -f location-service/location-service/pom.xml
mvn -f location-service/location-service/pom.xml
java -jar location-service/location-service/target/location-service-1.0-SNAPSHOT.jar &
java -jar cshr-api/vcm/target/vcm-0.0.1.jar
