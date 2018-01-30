package uk.gov.cshr.vcm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinates {
    private Double longitude;
    private Double latitude;
}
