package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.repository.DepartmentRepository;

@RestController
@RequestMapping(value = "/department", produces = MediaType.APPLICATION_JSON_VALUE)
public class DepartmentController {

	private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentRepository departmentRepository;

    @Autowired
    DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
	@ApiOperation(value = "Find all departments", nickname = "findAll")
    public ResponseEntity<Page<Department>> findAll(Pageable pageable) {
        Page<Department> departments = departmentRepository.findAll(pageable);
        return ResponseEntity.ok().body(departments);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{departmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Find a specific department", nickname = "findById")
    public ResponseEntity<Department> findById(@PathVariable Long departmentId) {

        Optional<Department> foundDepartment = departmentRepository.findById(departmentId);

		if ( ! foundDepartment.isPresent() ) {
			log.debug("No department found for id " + departmentId);
		}

        ResponseEntity<Department> notFound = ResponseEntity.notFound().build();

        return foundDepartment.map(department -> ResponseEntity.ok().body(department)).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Department> create(@RequestBody Department department) {

        Department savedDepartment = departmentRepository.save(department);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedDepartment.getId()).toUri();

        return ResponseEntity.created(location).body(savedDepartment);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{departmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Department> update(@PathVariable Long departmentId, @RequestBody Department departmentUpdate) {

        Optional<Department> foundDepartment = departmentRepository.findById(departmentId);

		if ( ! foundDepartment.isPresent() ) {
			log.error("No department found for id " + departmentId);
		}

        ResponseEntity<Department> notFound = ResponseEntity.notFound().build();

        return foundDepartment.map((Department department) -> {
            // Attention, mutable state on the argument
            departmentUpdate.setId(department.getId());
            departmentRepository.save(departmentUpdate);
            return ResponseEntity.ok().body(department);
        }).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{departmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Department> deleteById(@PathVariable Long departmentId) {

        departmentRepository.delete(departmentId);
        return ResponseEntity.noContent().build();
    }
}
