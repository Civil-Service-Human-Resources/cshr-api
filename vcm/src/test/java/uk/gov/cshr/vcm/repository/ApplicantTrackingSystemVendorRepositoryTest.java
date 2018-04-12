package uk.gov.cshr.vcm.repository;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;

/**
 * Tests {@link ApplicantTrackingSystemVendorRepository}
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
public class ApplicantTrackingSystemVendorRepositoryTest {
    @Inject
    private ApplicantTrackingSystemVendorRepository repository;

    @After
    public void tearDown() {
        repository.deleteAll();
    }

    @Test
    public void testFindByClientIdentifier_doesNotExist() {
        Optional<ApplicantTrackingSystemVendor> vendor = repository.findByClientIdentifier("foo");

        assertThat(vendor.isPresent(), is(false));
    }

    @Test
    public void testFindByClientIdentifier_doesExist() {
        ApplicantTrackingSystemVendor vendor = ApplicantTrackingSystemVendor.builder().id(1L).clientIdentifier("abc").name("ABC ATS").build();
        repository.save(vendor);

        Optional<ApplicantTrackingSystemVendor> actual = repository.findByClientIdentifier("abc");

        assertThat(actual.isPresent(), is(true));
    }
}
