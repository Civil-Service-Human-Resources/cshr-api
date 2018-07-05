package uk.gov.cshr.vcm.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.SearchStatusCode;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.EmailExtension;
import uk.gov.cshr.vcm.model.SearchResponse;
import uk.gov.cshr.vcm.model.VacancyEligibility;
import uk.gov.cshr.vcm.repository.EmailExtensionRepository;
import uk.gov.service.notify.NotificationClientException;

@Service
public class CshrAuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(CshrAuthenticationService.class);

    @Value("${spring.cshrAuthenticationService.secret}")
    private String SECRET;

    @Inject
    private EmailExtensionRepository emailExtensionRepository;

	public String createInternalJWT(String emailAddress, Department department) throws NotificationClientException {

        Date date = Date.from(
                LocalDateTime
                        .now()
                        .plusDays(1)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

        String compactJws = Jwts.builder()
                .setSubject("internal candidate")
                .claim("Vacancy Eligibility", VacancyEligibility.INTERNAL.toString())
                .claim("Email Address", emailAddress)
                .claim("Department ID", department.getId())
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .setExpiration(date)
                .compact();

        log.debug("jwt=" + compactJws);
        return compactJws;
	}

	/**
	 * Will return VacancyEligibility.PUBLIC if the JWT is invalid or null
	 * @param jwt
     * @param searchResponse
	 * @return
	 */
	public VacancyEligibility parseVacancyEligibility(String jwt, SearchResponse searchResponse) {

		if (jwt == null) {
            VacancyError vacancyError = VacancyError.builder()
                    .searchStatusCode(SearchStatusCode.NULL_JWT)
                    .build();
            searchResponse.getVacancyErrors().add(vacancyError);
            return VacancyEligibility.PUBLIC;
		}

		try {

			Claims claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET))
				.parseClaimsJws(jwt).getBody();

            String emailAddress = claims.get("Email Address", String.class);
            searchResponse.setAuthenticatedEmail(emailAddress);

			Object eligibilityClaim = claims.get("Vacancy Eligibility");
            Object departmentID = claims.get("Department ID");

			if ( eligibilityClaim != null ) {
				VacancyEligibility vacancyEligibility = VacancyEligibility.valueOf(eligibilityClaim.toString());
                vacancyEligibility.setEmailAddress(emailAddress);
                if (departmentID != null) {
                    vacancyEligibility.setDepartmentID(Long.parseLong(departmentID.toString()));
                }
                return vacancyEligibility;
			}
			else {
                VacancyError vacancyError = VacancyError.builder()
                        .searchStatusCode(SearchStatusCode.JWT_NO_ELIGIBILITY_CLAIM)
                        .build();
                searchResponse.getVacancyErrors().add(vacancyError);
				return VacancyEligibility.PUBLIC;
			}
		}
		catch(ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            
			log.error(e.getMessage(), e);
            VacancyError vacancyError = VacancyError.builder()
                    .message(e.getMessage())
                    .searchStatusCode(SearchStatusCode.INVALID_JWT)
                    .build();
                searchResponse.getVacancyErrors().add(vacancyError);
			return VacancyEligibility.PUBLIC;
		}
	}

	public Set<Department> verifyEmailAddress(String emailAddress) {

		Set<Department> departments = new HashSet<>();

        Iterable<EmailExtension> emailExtensions = emailExtensionRepository.findAll();

        emailExtensions.forEach(e -> {
            if ( emailAddress.endsWith(e.getEmailExtension()) ) {
                departments.add(e.getDepartment());
            }
        });

		return departments;
	}
}
