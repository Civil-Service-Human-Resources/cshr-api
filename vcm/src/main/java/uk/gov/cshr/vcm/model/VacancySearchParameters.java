package uk.gov.cshr.vcm.model;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="VacancySearchParameters", description="The parameters allowed for when searching for vacancies")
public class VacancySearchParameters implements Serializable {
    @ApiModelProperty(value = "location of the vacancy", required = true)
    @NonNull
    private String location;
    @ApiModelProperty(value = "word found in title or description of vacancy")
    private String keyword;
    @ApiModelProperty(value = "id of one or more departments to search vacancies for")
    private String[] department;
}
