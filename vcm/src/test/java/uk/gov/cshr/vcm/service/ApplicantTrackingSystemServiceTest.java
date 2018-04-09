package uk.gov.cshr.vcm.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.error.CSHRServiceStatus;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.controller.exception.InvalidApplicantTrackingSystemException;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;
import uk.gov.cshr.vcm.repository.ApplicantTrackingSystemVendorRepository;

/**
 * Tests {@link ApplicantTrackingSystemServiceImpl}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
@TestExecutionListeners(MockitoTestExecutionListener.class)
public class ApplicantTrackingSystemServiceTest extends AbstractJUnit4SpringContextTests {
    @Inject
    private ApplicantTrackingSystemService service;

    @MockBean
    private ApplicantTrackingSystemVendorRepository repository;

    @Test
    public void testValidateClientIdentifier_identifierIsUnknown() {
        String identifier = "foo";

        try {
            when(repository.findByClientIdentifier(identifier)).thenReturn(new ArrayList<>());
            service.validateClientIdentifier(identifier);
        } catch (RuntimeException re) {
            assertThat(re, instanceOf(InvalidApplicantTrackingSystemException.class));
            CSHRServiceStatus status = ((InvalidApplicantTrackingSystemException) re).getCshrServiceStatus();
            assertThat(status.getCode(), is(equalTo("CSHR_200")));
        }
    }

    @Test
    public void testValidateClientIdentifier_identifierIsKnown() {
        String identifier = "foo";

        try {
            List<ApplicantTrackingSystemVendor> vendors = new ArrayList<>();
            //Not interested in content code only expects one or more results to be valid
            vendors.add(ApplicantTrackingSystemVendor.builder().build());

            when(repository.findByClientIdentifier(identifier)).thenReturn(vendors);
            service.validateClientIdentifier(identifier);
        } catch (RuntimeException re) {
            fail("No Exception should have been thrown");
        }
    }
}
