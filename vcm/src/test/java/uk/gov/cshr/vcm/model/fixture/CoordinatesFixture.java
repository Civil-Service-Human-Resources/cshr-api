package uk.gov.cshr.vcm.model.fixture;

import uk.gov.cshr.vcm.model.Coordinates;

/**
 * This class is responsible for building some commonly used instances of Coordinates for use in testing.
 */
public final class CoordinatesFixture {
    private static final CoordinatesFixture INSTANCE = new CoordinatesFixture();

    private CoordinatesFixture() {}

    /**
     * Gets an instance of this class.
     *
     * @return CoordinatesFixture instance of this class
     */
    public static CoordinatesFixture getInstance() {
        return INSTANCE;
    }

    /**
     * This method gets the a set of coordinates that represents Bristol in the UK.
     *
     * The coordinates returned are those that would be returned by the Google API being used
     *
     * @return Coordinates set of coordinates that represents Bristol in the UK
     */
    public Coordinates getCoordinatesForBristol() {
        return Coordinates.builder().latitude(51.4549291).longitude(-2.6278111).build();
    }

    /**
     * This method gets the a set of coordinates that represents Newcastle in the UK.
     *
     * The coordinates returned are those that would be returned by the Google API being used
     *
     * @return Coordinates set of coordinates that represents Newcastle in the UK
     */
    public Coordinates getCoordinatesForNewcastle() {
        return Coordinates.builder().latitude(54.9806308).longitude(-1.6167437).build();
    }
}
