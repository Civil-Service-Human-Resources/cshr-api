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

    public String getLocation() {
        return vacancySearchParameters != null ? vacancySearchParameters.getLocation().getPlace() : null;
    }

    public Integer getRadius() {
        return vacancySearchParameters != null ? vacancySearchParameters.getLocation().getRadius() : null;
    }

    public String getKeyword() {
        return vacancySearchParameters != null ? vacancySearchParameters.getKeyword() : null;
    }

    public String[] getDepartment() {
        return vacancySearchParameters != null ? vacancySearchParameters.getDepartment() : null;
    }

    public Double getLongitude() {
        return coordinates != null ? coordinates.getLongitude() : null;
    }

    public Double getLatitude() {
        return coordinates != null ? coordinates.getLatitude() : null;
    }

    public Integer getSalaryMin() {
        return vacancySearchParameters != null ? vacancySearchParameters.getMinSalary() : null;
    }

    public Integer getSalaryMax() {
        return vacancySearchParameters != null ? vacancySearchParameters.getMaxSalary() : null;
    }
}
