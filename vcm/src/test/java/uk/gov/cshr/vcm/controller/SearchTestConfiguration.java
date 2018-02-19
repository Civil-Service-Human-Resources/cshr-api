package uk.gov.cshr.vcm.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.service.LocationService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

public abstract class SearchTestConfiguration extends AbstractJUnit4SpringContextTests {
    static final double BRISTOL_LATITUDE = 51.4549291;
    static final double BRISTOL_LONGITUDE = -2.6278111;

    static final double NEWCASTLE_LATITUDE = 54.9806308;
    static final double NEWCASTLE_LONGITUDE = -1.6167437;

    @MockBean
    LocationService locationService;

    void initLocationService() throws LocationServiceException {
        given(locationService.find("bristol"))
                .willReturn(new Coordinates(BRISTOL_LONGITUDE, BRISTOL_LATITUDE));

        given(locationService.find("newcastle"))
                .willReturn(new Coordinates(NEWCASTLE_LONGITUDE, NEWCASTLE_LATITUDE));
    }
}
