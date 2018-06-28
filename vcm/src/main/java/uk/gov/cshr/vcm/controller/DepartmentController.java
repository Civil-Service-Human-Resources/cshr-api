package uk.gov.cshr.vcm.controller;

import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.security.RolesAllowed;
import liquibase.util.csv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.EmailExtension;
import uk.gov.cshr.vcm.repository.DepartmentRepository;

@RestController
@RequestMapping(value = "/department", produces = MediaType.APPLICATION_JSON_VALUE)
@RolesAllowed("CRUD_ROLE")
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
	@RolesAllowed({"CRUD_ROLE", "SEARCH_ROLE"})
    public ResponseEntity<Page<Department>> findAll(Pageable pageable) {
        Page<Department> departments = departmentRepository.findAllByOrderByNameAsc(pageable);
        return ResponseEntity.ok().body(departments);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{departmentId}")
    @ApiOperation(value = "Find a specific department", nickname = "findById")
    public ResponseEntity<Department> findById(@PathVariable Long departmentId) {

        Optional<Department> foundDepartment = departmentRepository.findById(departmentId);

        if (log.isDebugEnabled() && !foundDepartment.isPresent()) {
            log.debug(String.format("No department found for id %d", departmentId));
        }

        ResponseEntity<Department> notFound = ResponseEntity.notFound().build();

        return foundDepartment.map(department -> ResponseEntity.ok().body(department)).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create a department", nickname = "create")
    public ResponseEntity<Department> create(@RequestBody Department department) {

        Department savedDepartment = departmentRepository.save(department);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedDepartment.getId()).toUri();

        return ResponseEntity.created(location).body(savedDepartment);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{departmentId}")
    @ApiOperation(value = "Update a department", nickname = "update")
    public ResponseEntity<Department> update(@PathVariable Long departmentId, @RequestBody Department departmentUpdate) {

        Optional<Department> foundDepartment = departmentRepository.findById(departmentId);

        if (log.isErrorEnabled() && !foundDepartment.isPresent()) {
            log.error(String.format("No department found for id %d", departmentId));
        }

        ResponseEntity<Department> notFound = ResponseEntity.notFound().build();

        return foundDepartment.map((Department department) -> {
            // Attention, mutable state on the argument
            departmentUpdate.setId(department.getId());
            departmentRepository.save(departmentUpdate);
            return ResponseEntity.ok().body(department);
        }).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{departmentId}")
    @ApiOperation(value = "Delete a department", nickname = "deleteById")
    public ResponseEntity<Department> deleteById(@PathVariable Long departmentId) {

        departmentRepository.delete(departmentId);
        return ResponseEntity.noContent().build();
    }

	@CacheEvict(value = "emailAddresses", allEntries = true)
    @RequestMapping(method = RequestMethod.PUT, value = "/load")
    @ApiOperation(value = "load departments", nickname = "load")
    public ResponseEntity<Department> load() throws IOException {

        Reader reader = Files.newBufferedReader(Paths.get("src/test/resources/RPGDepartmentDataMaster.csv"));
        CSVReader csvReader = new CSVReader(reader);

		// skip first line
		csvReader.readNext();

        String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {
            System.out.println("id : " + nextRecord[0]);
            String name = nextRecord[1];
            System.out.println("Phone : " + nextRecord[2]);
            String emails = nextRecord[8];
            System.out.println("==========================");

			Department department = new Department();
			department.setName(name);

			Set<EmailExtension> emailSet = new HashSet<>();
            List<String> emailsList = Arrays.asList(emails.split("\n"));

            for (String string : emailsList) {
                emailSet.add( EmailExtension.builder().emailExtension(string).build());
            }

			department.setAcceptedEmailExtensions(emailSet);

			departmentRepository.save(department);
        }
        return ResponseEntity.noContent().build();
    }
}
