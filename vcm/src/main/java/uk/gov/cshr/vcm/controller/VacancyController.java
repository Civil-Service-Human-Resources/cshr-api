package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.repository.VacancyRepository;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(value = "/vacancy", produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
@Api(value = "vacancyservice", description = "Operations pertaining to vacancies for jobs in Government")
public class VacancyController {

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

        // test change

        ResponseEntity<Vacancy> notFound = ResponseEntity.notFound().build();

        return foundVacancy.map(vacancy -> ResponseEntity.ok().body(vacancy)).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value="create", nickname = "create")
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

        ResponseEntity<Vacancy> notFound = ResponseEntity.notFound().build();

        return foundVacancy.map((Vacancy vacancy) -> {
            // Attention, mutable state on the argument
            vacancyUpdate.setId(vacancy.getId());
            vacancyRepository.save(vacancyUpdate);
            return ResponseEntity.ok().body(vacancy);
        }).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value="delete", nickname = "delete")
    public ResponseEntity<Vacancy> deleteById(@PathVariable Long vacancyId) {

        vacancyRepository.delete(vacancyId);
        return ResponseEntity.noContent().build();

    }

    @RequestMapping(method = RequestMethod.GET, value = "/search/location/{location}/keyword/{keyword}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search for vacancies by location and keyword with support for pagination", nickname = "searchByLocationAndKeyword")
    public ResponseEntity<Page<Vacancy>> search(@PathVariable String location, @PathVariable String keyword, Pageable pageable) {
        Page<Vacancy> vacancies = vacancyRepository.search(location, keyword, pageable);

        return ResponseEntity.ok().body(vacancies);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/search/location/{location}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Search for vacancies by location", nickname = "searchByLocation")
    public ResponseEntity<Page<Vacancy>> search(@PathVariable String location, Pageable pageable) {
        Page<Vacancy> vacancies = vacancyRepository.search(location, pageable);

        return ResponseEntity.ok().body(vacancies);
    }
}
