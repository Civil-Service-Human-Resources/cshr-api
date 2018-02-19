package uk.gov.cshr.vcm.controller;

import static org.mockito.BDDMockito.given;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.service.LocationService;

public abstract class SearchTestConfiguration extends AbstractJUnit4SpringContextTests {
    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    public static final double NEWCASTLE_LATITUDE = 54.9806308;
    public static final double NEWCASTLE_LONGITUDE = -1.6167437;

    public static final Coordinates BRISTOL_COORDINATES = Coordinates.builder().latitude(BRISTOL_LATITUDE).longitude(BRISTOL_LONGITUDE).build();

    @MockBean
    public LocationService locationService;

    public void initLocationService() throws LocationServiceException {
        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE));

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE));
    }
}
