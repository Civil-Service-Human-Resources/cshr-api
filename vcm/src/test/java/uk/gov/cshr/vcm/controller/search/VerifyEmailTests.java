package uk.gov.cshr.vcm.controller.search;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.EmailExtension;
import uk.gov.cshr.vcm.model.VerifyRequest;
import uk.gov.cshr.vcm.model.VerifyResponse;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.cshr.vcm.repository.EmailExtensionRepository;
import uk.gov.cshr.vcm.service.NotifyService;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
@Ignore
public class VerifyEmailTests extends AbstractTestNGSpringContextTests {

    public static final double BRISTOL_LATITUDE = 51.4549291;
    public static final double BRISTOL_LONGITUDE = -2.6278111;

    public static final double NEWCASTLE_LATITUDE = 54.9806308;
    public static final double NEWCASTLE_LONGITUDE = -1.6167437;

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Inject
    private WebApplicationContext webApplicationContext;

    @Inject
    private DepartmentRepository departmentRepository;

    @Inject
    private EmailExtensionRepository emailExtensionRepository;

    private MockMvc mockMvc;

    @MockBean
    private NotifyService notifyService;

    private Department department1;
    private Department department2;

    @Before
    public void before() throws LocationServiceException {

        emailExtensionRepository.deleteAll();
        departmentRepository.deleteAll();

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        department1 = Department.builder()
                .name("Department One")
                .disabilityLogo("disabilityLogo")
                .build();

        EmailExtension emailExtension = EmailExtension.builder()
                .department(department1)
                .emailExtension("cabinetoffice.gov.uk")
                .build();

        department1.getAcceptedEmailExtensions().add(emailExtension);
        department1 = departmentRepository.save(department1);

    }

    @After
    public void after() {
    }


    @Test
    public void testVerifyEmailOneDepartment() throws Exception {

        VerifyRequest verifyRequest = VerifyRequest.builder()
                .emailAddress("test@cabinetoffice.gov.uk")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(verifyRequest);

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/verifyemail")
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    public void testVerifyEmailMultipleDepartments() throws Exception {

        department2 = Department.builder()
                .name("Department two")
                .disabilityLogo("disabilityLogo")
                .build();

        EmailExtension emailExtension = EmailExtension.builder()
                .department(department2)
                .emailExtension("cabinetoffice.gov.uk")
                .build();

        department2.getAcceptedEmailExtensions().add(emailExtension);
        department2 = departmentRepository.save(department2);

        VerifyRequest verifyRequest = VerifyRequest.builder()
                .emailAddress("test@cabinetoffice.gov.uk")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(verifyRequest);

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/verifyemail")
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();

        String searchResponse = mvcResult.getResponse().getContentAsString();

        VerifyResponse verifyResponse = objectMapper.readValue(searchResponse, VerifyResponse.class);

        Assert.assertEquals(2, verifyResponse.getDepartments().size());
    }

    @Test
    public void testVerifyEmailChooseDepartment() throws Exception {

        department2 = Department.builder()
                .name("Department two")
                .disabilityLogo("disabilityLogo")
                .build();

        EmailExtension emailExtension = EmailExtension.builder()
                .department(department2)
                .emailExtension("cabinetoffice.gov.uk")
                .build();

        department2.getAcceptedEmailExtensions().add(emailExtension);
        department2 = departmentRepository.save(department2);

        VerifyRequest verifyRequest = VerifyRequest.builder()
                .emailAddress("test@cabinetoffice.gov.uk")
                .departmentID(department2.getId())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(verifyRequest);

        MvcResult mvcResult = this.mockMvc.perform(post("/vacancy/verifyemail")
                .with(user("searchusername").password("searchpassword").roles("SEARCH_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent())
                .andReturn();
    }
}
