package uk.gov.cshr.vcm.controller;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.cshr.vcm.controller.exception.VacancyError;
import uk.gov.cshr.vcm.model.SearchResponse;

/**
 * As org.springframework.data.domain.Page is abstract it cannot be
 * deserialised. This class allows Paged Vacancy results to be deserialised
 * automatically
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponsePage extends SearchResponse {

    @JsonCreator
    public SearchResponsePage(
            @JsonProperty("vacancies") VacancyPage vacancyPage,
            @JsonProperty("vacanyErrors") List<VacancyError> vacanyErrors,
            @JsonProperty("cshrServiceStatus") CSHRServiceStatus cshrServiceStatus,
            @JsonProperty("authenticatedEmail") String authenticatedEmail) {

        super(vacancyPage, vacanyErrors, cshrServiceStatus, authenticatedEmail);
    }
}
