package uk.gov.cshr.vcm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.vcm.VcmApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class VacancyControllerTest extends AbstractJUnit4SpringContextTests {
    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    private static final String REQUEST_PATH = "/vacancy/search?page=0&size=10";

    @Inject
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void search_invalidHttpVerb() throws Exception {
        String requestBody = "{\n" +
                "  \"department\": [],\n" +
                "  \"keyword\": \"search\",\n" +
                "  \"location\": {\n" +
                "    \"place\": \"searchQueryLocation\",\n" +
                "    \"radius\": \"30\"\n" +
                "  }\n" +
                "}";



        mvc.perform(get(REQUEST_PATH).contentType(APPLICATION_JSON_UTF8).content(requestBody)).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    private void performSearchError400Test(String requestBody) throws Exception {
        mvc.perform(post(REQUEST_PATH)
                .contentType(APPLICATION_JSON_UTF8)
                .content(requestBody))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void search_noSearchParametersSupplied() throws Exception {
        performSearchError400Test("");
    }

    @Test
    public void search_noLocationSupplied() throws Exception {
        performSearchError400Test("{\"keyword\": \"foo\"}");
    }
}