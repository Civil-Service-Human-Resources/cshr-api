package uk.gov.cshr.vcm.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Location", description = "The parameters that make a location for search made of a place and a radius to search from this place")
public class Location implements Serializable {
    @ApiModelProperty(value = "the place being searched from", required = true)
    private String place;
    @ApiModelProperty(value = "the distance in miles to search from the given place", required = true)
    private Integer radius;

    @Override
    public String toString() {
        return "Location{" +
                "place='" + place + '\'' +
                ", radius=" + radius +
                '}';
    }
}