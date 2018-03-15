package uk.gov.cshr.vcm.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vacancylocations")
@SequenceGenerator(name = "vacancylocations_id_seq", sequenceName = "vacancylocations_id_seq", allocationSize = 1)
public class VacancyLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Double longitude;

    private Double latitude;

    @NonNull
    private String location;

    @ManyToOne
    @JoinColumn(name = "vacancyid")
    private Vacancy vacancy;

}
