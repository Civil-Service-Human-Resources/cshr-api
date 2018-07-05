package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.SearchResponse;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyEligibility;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.model.VerifyRequest;
import uk.gov.cshr.vcm.model.VerifyResponse;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.CshrAuthenticationService;
import uk.gov.cshr.vcm.service.NotifyService;
import uk.gov.cshr.vcm.service.SearchService;
import uk.gov.service.notify.NotificationClientException;

@RestController
@RequestMapping(value = "/vacancy", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@Api(value = "vacancySearchService")
@RolesAllowed("SEARCH_ROLE")
public class VacancySearchController {

    private static final Logger log = LoggerFactory.getLogger(VacancySearchController.class);

    @Inject
    private SearchService searchService;

	@Inject
	private NotifyService notifyService;

	@Inject
	private CshrAuthenticationService cshrAuthenticationService;

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
    public ResponseEntity<SearchResponse> search(
			@ApiParam(name = "searchParameters", value = "The values supplied to perform the search", required = true)
            @RequestBody VacancySearchParameters vacancySearchParameters,
			@RequestHeader(value = "cshr-authentication", required = false) String jwt,
			Pageable pageable)
            throws LocationServiceException, IOException {

        SearchResponse searchResponse = SearchResponse.builder().build();

		VacancyEligibility vacancyEligibility = cshrAuthenticationService.parseVacancyEligibility(jwt, searchResponse);
		vacancySearchParameters.setVacancyEligibility(vacancyEligibility);

		searchService.search(vacancySearchParameters, searchResponse, pageable);
		return ResponseEntity.ok().body(searchResponse);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/verifyemail")
    @ApiOperation(value = "Generate a JWT to enable access to internal vacancies", nickname = "verifyEmailJWT")
    public ResponseEntity<VerifyResponse> verifyEmailJWT(@RequestBody VerifyRequest verifyRequest) throws NotificationClientException {

        String emailAddress = verifyRequest.getEmailAddress();
        Long departmentID = verifyRequest.getDepartmentID();

        if (emailAddress == null) {
            log.debug("emailAddress cannot be null");
            return ResponseEntity.badRequest().build();
        }

        Set<Department> departments = cshrAuthenticationService.verifyEmailAddress(emailAddress);
        List<Long> permittedDepartmentIDs = new ArrayList<>();

        for (Department department : departments) {
            permittedDepartmentIDs.add(department.getId());
        }

        if (departments.size() == 1) {

            String jwt = cshrAuthenticationService.createInternalJWT(emailAddress, departments.iterator().next());
            notifyService.emailInternalJWT(emailAddress, jwt, "name");
            return ResponseEntity.noContent().build();
        }

        else if (departments.size() > 1) {

            if (departmentID == null) {

                VerifyResponse verifyResponse = VerifyResponse.builder()
                        .departments(new ArrayList<>(departments))
                        .build();
                return ResponseEntity.ok().body(verifyResponse);
            }

            if (!permittedDepartmentIDs.contains(departmentID)) {

                VacancyError vacancyError = VacancyError.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .build();
                return ResponseEntity.ok().body(VerifyResponse.builder()
                        .vacancyError(vacancyError)
                        .build());
            }
            else {
                String jwt = cshrAuthenticationService.createInternalJWT(emailAddress, findDepartment(departmentID, departments));
                notifyService.emailInternalJWT(emailAddress, jwt, "name");
                return ResponseEntity.noContent().build();
            }

        }

        else {

            VacancyError vacancyError = VacancyError.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
            return ResponseEntity.ok().body(VerifyResponse.builder().vacancyError(vacancyError).build());
        }
    }

    private Department findDepartment(Long id, Set<Department> departments) {
        for (Department department : departments) {
            if ( department.getId().equals(id) ) {
                return department;
            }
        }
        return null;
    }
}
