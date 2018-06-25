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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.controller.exception.SearchStatusCode;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.SearchResponse;
import uk.gov.cshr.vcm.model.VacancyEligibility;
import uk.gov.cshr.vcm.repository.DepartmentRepository;
import uk.gov.service.notify.NotificationClientException;

@Service
public class CshrAuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(CshrAuthenticationService.class);

	private static final String SECRET = UUID.randomUUID().toString();

	@Inject
	private DepartmentRepository departmentRepository;

	public String createInternalJWT(String emailAddress) throws NotificationClientException {

		List<Department> departments = verifyEmailAddress(emailAddress);
		StringBuilder stringBuilder = new StringBuilder();

		for (Department department : departments) {
			stringBuilder.append(department.getId() + ",");

			System.out.println(department.getName());
		}

		if( ! departments.isEmpty() ) {

			Date date = Date.from(
					LocalDateTime
							.now()
							.plusDays(1)
							.atZone(ZoneId.systemDefault())
							.toInstant());

			String compactJws = Jwts.builder()
					.setSubject("internal candidate")
					.claim("Vacancy Eligibility", VacancyEligibility.ACROSS_GOVERNMENT.toString())
                    .claim("Email Address", emailAddress)
					.claim("Departments", stringBuilder.toString())
					.signWith(SignatureAlgorithm.HS512, SECRET)
					.setExpiration(date)
					.compact();

			log.debug("jwt=" + compactJws);
			return compactJws;
		}
		else {
			return null;
		}
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

			if ( eligibilityClaim != null ) {
				return VacancyEligibility.valueOf(eligibilityClaim.toString());
			}
			else {
                VacancyError vacancyError = VacancyError.builder()
                        .searchStatusCode(SearchStatusCode.JWT_NO_ELIGIBILITY_CLAIM)
                        .build();
                searchResponse.getVacancyErrors().add(vacancyError);
				return VacancyEligibility.PUBLIC;
			}
		}
		// Who knows what exceptions Jwts.parser may throw
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

	@Cacheable("emailAddresses")
	public List<Department> verifyEmailAddress(String emailAddress) {

		System.out.println("emailAddresses");

		List<Department> departmentIDs = new ArrayList<>();

		Iterable<Department> departments = departmentRepository.findAll();

		departments.forEach(d -> {
			Set<String> emailExtensions = d.getAcceptedEmailExtensions();
			for (String emailExtension : emailExtensions) {

				if ( emailExtension.trim().isEmpty() ) {
					continue;
				}

				String pattern = emailExtension.replace(".", "\\.").replace("x", "([^$]*)");
				pattern = "([^$]*)" + pattern;
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(emailAddress);

				if ( m.find() ) {
					departmentIDs.add(d);
				}
			}
		});

//		return emailAddress.endsWith(".gov.uk")
//				|| emailAddress.endsWith("valtech.co.uk")
//                || emailAddress.endsWith("cabinetoffice.gov.uk");

		return departmentIDs;
	}
}
