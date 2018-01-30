package uk.gov.cshr.vcm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String place;
    private Integer radius;

    @Override
    public String toString() {
        return "Location{" +
                "place='" + place + '\'' +
                ", radius=" + radius +
                '}';
    }
}
