package uk.gov.cshr.vcm.model;

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

    private VacancyError vacancyError;

    private CSHRServiceStatus cshrServiceStatus;
}
