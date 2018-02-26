package uk.gov.cshr.vcm.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinates implements Serializable {

    private Double longitude;
    private Double latitude;
    private String region;
}
