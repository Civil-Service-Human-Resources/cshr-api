package uk.gov.cshr.vcm.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.cshr.status.CSHRServiceStatus;

/**
 * The REST response for getting vacancy metadata.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyMetadataResponse {
    private List<VacancyMetadata> vacancies;
    private CSHRServiceStatus responseStatus;
}
