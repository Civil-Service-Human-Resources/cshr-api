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
@Table(name = "vacancies")
@SequenceGenerator(name = "vacancies_sequence", sequenceName = "vacancies_sequence", allocationSize = 1)
public class Vacancy  implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacancies_sequence")
    private Long id;

    private @NonNull String title;

    private @NonNull String description;

    private @NonNull String location;

    private @NonNull String grade;

    private @NonNull String closingDate;

    private int salaryMin;

    private int salaryMax;

    private int numberVacancies;

}
