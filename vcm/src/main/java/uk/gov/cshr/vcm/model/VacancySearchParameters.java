package uk.gov.cshr.vcm.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "VacancySearchParameters", description = "The parameters allowed for when searching for vacancies")
public class VacancySearchParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "location of the vacancy")
    private Location location;

    @ApiModelProperty(value = "word found in title or description of vacancy")
    private String keyword;

    @ApiModelProperty(value = "id of one or more departments to search vacancies for")
    private String[] department;

    private Integer maxSalary;

    private Integer minSalary;

    private Boolean overseasJob;

    @ApiModelProperty(notes = "A string array of contract types",
                      example = "FULL_TIME PART_TIME CONTRACT TEMPORARY SEASONAL INTERNSHIP")
    private String[] contractTypes;

    @ApiModelProperty(notes = "A string array of working patterns",
                      example = "FLEXIBLE_WORKING FULL_TIME PART_TIME JOB_SHARE HOME_WORKING")
	private String[] workingPatterns;

	@ApiModelProperty
	private VacancyEligibility vacancyEligibility;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
