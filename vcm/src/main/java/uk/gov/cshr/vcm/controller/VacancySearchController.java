package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.SearchService;

@RestController
@RequestMapping(value = "/vacancy", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@Api(value = "vacancySearchService")
@RolesAllowed("SEARCH_ROLE")
public class VacancySearchController {

    private static final Logger log = LoggerFactory.getLogger(VacancySearchController.class);

    @Inject
    private SearchService searchService;

    private final VacancyRepository vacancyRepository;

    @Autowired
    VacancySearchController(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{vacancyId}")
    @ApiOperation(value = "Find a specific vacancy", nickname = "findById")
	@ApiResponses(value = {
		@ApiResponse(
				code = 410,
				message = VacancyClosedException.CLOSED_MESSAGE,
				response = VacancyError.class)
	})
	@RolesAllowed({"SEARCH_ROLE", "CRUD_ROLE"})
    public ResponseEntity<Vacancy> findById(@PathVariable Long vacancyId)
			throws VacancyClosedException {

        Optional<Vacancy> foundVacancy = vacancyRepository.findById(vacancyId);

        if ( ! foundVacancy.isPresent() && log.isDebugEnabled()) {
            log.debug("No vacancy found for id " + vacancyId);
        }

		if ( foundVacancy.isPresent() && (foundVacancy.get().getActive() == false
                || foundVacancy.get().getClosingDate().before(new Date()) ) ) {
			throw new VacancyClosedException(vacancyId);
		}
        else {
            return foundVacancy.map(ResponseEntity.ok()::body).orElse(ResponseEntity.notFound().build());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/search")
    @ApiOperation(value = "Search for vacancies by location and keyword with support for pagination")
	@ApiResponses(value = {
		@ApiResponse(
				code = 503,
				message = LocationServiceException.SERVICE_UNAVAILABLE_MESSAGE,
				response = VacancyError.class)
	})
    public ResponseEntity<Page<Vacancy>> search(
			@ApiParam(name = "searchParameters", value = "The values supplied to perform the search", required = true)
            @RequestBody VacancySearchParameters vacancySearchParameters, Pageable pageable)
            throws LocationServiceException, IOException {

		Page<Vacancy> vacancies = searchService.search(vacancySearchParameters, pageable);
		return ResponseEntity.ok().body(vacancies);
    }
}
