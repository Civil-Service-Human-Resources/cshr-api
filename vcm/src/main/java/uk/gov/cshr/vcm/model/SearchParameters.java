package uk.gov.cshr.vcm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is responsible for aggregating input search parameters with the supplied location as a coordinate represented by a longitude and latitude.
 *
 * This required so that validation marshalling and unmarshalling VacancySearchParameters works correctly.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParameters {

    private VacancySearchParameters vacancySearchParameters;
    private Coordinates coordinates;
}
