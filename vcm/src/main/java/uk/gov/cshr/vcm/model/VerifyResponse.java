package uk.gov.cshr.vcm.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.vcm.controller.exception.VacancyError;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResponse {

    private List<Department> departments;

    private VacancyError vacancyError;
}
