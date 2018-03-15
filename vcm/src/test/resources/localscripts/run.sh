#cd ../../../../../../rpg-candidate-interface-ui
#npm start &
cd ../../../../../../
java -jar location-service/location-service/target/location-service-1.0-SNAPSHOT.jar &
java -jar cshr-api/vcm/target/vcm-0.0.1.jar
