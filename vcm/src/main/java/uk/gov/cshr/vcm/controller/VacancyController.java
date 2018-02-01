package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import uk.gov.cshr.vcm.model.Coordinates;
import uk.gov.cshr.vcm.model.Location;
import uk.gov.cshr.vcm.model.SearchParameters;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancySearchParameters;
import uk.gov.cshr.vcm.repository.VacancyRepository;
import uk.gov.cshr.vcm.service.LocationService;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping(value = "/vacancy", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@Api(value = "vacancyservice", description = "Operations pertaining to vacancies for jobs in Government")
public class VacancyController {
    private static final Logger log = LoggerFactory.getLogger(VacancyController.class);

    @Inject
    private LocationService locationService;

    private final VacancyRepository vacancyRepository;

    @Autowired
    VacancyController(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Find all vacancies with support for pagination", nickname = "findAll")
    public ResponseEntity<Page<Vacancy>> findAll(Pageable pageable) {
        Page<Vacancy> vacancies = vacancyRepository.findAll(pageable);
        return ResponseEntity.ok().body(vacancies);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Find a specific vacancy", nickname = "findById")
    public ResponseEntity<Vacancy> findById(@PathVariable Long vacancyId) {

        Optional<Vacancy> foundVacancy = vacancyRepository.findById(vacancyId);
        System.out.print("Test");

        if (!foundVacancy.isPresent()) {
            log.debug("No vancancy found for id " + vacancyId);
        }
        ResponseEntity<Vacancy> notFound = ResponseEntity.notFound().build();
        return foundVacancy.map(vacancy -> ResponseEntity.ok().body(vacancy)).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "create", nickname = "create")
    public ResponseEntity<Vacancy> create(@RequestBody Vacancy vacancy) {

        Vacancy savedVacancy = vacancyRepository.save(vacancy);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedVacancy.getId()).toUri();

        return ResponseEntity.created(location).body(savedVacancy);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "update", nickname = "update")
    public ResponseEntity<Vacancy> update(@PathVariable Long vacancyId, @RequestBody Vacancy vacancyUpdate) {

        Optional<Vacancy> foundVacancy = vacancyRepository.findById(vacancyId);

        if (!foundVacancy.isPresent()) {
            log.error("No vacancy found for id " + vacancyId);
        }

        ResponseEntity<Vacancy> notFound = ResponseEntity.notFound().build();

        return foundVacancy.map((Vacancy vacancy) -> {
            // Attention, mutable state on the argument
            vacancyUpdate.setId(vacancy.getId());
            vacancyRepository.save(vacancyUpdate);
            return ResponseEntity.ok().body(vacancy);
        }).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "delete", nickname = "delete")
    public ResponseEntity<Vacancy> deleteById(@PathVariable Long vacancyId) {

        vacancyRepository.delete(vacancyId);
        return ResponseEntity.noContent().build();

    }

    @RequestMapping(method = RequestMethod.GET, value = "/search/location/{location}/keyword/{keyword}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search for vacancies by location and keyword with support for pagination", nickname = "searchByLocationAndKeyword")
    @Deprecated
    public ResponseEntity<Page<Vacancy>> search(@PathVariable String location, @PathVariable String keyword, Pageable pageable) {
        Page<Vacancy> vacancies = vacancyRepository.search(location, keyword, pageable);

        return ResponseEntity.ok().body(vacancies);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/search/location/{location}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search for vacancies by location", nickname = "searchByLocation")
    @Deprecated
    public ResponseEntity<Page<Vacancy>> search(@PathVariable String location, Pageable pageable) {
        Page<Vacancy> vacancies = vacancyRepository.search(location, pageable);

        return ResponseEntity.ok().body(vacancies);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search for vacancies by location and keyword with support for pagination", nickname = "search")
    public ResponseEntity<Page<Vacancy>> search(@ApiParam(name = "searchParameters", value = "The values supplied to perform the search with", required = true) @RequestBody VacancySearchParameters vacancySearchParameters, Pageable pageable) {
        log.debug("Starting search with vacancySearchParameters: " + vacancySearchParameters.toString());

        Coordinates coordinates = locationService.find(vacancySearchParameters.getLocation().getPlace());

        Page<Vacancy> vacancies;

        if (coordinatesExist(coordinates)) {
            log.debug("Coordinates for " + vacancySearchParameters.getLocation().getPlace() + " with radius of " + vacancySearchParameters.getLocation().getRadius() + " exist");
            SearchParameters searchParameters = SearchParameters.builder()
                    .vacancySearchParameters(vacancySearchParameters)
                    .coordinates(coordinates)
                    .build();
            vacancies = vacancyRepository.search(searchParameters, pageable);
        } else {
            log.debug("No coordinates for " + vacancySearchParameters.getLocation().getPlace() + " with radius of " + vacancySearchParameters.getLocation().getRadius() + " exist");
            vacancies = new PageImpl<>(new ArrayList<Vacancy>());
        }

        return ResponseEntity.ok().body(vacancies);
    }

    private boolean coordinatesExist(Coordinates coordinates) {
        return coordinates != null && coordinates.getLatitude() != null && coordinates.getLongitude() != null;
    }
}
