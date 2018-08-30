package uk.gov.cshr.vcm.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.UrlValidator;

/**
 * This class is responsible for sanitising the apply url to ensure it is formatted correctly.
 */
@Slf4j
public class ApplyURLSanitiser {
    public static Vacancy sanitise(Vacancy vacancy) {
        String originalURL = vacancy.getApplyURL();

        String url = vacancy.getApplyURL();

        if (url != null && !url.toLowerCase().matches("^\\w+://.*")) {
            url = "https://" + url;
        }

        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (!urlValidator.isValid(url)) {
            url = null;
        }

        log.debug("setting applyurl '" + originalURL + "' to: " + url);
        vacancy.setApplyURL(url);

        return vacancy;
    }
}
