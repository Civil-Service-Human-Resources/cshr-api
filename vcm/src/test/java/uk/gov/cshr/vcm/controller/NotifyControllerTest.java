package uk.gov.cshr.vcm.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.LocationServiceException;
import uk.gov.cshr.vcm.model.Notification;
import uk.gov.cshr.vcm.service.NotifyService;
import uk.gov.service.notify.NotificationClientException;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = VcmApplication.class)
@ContextConfiguration
@WebAppConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class NotifyControllerTest extends AbstractTestNGSpringContextTests {

    final private MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Inject
    private WebApplicationContext webApplicationContext;

    @MockBean
    private NotifyService notifyService;

    private MockMvc mockMvc;

    @Before
    public void before() throws LocationServiceException {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }


    @Test
    public void testNotify() throws Exception {

        reset(notifyService);
        when(notifyService.notify(any())).thenReturn(true);

        Notification notification = new Notification();
        notification.setEmail("anyemail@anyemail.com");
        notification.setNotifyCode("1234");
        notification.setTemplateID("xyz");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(notification);

        MvcResult mvcResult = this.mockMvc.perform(post("/notify")
                .with(user("notifyusername").password("notifypassword").roles("NOTIFY_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testNotifyFail() throws Exception {

        Notification notification = new Notification();

        when(notifyService.notify(any())).thenThrow(NotificationClientException.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(notification);

        MvcResult mvcResult = this.mockMvc.perform(post("/notify")
                .with(user("notifyusername").password("notifypassword").roles("NOTIFY_ROLE"))
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
                .accept(APPLICATION_JSON_UTF8))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }
}
