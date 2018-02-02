package uk.gov.cshr.vcm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.vcm.model.Coordinates;

/**
 * This service is responsible for calling an external location lookup service to transform a place into a coordinates representing latitude and longitude.
 */
@Service
public class LocationService {
    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

//    @Value("${spring.location.service.url}")
    private String locationServiceURL = "http://location-service.dev.cshr-gov.uk/findlocation/";

    /**
     * This method calls an external service to map the given location to coordinates representing a single latitude and longitude.
     *
     * It is possible that no results are found.
     *
     * @param location the place whose latitude and longitude coordinates are required
     * @return Coordinates the corresponding latitude and longitude of the given location
     */
    public Coordinates find(final String location) {
        log.debug("Starting find() and LOCATION SERVICE URL = " + locationServiceURL);
        Coordinates coordinates = null;

//        Map<String, String> params = new HashMap<>();
//        params.put("searchTerm", location);
        log.debug("URL==" + locationServiceURL + location);

        try {
            coordinates = new RestTemplate().getForObject(locationServiceURL + location, Coordinates.class);
            log.debug("COORDINATES FOR " + location + " ARE " + coordinates.toString());
        } catch (Exception ex) {
            log.error("An unexpected error occurred trying to find coordinates for " + location, ex);
            //THROW A SERVICE EXCEPTION
        }

        return coordinates;
    }
}
