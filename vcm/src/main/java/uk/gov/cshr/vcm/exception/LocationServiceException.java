package uk.gov.cshr.vcm.exception;

/**
 * This class represents exceptions encountered in the location service
 */
public class LocationServiceException extends Exception {
    /**
     * Constructor for message.
     *
     * @param message Message for this exception
     */
    public LocationServiceException(String message) {
        super(message);
    }
}
