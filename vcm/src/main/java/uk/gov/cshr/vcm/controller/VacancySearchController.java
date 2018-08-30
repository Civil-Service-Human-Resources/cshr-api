package uk.gov.cshr.vcm.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.cshr.status.StatusCode;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.controller.exception.VacancyClosedException;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.PublicVacancyAccessAuthenticator;
import uk.gov.cshr.vcm.model.SearchResponse;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyAuthenticationStatus;
import uk.gov.cshr.vcm.model.VacancyEligibility;
import uk.gov.cshr.vcm.model.VacancyMetadata;
import uk.gov.cshr.vcm.model.VacancyMetadataResponse;
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
@Slf4j
public class VacancySearchController {
    private SearchService searchService;
    private NotifyService notifyService;
    private CshrAuthenticationService cshrAuthenticationService;

    private final VacancyRepository vacancyRepository;

    public VacancySearchController(SearchService searchService, NotifyService notifyService,
                            CshrAuthenticationService cshrAuthenticationService, VacancyRepository vacancyRepository) {
        this.searchService = searchService;
        this.notifyService = notifyService;
        this.cshrAuthenticationService = cshrAuthenticationService;
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

        if (!foundVacancy.isPresent() && log.isDebugEnabled()) {
            log.debug("No vacancy found for id " + vacancyId);
        }

        if (foundVacancy.isPresent() && (!foundVacancy.get().getActive()
                || foundVacancy.get().getClosingDate().before(new Date()))) {
            throw new VacancyClosedException(vacancyId);
        } else {
            return foundVacancy.map(ResponseEntity.ok()::body).orElse(ResponseEntity.notFound().build());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{atsVendorIdentifier}/{atsReferenceIdentifier}")
    @ApiOperation(value = "Find a specific public vacancy", nickname = "find")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 410,
                    message = VacancyClosedException.CLOSED_MESSAGE,
                    response = VacancyError.class)
    })
    @RolesAllowed({"SEARCH_ROLE"})
    public ResponseEntity<Vacancy> find(@PathVariable String atsVendorIdentifier, @PathVariable Long atsReferenceIdentifier)
            throws VacancyClosedException {
        ResponseEntity<Vacancy> response;
        Vacancy vacancy = vacancyRepository.findVacancy(atsReferenceIdentifier, atsVendorIdentifier);

        if (vacancy != null) {
            VacancyAuthenticationStatus status = new PublicVacancyAccessAuthenticator().authenticate(vacancy);

            if (VacancyAuthenticationStatus.CLOSED.equals(status)) {
                throw new VacancyClosedException(atsReferenceIdentifier);
            } else if (VacancyAuthenticationStatus.NOT_AUTHENTICATED.equals(status)) {
                response = ResponseEntity.notFound().build();
            } else if (VacancyAuthenticationStatus.NOT_OPEN.equals(status)) {
                response = ResponseEntity.notFound().build();
            } else {
                response = ResponseEntity.ok(vacancy);
            }
        } else {
            response = ResponseEntity.notFound().build();
        }

        return response;
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
            throws LocationServiceException {

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

        if (departments.size() == 1) {

            String jwt = cshrAuthenticationService.createInternalJWT(emailAddress, departments.iterator().next());
            notifyService.emailInternalJWT(emailAddress, jwt, "name");
            return ResponseEntity.noContent().build();
        } else if (departments.size() > 1) {

            if (departmentID == null) {

                VerifyResponse verifyResponse = VerifyResponse.builder()
                        .departments(new ArrayList<>(departments))
                        .build();
                return ResponseEntity.ok().body(verifyResponse);
            }

            Set<Long> permittedDepartmentIDs = departments
                    .stream()
                    .map(Department::getId)
                    .collect(Collectors.toSet());

            if (!permittedDepartmentIDs.contains(departmentID)) {

                VacancyError vacancyError = VacancyError.builder()
                        .status(HttpStatus.UNAUTHORIZED)
                        .build();
                return ResponseEntity.ok().body(VerifyResponse.builder()
                        .vacancyError(vacancyError)
                        .build());
            } else {
                String jwt = cshrAuthenticationService.createInternalJWT(emailAddress, findDepartment(departmentID, departments));
                notifyService.emailInternalJWT(emailAddress, jwt, "name");
                return ResponseEntity.noContent().build();
            }
        } else {
            VacancyError vacancyError = VacancyError.builder().status(HttpStatus.UNAUTHORIZED).build();
            return ResponseEntity.ok().body(VerifyResponse.builder().vacancyError(vacancyError).build());
        }
    }

    private Department findDepartment(Long id, Set<Department> departments) {
        for (Department department : departments) {
            if (department.getId().equals(id)) {
                return department;
            }
        }

        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/vacancymetadata")
    @ApiOperation(value = "Get a collection vacancies with just identifier and date modified returned for each one.", nickname = "vacancymetadata")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Request to retrieve the metadata was successful",
                            response = VacancyMetadataResponse.class),
                    @ApiResponse(
                            code = 401,
                            message = "You are not authorised to use this service. Please supply the correct " +
                                    "credentials or contact the system administrator if you believe they are " +
                                    "correct.",
                            response = VacancyError.class
                    ),
                    @ApiResponse(
                            code = 500,
                            message =
                                    "An unexpected error occurred processing your request. Please contact the system " +
                                            "administrator.",
                            response = VacancyError.class
                    ),
                    @ApiResponse(
                            code = 503,
                            message =
                                    "The service is currently unavailable and your request cannot be processed at " +
                                            "this time. This may be a temporary condition and if it persists please " +
                                            "contact the system administrator",
                            response = VacancyError.class
                    )
            }
    )
    public ResponseEntity<VacancyMetadataResponse> getVacancyMetadata() {
        List<VacancyMetadata> vacancies = searchService.getVacancyMetadata();

        return ResponseEntity.ok().body(VacancyMetadataResponse.builder()
                .vacancies(vacancies)
                .responseStatus(CSHRServiceStatus.builder()
                        .code(StatusCode.PROCESS_COMPLETED.getCode())
                        .summary(vacancies.size() + " vacancies were retrieved")
                        .build())
                .build());
    }
}
