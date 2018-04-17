package uk.gov.cshr.vcm.service;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;

/**
 * This service is responsible for calling an external location lookup service to transform a place into a coordinates representing latitude and longitude.
 */
@Service
public class LocationService {
    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    @Value("${spring.location.service.url}")
    private String locationServiceURL;

	@Value("${spring.location.service.username}")
    private String locationServiceUsername;

	@Value("${spring.location.service.password}")
    private String locationServicePassword;

    /**
     * This method calls an external service to map the given location to coordinates representing a single latitude and longitude.
     *
     * It is possible that no results are found.
     *
     * @param location the place whose latitude and longitude coordinates are required
     * @return Coordinates the corresponding latitude and longitude of the given location
	 * @throws uk.gov.cshr.vcm.controller.exception.LocationServiceException
     */
    public Coordinates find(String location) throws LocationServiceException {

        Map<String, String> params = new HashMap<>();
        params.put("searchTerm", location);

        try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders httpHeaders = createHeaders(locationServiceUsername, locationServicePassword);
			HttpEntity<String> httpEntity = new HttpEntity(httpHeaders);

			ResponseEntity<Coordinates> response = restTemplate.exchange(locationServiceURL, HttpMethod.GET, httpEntity, Coordinates.class, params);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Coordinates returned from the external lookup service for %s are %s", location, response.getBody()));
            }

			return response.getBody();
        }
		catch (RestClientException ex) {
            if (log.isErrorEnabled()) {
                log.error(String.format("An unexpected error occurred trying to find coordinates for %s", location), ex);
            }
            throw new LocationServiceException();
        }
    }

	private HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.getEncoder().encode(
						auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
			}
		};
	}
}
