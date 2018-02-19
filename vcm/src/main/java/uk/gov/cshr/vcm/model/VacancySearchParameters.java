package uk.gov.cshr.vcm.model;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "VacancySearchParameters", description = "The parameters allowed for when searching for vacancies")
public class VacancySearchParameters implements Serializable {

    @ApiModelProperty(value = "location of the vacancy", required = true)
    @NonNull
    private Location location;

    @ApiModelProperty(value = "word found in title or description of vacancy")
    private String keyword;

    @ApiModelProperty(value = "id of one or more departments to search vacancies for")
    private String[] department;

    private Integer maxSalary;

    private Integer minSalary;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
