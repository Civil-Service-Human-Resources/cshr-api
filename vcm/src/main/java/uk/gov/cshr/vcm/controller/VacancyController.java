package uk.gov.cshr.vcm.controller;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.UrlValidator;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.cshr.status.StatusCode;
import uk.gov.cshr.vcm.model.ApplyURLSanitiser;
import uk.gov.cshr.vcm.model.MaxSalaryDerivator;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.ApplicantTrackingSystemService;
import uk.gov.cshr.vcm.service.HibernateSearchService;

@RestController
@RequestMapping(value = "/vacancy", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@Api(value = "vacancyservice")
@RolesAllowed("CRUD_ROLE")
@Slf4j
public class VacancyController {
    private final ApplicantTrackingSystemService applicantTrackingSystemService;
    private HibernateSearchService hibernateSearchService;
    private final VacancyRepository vacancyRepository;

    VacancyController(ApplicantTrackingSystemService applicantTrackingSystemService,
                      VacancyRepository vacancyRepository, HibernateSearchService hibernateSearchService) {
        this.applicantTrackingSystemService = applicantTrackingSystemService;
        this.vacancyRepository = vacancyRepository;
        this.hibernateSearchService = hibernateSearchService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create a Vacancy", nickname = "createVacancy")
    public ResponseEntity<Vacancy> create(@RequestBody Vacancy vacancy) {

        if (vacancy.getActive() == null) {
            vacancy.setActive(Boolean.TRUE);
        }

        Vacancy savedVacancy = createVacancy(vacancy);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedVacancy.getId()).toUri();

        return ResponseEntity.created(location).body(savedVacancy);
    }

    private Vacancy createVacancy(Vacancy vacancy) {

        if (vacancy.getVacancyLocations() != null) {
            vacancy.getVacancyLocations().forEach(vacancyLocation -> vacancyLocation.setVacancy(vacancy));
        }

        applyCommonRules(vacancy);

        return vacancyRepository.save(applyCommonRules(vacancy));
    }

    private Vacancy applyCommonRules(Vacancy vacancy) {
        vacancy = ApplyURLSanitiser.sanitise(vacancy);

        return MaxSalaryDerivator.deriveMaxSalary(vacancy);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{vacancyId}")
    @ApiOperation(value = "Update a Vacancy", nickname = "update")
    public ResponseEntity<Vacancy> update(@PathVariable Long vacancyId, @RequestBody Vacancy vacancyUpdate) {

        Optional<Vacancy> foundVacancy = vacancyRepository.findById(vacancyId);

        if (foundVacancy.isPresent()) {
            Vacancy updatedVacancy = updateVacancy(vacancyUpdate, foundVacancy.get());
            return ResponseEntity.ok().body(updatedVacancy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Vacancy updateVacancy(@RequestBody Vacancy vacancyUpdate, Vacancy foundVacancy) {
        vacancyUpdate.getVacancyLocations().forEach(vacancyLocation -> vacancyLocation.setVacancy(vacancyUpdate));

        vacancyUpdate.setId(foundVacancy.getId());

        return vacancyRepository.save(applyCommonRules(vacancyUpdate));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{vacancyId}")
    @ApiOperation(value = "Delete a Vacancy", nickname = "delete")
    public ResponseEntity<Vacancy> deleteById(@PathVariable Long vacancyId) {

        vacancyRepository.delete(vacancyId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Find all vacancies with support for pagination", nickname = "findAll")
    public ResponseEntity<Page<Vacancy>> findAll(Pageable pageable) {

        Page<Vacancy> vacancies = vacancyRepository.findAll(pageable);
        return ResponseEntity.ok().body(vacancies);
    }

    /**
     * This method is responsible for saving a vacancy.
     * <p>
     * The method will validate that the Applicant Tracking System Vendor identifier supplied in the vacancy is one
     * that is recognised by CSHR.  If the identifier is not valid an InvalidApplicantTrackingSystemException will be thrown.
     * <p>
     * The method will check if the vacancy already exists (based on Vacancy.identifier and Vacancy.atsVendorIdentifier).
     * If the vacancy does not exist a new one will be created otherwise the existing one will be updated.
     *
     * @param vacancyToSave vacancy supplied to be saved in the data store
     * @return Response include HttpStatus and instance of CSHRServiceStatus with information about whether the vacancye was created or updated.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/save")
    @ApiOperation(value = "Creates a new or updates an existing vacancy", nickname = "save")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request to save vacancy was successful.", response = CSHRServiceStatus.class),
            @ApiResponse(code = 422, message = "An unknown Applicant Tracking System Vendor was supplied.", response = CSHRServiceStatus.class)})
    public ResponseEntity<CSHRServiceStatus> save(@RequestBody Vacancy vacancyToSave) {
        applicantTrackingSystemService.validateClientIdentifier(vacancyToSave.getAtsVendorIdentifier());

        Vacancy vacancy = vacancyRepository.findVacancy(vacancyToSave.getIdentifier(), vacancyToSave.getAtsVendorIdentifier());

        String message;
        String code;
        if (vacancy == null) {
            vacancy = createVacancy(vacancyToSave);
            message = "Vacancy created for jobRef " + vacancy.getIdentifier().toString();
            code = StatusCode.RECORD_CREATED.getCode();
        } else {
            vacancy = updateVacancy(vacancyToSave, vacancy);
            message = "Vacancy updated for jobRef " + vacancy.getIdentifier().toString();
            code = StatusCode.RECORD_UPDATED.getCode();
        }

        return ResponseEntity.ok()
                .body(
                        CSHRServiceStatus.builder()
                                .code(code)
                                .summary(message)
                                .detail(Collections.emptyList())
                                .build());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/refresh")
    public ResponseEntity<?> refresh() throws InterruptedException {

        hibernateSearchService.initializeHibernateSearch();
        return ResponseEntity.noContent().build();
    }
}
