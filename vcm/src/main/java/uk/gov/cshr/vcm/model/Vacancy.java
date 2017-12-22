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
public class Vacancy implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacancies_sequence")
    private long id;

    private @NonNull String title;

    private @NonNull String description;

    private @NonNull String location;

    private @NonNull String grade;

    private @NonNull String role;

    private @NonNull String responsibilities;

    private @NonNull String workingHours;

    private @NonNull String closingDate;

    private @NonNull String contactName;

    private @NonNull String contactDepartment;

    private @NonNull String contactEmail;

    private @NonNull String contactTelephone;

    private @NonNull String eligibility;

    private int salaryMin;

    private int salaryMax;

    private int numberVacancies;

}
