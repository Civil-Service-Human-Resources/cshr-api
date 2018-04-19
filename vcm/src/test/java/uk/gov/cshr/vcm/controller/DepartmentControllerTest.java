package uk.gov.cshr.vcm.controller;

import static java.lang.Math.toIntExact;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.VacancyRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
public class DepartmentControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private VacancyRepository vacancyRepository;

    private MockMvc mvc;

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private final String departmentOneName = "testTitle1 dept";
    private final String departmentTwoName = "testTitle2";


    private final Department requestBodyDepartment = Department.builder()
            .name("department name")
            .build();


    private final String requestBody = "{" +
            "\"name\":\"" + requestBodyDepartment.getName() + "\"" +
            "}";


    @BeforeMethod
    void setup() {

        this.mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        vacancyRepository.deleteAll();
        this.departmentRepository.deleteAll();
    }

    public List<Department> createDepartments(String... departmentNames) {

        List<Department> departments = new ArrayList<>();

        for (String departmentName : departmentNames) {
             Department department = departmentRepository.save(
                     Department.builder().name(departmentName).build());
             departments.add(department);
        }
        return departments;
    }

    @Test
    public void testFindAll() throws Exception {

        DepartmentRepository departmentRepositoryMock = Mockito.mock(DepartmentRepository.class);

        List<Department> departments = createDepartments(
                "xx",
                "zz",
                "yy",                
                departmentTwoName,
                departmentOneName);

        // Given
        String path = "/department";

        // When
        ResultActions sendRequest = mvc.perform(get(path)
			.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.content[0].name", is(departments.get(4).getName())))
                .andExpect(jsonPath("$.content[4].name", is(departments.get(1).getName())));


        sendRequest = mvc.perform(get(path)
			.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE")));

//        departmentRepositorySpy.

        Mockito.verify(departmentRepositoryMock).findAll();

    }

    @Test
    public void testFindById() throws Exception {

        List<Department> departments = createDepartments(departmentOneName, departmentTwoName);

        // Given
        String path = "/department/" + departments.get(0).getId();

        // When
        ResultActions sendRequest = mvc.perform(get(path)
			.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE")));

        MvcResult mvcResult = sendRequest.andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(toIntExact(departments.get(0).getId()))))
                .andExpect(jsonPath("$.disabilityLogo", is(departments.get(0).getDisabilityLogo())))
                .andExpect(jsonPath("$.name", is(departments.get(0).getName())));
    }

    @Test
    public void testFindByIdNotFound() throws Exception {
        // Given
        String path = "/department/-1";

        // When
        ResultActions sendRequest = mvc.perform(get(path)
			.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest.andExpect(status().isNotFound());

    }

    @Test
    public void testCreate() throws Exception {
        // Given
        String path = "/department";

        // When
        ResultActions sendRequest = mvc.perform(post(path)
				.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE"))
				.contentType(APPLICATION_JSON_UTF8).content(requestBody));

        MvcResult sendRequestResult = sendRequest.andReturn();

        String returnedLocation = sendRequestResult.getResponse().getRedirectedUrl();

        // Then
        sendRequest
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));

        Long createdDepartmentId = getResourceIdFromUrl(returnedLocation);

        Department storedDepartment = departmentRepository.findOne(createdDepartmentId);

        Assertions.assertThat(storedDepartment).isEqualToIgnoringGivenFields(requestBodyDepartment, "id");
    }


    @Test
    public void testUpdate() throws Exception {

        List<Department> departments = createDepartments(departmentOneName, departmentTwoName);

        // Given
        String path = "/department/" + departments.get(0).getId();

        // When
        ResultActions sendRequest = mvc.perform(put(path)
				.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE"))
				.contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(toIntExact(departments.get(0).getId()))))
                .andExpect(jsonPath("$.name", is("department name")));
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        // Given
        String path = "/department/-1";

        // When
        ResultActions sendRequest = mvc.perform(put(path)
				.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE"))
				.contentType(APPLICATION_JSON_UTF8).content(requestBody));

        // Then
        sendRequest.andExpect(status().isNotFound());

    }

    private long getResourceIdFromUrl(String locationUrl) {
        String[] parts = locationUrl.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }

    @Test
    public void testDelete() throws Exception {

        List<Department> departments = createDepartments(departmentOneName, departmentTwoName);

        // Given
        String path = "/department/" + departments.get(0).getId();

        // When
        ResultActions sendRequest = mvc.perform(delete(path)
			.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE")));

        Iterable<Department> vacancies = departmentRepository.findAll();

        // Then
        sendRequest.andExpect(status().isNoContent());
        Assertions.assertThat(vacancies).hasSize(1);
        Assertions.assertThat(vacancies.iterator().next()).isEqualToComparingFieldByField(departments.get(1));

    }

    @Test
    public void testFindAllPaginated() throws Exception {

        List<Department> departments = createDepartments(departmentOneName, departmentTwoName);

        // Given
        String path = "/department/?page=0&size=1";

        // When
        ResultActions sendRequest = mvc.perform(get(path)
			.with(user("crudusername").password("crudpassword").roles("CRUD_ROLE")));

        // Then
        sendRequest
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.numberOfElements", is(1)));

    }

}