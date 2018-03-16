package uk.gov.cshr.vcm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

//    @Resource
//    private VacancyRepository vacancyRepository;
    @Inject
    private HibernateSearchService hibernateSearchService;

    public Page<Vacancy> search(VacancySearchParameters vacancySearchParameters, Pageable pageable)
            throws LocationServiceException, IOException {
		
        debug("staring search()");
        Coordinates coordinates = locationService.find(vacancySearchParameters.getLocation().getPlace());

        Page<VacancyLocation> vacancyLocations;

        if (coordinatesExist(coordinates)) {
            debug("Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
            SearchParameters searchParameters = SearchParameters.builder()
                    .vacancySearchParameters(vacancySearchParameters)
                    .coordinates(coordinates)
                    .build();
            vacancyLocations = hibernateSearchService.search(searchParameters, pageable);
        } else {
            debug("No Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
            vacancyLocations = new PageImpl<>(new ArrayList<>());
        }

        HashSet<Vacancy> vacanciesSet = new HashSet<>();
        for (VacancyLocation vacancyLocation : vacancyLocations.getContent()) {
            vacanciesSet.add(vacancyLocation.getVacancy());
        }

        PageImpl vacancies = new PageImpl<>(Arrays.asList(vacanciesSet.toArray()), pageable, 1);

//        Page<Vacancy> vacancies = new PageImpl<>(new ArrayList<>());
//
//        for (Vacancy vacancy : vacanciesSet) {
//            vacancies.getContent().add(vacancy);
//        }

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
