package uk.gov.cshr.vcm.service;

import java.io.IOException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Inject
    private LocationService locationService;

    @Inject
    private HibernateSearchService hibernateSearchService;

    public Page<Vacancy> search(VacancySearchParameters vacancySearchParameters, Pageable pageable)
            throws LocationServiceException, IOException {
		
        debug("staring search()");

//        if (coordinatesExist(coordinates)) {
//            debug("Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
//            SearchParameters searchParameters = SearchParameters.builder()
//                    .vacancySearchParameters(vacancySearchParameters)
//                    .coordinates(coordinates)
//                    .build();
//            vacancyLocations = hibernateSearchService.search(searchParameters, pageable);
//        }
//        else {
//            debug("No Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
//            vacancyLocations = new PageImpl<>(new ArrayList<>());
//        }
        SearchParameters searchParameters = SearchParameters.builder()
                .vacancySearchParameters(vacancySearchParameters)
                .build();

        if (vacancySearchParameters.getLocation() != null) {
            Coordinates coordinates = locationService.find(vacancySearchParameters.getLocation().getPlace());
            if (coordinatesExist(coordinates)) {
                searchParameters.setCoordinates(coordinates);
            }
            else {
                debug("No Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
            }
        }

        Page<Vacancy> vacancies = hibernateSearchService.search(searchParameters, pageable);

        for (Vacancy vacancy : vacancies.getContent()) {
            System.out.println("Vacancy=" + vacancy.getTitle() + ":" + vacancy.getId());
            for (VacancyLocation vacancyLocation : vacancy.getVacancyLocations()) {
                System.out.println("\t" + vacancyLocation.getLocation() + ": " + vacancyLocation.getLatitude() + "," + vacancyLocation.getLongitude());
            }
        }

        return vacancies;
    }

    private boolean coordinatesExist(Coordinates coordinates) {
        return coordinates != null && coordinates.getLatitude() != null && coordinates.getLongitude() != null;
    }

    private void debug(String message, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(message, params));
        }
    }
}
