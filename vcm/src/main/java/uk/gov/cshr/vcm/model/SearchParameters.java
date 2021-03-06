package uk.gov.cshr.vcm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * This class is responsible for aggregating input search parameters with the supplied location as a coordinate represented by a longitude and latitude.
 * <p>
 * This required so that validation marshalling and unmarshalling VacancySearchParameters works correctly.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParameters {
    @NonNull
    private VacancySearchParameters vacancySearchParameters;
    private Coordinates coordinates;
    private VacancyEligibility vacancyEligibility;
}
