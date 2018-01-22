package uk.gov.cshr.vcm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vacancies")
@SequenceGenerator(name = "vacancies_sequence", sequenceName = "vacancies_sequence", allocationSize = 1)
public class Vacancy implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vacancies_sequence")
    private long id;

    private @NonNull
    String title;

    private @NonNull
    String description;

    private @NonNull
    String location;

    private @NonNull
    String grade;

    private @NonNull
    String role;

    private @NonNull
    String responsibilities;

    private @NonNull
    String workingHours;

	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.m")
    private @NonNull
    Timestamp closingDate;

    private @NonNull
    String contactName;

    private @NonNull
    String contactDepartment;

    private @NonNull
    String contactEmail;

    private @NonNull
    String contactTelephone;

    private @NonNull
    String eligibility;

    private int salaryMin;

    private int salaryMax;

    private int numberVacancies;

    @OneToOne
    @JoinColumn(name = "dept_id")
    private Department department;
}
