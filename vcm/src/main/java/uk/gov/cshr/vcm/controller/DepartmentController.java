package uk.gov.cshr.vcm.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.service.DepartmentService;

@RestController
@RequestMapping(value = "/department", produces = MediaType.APPLICATION_JSON_VALUE)
@RolesAllowed("CRUD_ROLE")
@Slf4j
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private DepartmentService departmentService;

    @Autowired
    DepartmentController(DepartmentRepository departmentRepository, DepartmentService departmentService) {
        this.departmentRepository = departmentRepository;
        this.departmentService = departmentService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Find all departments", nickname = "findAllDepartments")
    @RolesAllowed({"CRUD_ROLE", "SEARCH_ROLE"})
    public ResponseEntity<Page<Department>> findAll(Pageable pageable) {
        Page<Department> departments = departmentRepository.findAllByOrderByNameAsc(pageable);
        return ResponseEntity.ok().body(departments);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{departmentId}")
    @ApiOperation(value = "Find a specific department", nickname = "findDepartmentById")
    public ResponseEntity<Department> findById(@PathVariable Long departmentId) {

        Optional<Department> foundDepartment = departmentRepository.findById(departmentId);

        if (log.isDebugEnabled() && !foundDepartment.isPresent()) {
            log.debug(String.format("No department found for id %d", departmentId));
        }

        ResponseEntity<Department> notFound = ResponseEntity.notFound().build();
        return foundDepartment.map(department -> ResponseEntity.ok().body(department)).orElse(notFound);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create a department", nickname = "createDepartment")
    public ResponseEntity<Department> create(@RequestBody Department department) {

        Department savedDepartment = departmentRepository.save(department);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedDepartment.getId()).toUri();

        return ResponseEntity.created(location).body(savedDepartment);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{departmentId}")
    @ApiOperation(value = "Update a department", nickname = "updateDepartment")
    public ResponseEntity<Department> update(@PathVariable Long departmentId, @RequestBody Department departmentUpdate) {

        Optional<Department> foundDepartment = departmentRepository.findById(departmentId);

        if (log.isErrorEnabled() && !foundDepartment.isPresent()) {
            log.error(String.format("No department found for id %d", departmentId));
        }

        ResponseEntity<Department> notFound = ResponseEntity.notFound().build();

        return foundDepartment.map((Department department) -> {
            // Attention, mutable state on the argument
            departmentUpdate.setId(department.getId());
            if (departmentUpdate.getAcceptedEmailExtensions() == null) {
                departmentUpdate.setAcceptedEmailExtensions(department.getAcceptedEmailExtensions());
            }
            departmentRepository.save(departmentUpdate);
            return ResponseEntity.ok().body(department);
        }).orElse(notFound);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/{departmentId}")
    @ApiOperation(value = "Delete a department", nickname = "deleteDepartmentById")
    public ResponseEntity<Department> deleteById(@PathVariable Long departmentId) {

        departmentRepository.delete(departmentId);
        return ResponseEntity.noContent().build();
    }

    @CacheEvict(value = "departments", allEntries = true)
    @RequestMapping(method = RequestMethod.POST, value = "/loaddepartments")
    @ApiOperation(value = "load departments", nickname = "load")
    public ResponseEntity<?> load(@RequestParam("file") MultipartFile file)
            throws IOException, ParseException {

        try (InputStream inputStream = file.getInputStream()) {
            departmentService.readDepartments(inputStream);
            return ResponseEntity.noContent().build();
        }
    }
}
