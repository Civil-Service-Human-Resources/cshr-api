package uk.gov.cshr.vcm.repository;

import static uk.gov.cshr.repository.EntityFinder.findOrEmpty;

import java.util.Optional;

import javax.persistence.EntityManager;

import uk.gov.cshr.vcm.model.ApplicantTrackingSystemVendor;

/**
 * This class is responsible for providing custom query methods rather than relying on annotation or method name based queries
 */
public class ApplicationTrackingSystemVendorImpl implements ApplicationTrackingSystemVendorRepositoryCustom {
    private EntityManager em;

    public ApplicationTrackingSystemVendorImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<ApplicantTrackingSystemVendor> findByClientIdentifier(String clientIdentifier) {
        return findOrEmpty(() ->
                (ApplicantTrackingSystemVendor) em.createQuery("select atsv from ApplicantTrackingSystemVendor atsv where atsv.clientIdentifier = :clientIdentifier")
                .setParameter("clientIdentifier", clientIdentifier)
                .getSingleResult());
    }
}
