package uk.gov.cshr.vcm.service;

import java.util.List;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.cshr.status.StatusCode;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.SearchResponse;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyMetadata;
import uk.gov.cshr.vcm.model.VacancySearchParameters;

@Service
@Slf4j
public class SearchService {
    @Inject
    private LocationService locationService;

    @Inject
    private HibernateSearchService hibernateSearchService;

    public void search(VacancySearchParameters vacancySearchParameters, SearchResponse searchResponse,
                       Pageable pageable) throws LocationServiceException {

        debug("staring search()");

        SearchParameters searchParameters = SearchParameters.builder()
                .vacancySearchParameters(vacancySearchParameters)
                .vacancyEligibility(vacancySearchParameters.getVacancyEligibility())
                .build();

        if (filterByLocation(vacancySearchParameters)) {

            Coordinates coordinates = locationService.find(vacancySearchParameters.getLocation().getPlace());

            if (coordinatesExist(coordinates)) {
                searchParameters.setCoordinates(coordinates);
            } else {
                searchResponse.setCshrServiceStatus(CSHRServiceStatus.builder()
                        .code(StatusCode.NO_RESULTS_FOR_LOCATION.getCode())
                        .summary("The location service was unable to match the location supplied.")
                        .build());
                debug("No Coordinates for %s with radius of %d exist",
                        vacancySearchParameters.getLocation().getPlace(),
                        vacancySearchParameters.getLocation().getRadius());
            }
        }

        Page<Vacancy> vacancies = hibernateSearchService.search(searchParameters, pageable);
        searchResponse.setVacancies(vacancies);
    }

    private boolean filterByLocation(VacancySearchParameters vacancySearchParameters) {
        return vacancySearchParameters.getLocation() != null && StringUtils.isNotEmpty(vacancySearchParameters.getLocation().getPlace());
    }

    private boolean coordinatesExist(Coordinates coordinates) {
        return coordinates != null && coordinates.getLatitude() != null && coordinates.getLongitude() != null;
    }

    private void debug(String message, Object... params) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(message, params));
        }
    }

    /**
     * This method delegates responsibility or retrieving a collection of ids and dates last modified for public facing
     * vacancies.
     *
     * @return a collection of ids and dates last modified for public facing vacancies
     */
    public List<VacancyMetadata> getVacancyMetadata() {
        return hibernateSearchService.getVacancyMetadata();
    }
}
