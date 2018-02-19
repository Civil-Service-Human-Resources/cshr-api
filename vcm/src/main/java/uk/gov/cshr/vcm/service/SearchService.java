package uk.gov.cshr.vcm.service;

import java.util.ArrayList;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.VacancyRepository;

@Service
public class SearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Inject
    private LocationService locationService;

    @Resource
    private VacancyRepository vacancyRepository;

    public Page<Vacancy> search(VacancySearchParameters vacancySearchParameters, Pageable pageable) throws LocationServiceException {
        debug("staring search()");
        Coordinates coordinates = locationService.find(vacancySearchParameters.getLocation().getPlace());

        Page<Vacancy> vacancies;

        if (coordinatesExist(coordinates)) {
            debug("Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
            SearchParameters searchParameters = SearchParameters.builder()
                    .vacancySearchParameters(vacancySearchParameters)
                    .coordinates(coordinates)
                    .build();
            vacancies = vacancyRepository.search(searchParameters, pageable);
        } else {
            debug("No Coordinates for %s with radius of %d exist", vacancySearchParameters.getLocation().getPlace(), vacancySearchParameters.getLocation().getRadius());
            vacancies = new PageImpl<>(new ArrayList<Vacancy>());
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
