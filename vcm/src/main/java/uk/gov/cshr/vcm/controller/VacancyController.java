package uk.gov.cshr.vcm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.repository.VacancyRepository;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/vacancy", produces = MediaType.APPLICATION_JSON_VALUE)
public class VacancyController {

    private final VacancyRepository vacancyRepository;

    @Autowired
    VacancyController(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Vacancy>> findAll() {
        List<Vacancy> vacancies = vacancyRepository.findAll();
        return ResponseEntity.ok().body(vacancies);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Vacancy> findById(@PathVariable Long vacancyId) {

        Optional<Vacancy> foundVacancy = vacancyRepository.findById(vacancyId);

        ResponseEntity<Vacancy> notFound = ResponseEntity.notFound().build();

        return foundVacancy.map(vacancy -> ResponseEntity.ok().body(vacancy)).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Vacancy> create(@RequestBody Vacancy vacancy) {

        Vacancy savedVacancy = vacancyRepository.save(vacancy);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedVacancy.getId()).toUri();

        return ResponseEntity.created(location).body(savedVacancy);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public ResponseEntity<Vacancy> deleteById(@PathVariable Long vacancyId) {

        vacancyRepository.delete(vacancyId);
        return ResponseEntity.noContent().build();

    }

    @RequestMapping(method = RequestMethod.GET, value = "/search/location/{location}/keyword/{keyword}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Vacancy>> search(@PathVariable String location, @PathVariable String keyword) {
        List<Vacancy> vacancies = vacancyRepository.search(location, keyword);
        return ResponseEntity.ok().body(vacancies);
    }
}
