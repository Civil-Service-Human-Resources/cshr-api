package uk.gov.cshr.vcm.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vacancy  implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private @NonNull String title;

    private @NonNull String description;

    private @NonNull String location;

    private int salaryMin;

    private int salaryMax;

}
