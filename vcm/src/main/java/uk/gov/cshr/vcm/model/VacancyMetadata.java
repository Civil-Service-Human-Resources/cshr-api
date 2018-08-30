package uk.gov.cshr.vcm.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class gives access to the id and last modified date of a vacancy.  This could be used for such things as
 * building links for search engines.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyMetadata {
    private Long identifier;
    private String atsVendorId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    private Date lastModified;
}
