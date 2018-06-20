package uk.gov.cshr.vcm.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.model.VacancyEligibility;
import uk.gov.service.notify.NotificationClientException;

@Service
public class CshrAuthenticationService {

	private static final Logger log = LoggerFactory.getLogger(CshrAuthenticationService.class);

	private static final String SECRET = UUID.randomUUID().toString();

	public String createInternalJWT(String emailAddress) throws NotificationClientException {

		if( verifyEmailAddress(emailAddress) ) {

			Date date = Date.from(
					LocalDateTime
							.now()
							.plusDays(1)
							.atZone(ZoneId.systemDefault())
							.toInstant());

			String compactJws = Jwts.builder()
					.setSubject("internal candidate")
					.claim("Vacancy Eligibility", VacancyEligibility.ACROSS_GOVERNMENT.toString())
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
	 * @return
	 */
	public VacancyEligibility parseVacancyEligibility(String jwt) {

		if (jwt == null) {
			return VacancyEligibility.PUBLIC;
		}

		try {

			Claims claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET))
				.parseClaimsJws(jwt).getBody();

			Object eligibilityClaim = claims.get("Vacancy Eligibility");

			if ( eligibilityClaim != null ) {
				return VacancyEligibility.valueOf(eligibilityClaim.toString());
			}
			else {
				return VacancyEligibility.PUBLIC;
			}
		}
		catch(IllegalArgumentException | MalformedJwtException e) {
			log.error(e.getMessage(), e);
			return VacancyEligibility.PUBLIC;
		}
	}

	private boolean verifyEmailAddress(String emailAddress) {

		return emailAddress.endsWith(".gov.uk")
				|| emailAddress.endsWith("valtech.co.uk")
                || emailAddress.endsWith("cabinetoffice.gov.uk");
	}
}
