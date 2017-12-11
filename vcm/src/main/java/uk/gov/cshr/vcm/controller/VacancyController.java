package uk.gov.cshr.vcm.controller;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.repository.VacancyRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Iterable<Vacancy> source = vacancyRepository.findAll();
        List<Vacancy> vacancies = Lists.newArrayList(source);
        return ResponseEntity.ok().body(vacancies);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Vacancy> findById(@PathVariable Long vacancyId) {

        Vacancy vacancy = vacancyRepository.findOne(vacancyId);

        if (vacancy == null) return ResponseEntity.notFound().build();
        else return ResponseEntity.ok().body(vacancy);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Vacancy> create(@RequestBody Vacancy vacancy) {

        Vacancy savedVacancy = vacancyRepository.save(vacancy);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedVacancy.getId()).toUri();

        return ResponseEntity.created(location).body(savedVacancy);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{vacancyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Vacancy> deleteById(@PathVariable Long vacancyId) {

        vacancyRepository.delete(vacancyId);
        return ResponseEntity.noContent().build();

    }
}
