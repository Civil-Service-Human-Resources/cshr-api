package uk.gov.cshr.vcm.model;

import java.util.ArrayList;
import java.util.List;
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

    private Page<Vacancy> vacancies;

    @Builder.Default
    private List<VacancyError> vacancyErrors = new ArrayList<>();

    @Builder.Default
    private List<CSHRServiceStatus> cshrServiceStatuses = new ArrayList<>();

    private String authenticatedEmail;
}
