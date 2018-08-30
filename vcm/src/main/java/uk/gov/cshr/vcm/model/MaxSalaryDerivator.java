package uk.gov.cshr.vcm.model;

/**
 * This class is responsible for ensuring a vacancy has a value for the max salary property.
 */
public class MaxSalaryDerivator {
    /**
     * This method is responsible for setting the value of the max salary property if none exists.
     *
     * If no max salary exists it will be set to the value of the min salary.
     *
     * @param source vacancy to have its max salary derived if required
     * @return source vacancy with max salary set as required.
     */
    public static Vacancy deriveMaxSalary(Vacancy source) {
        if (source.getSalaryMax() == null) {
            source.setSalaryMax(source.getSalaryMin());
        }

        return source;
    }
}
