package uk.gov.cshr.vcm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;

import java.util.HashMap;
import java.util.Map;

/**
 * This service is responsible for calling an external location lookup service to transform a place into a coordinates representing latitude and longitude.
 */
@Service
public class LocationService {
    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    @Value("${spring.location.service.url}")
    private String locationServiceURL;

    /**
     * This method calls an external service to map the given location to coordinates representing a single latitude and longitude.
     *
     * It is possible that no results are found.
     *
     * @param location the place whose latitude and longitude coordinates are required
     * @return Coordinates the corresponding latitude and longitude of the given location
     */
    public Coordinates find(final String location) throws LocationServiceException {
        Coordinates coordinates;

        Map<String, String> params = new HashMap<>();
        params.put("searchTerm", location);

        try {
            coordinates = new RestTemplate().getForObject(locationServiceURL, Coordinates.class, params);

            if (log.isDebugEnabled() && coordinates != null) {
                log.debug(String.format("Coordinates returned from the external lookup service for %s are %s", location, coordinates.toString()));
            }
        } catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error(String.format("An unexpected error occurred trying to find coordinates for %s", location), ex);
            }

            throw new LocationServiceException("An unexpected error occurred trying to find coordinates for " + location);
        }

        return coordinates;
    }
}
