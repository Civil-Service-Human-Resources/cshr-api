package uk.gov.cshr.vcm.controller;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.cshr.vcm.model.Vacancy;

/**
 * As org.springframework.data.domain.Page is abstract it cannot be
 * deserialised. This class allows Paged Vacancy results to be deserialised
 * automatically
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VacancyPage extends PageImpl<Vacancy> {

    @JsonCreator
    public VacancyPage(@JsonProperty("content") List<Vacancy> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") Long totalElements) {
        super(content, new PageRequest(number, size), totalElements);
    }
}
