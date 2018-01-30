package uk.gov.cshr.vcm.service;

import uk.gov.cshr.vcm.model.Coordinates;

import java.util.HashMap;
import java.util.Map;

public class LocationService {
    private static final Map<String, Coordinates> COORDINATES_MAP = new HashMap<>();

    static {
        COORDINATES_MAP.put("BA14", Coordinates.builder().latitude(51.3202692).longitude(-2.2150652).build());
        COORDINATES_MAP.put("BRISTOL", Coordinates.builder().latitude(51.468468).longitude(-2.6609202).build());
        COORDINATES_MAP.put("BS1", Coordinates.builder().latitude(51.4548812).longitude(-2.6079938).build());
        COORDINATES_MAP.put("BS13", Coordinates.builder().latitude(51.415705).longitude(-2.6304586).build());
        COORDINATES_MAP.put("BS20", Coordinates.builder().latitude(51.4744213).longitude(-2.7715351).build());
        COORDINATES_MAP.put("HA9", Coordinates.builder().latitude(51.5609154).longitude(-0.3053616).build());
        COORDINATES_MAP.put("LONDON", Coordinates.builder().latitude(51.5285578).longitude(-0.2420229).build());
        COORDINATES_MAP.put("SE4", Coordinates.builder().latitude(51.4604104).longitude(-0.0448475).build());
        COORDINATES_MAP.put("SN14", Coordinates.builder().latitude(51.5023133).longitude(-2.3295928).build());
        COORDINATES_MAP.put("UB8", Coordinates.builder().latitude(51.5375109).longitude(-0.4844852).build());
    }

    public Coordinates find(final String location) {
        return COORDINATES_MAP.get(location.toUpperCase());
    }
}
