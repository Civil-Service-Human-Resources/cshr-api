#! /bin/bash
##
# Due to some restrictions with the Azure platform we need to
# run filebeat alongside the application in the same container.
# In order to achieve this we need an entrypoint file

echo "cshr-api: In entrypoint.sh"

echo "entrypoint.sh: command passed: " ${1}

if [[ ${#} -eq 0 ]]; then
    # -E to preserve the environment
    echo "Starting filebeat"
    sudo -E filebeat -e -c /etc/filebeat/filebeat.yml &
    echo "Starting application"
    java -Djava.security.egd=file:/dev/./urandom -jar /app/vcm-0.0.1.jar \
            --spring.location.service.url=${LOCATION_SERVICE_URL} \
            --spring.location.service.username=${LOCATION_SERVICE_USERNAME} \
            --spring.location.service.password=${LOCATION_SERVICE_PASSWORD} \
            --spring.datasource.url=${DATASOURCE_URL} \
            --spring.datasource.username=${DATASOURCE_USERNAME} \
            --spring.datasource.password=${DATASOURCE_PASSWORD} \
            --spring.security.search_username=${SEARCH_USERNAME} \
            --spring.security.search_password=${SEARCH_PASSWORD} \
            --spring.security.crud_username=${CRUD_USERNAME} \
            --spring.security.crud_password=${CRUD_PASSWORD} \
            --spring.notifyService.templateid=${NOTIFY_SERVICE_TEMPLATE_ID} \
            --spring.notifyservice.notifyApiKey=${NOTIFY_SERVICE_NOTIFY_API_KEY} \
            --spring.security.notify_username=${NOTIFY_USERNAME} \
            --spring.security.notify_password=${NOTIFY_PASSWORD} 
else
    echo "Running command:"
    exec "$@"
fi
