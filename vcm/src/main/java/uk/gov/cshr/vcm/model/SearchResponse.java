package uk.gov.cshr.vcm.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import uk.gov.cshr.status.CSHRServiceStatus;
import uk.gov.cshr.vcm.controller.exception.VacancyError;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {

    @ApiModelProperty(notes = "The search results ")
    private Page<Vacancy> vacancies;

    @ApiModelProperty(notes = "A list of errors which may have affected the search results")
    @Builder.Default
    private List<VacancyError> vacancyErrors = new ArrayList<>();

    @ApiModelProperty(notes = "Business status code associated with the request")
    private CSHRServiceStatus cshrServiceStatus;

    @ApiModelProperty(notes = "The email which was verified to give access to non-public jobs")
    private String authenticatedEmail;
}
