package uk.gov.cshr.vcm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
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
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Latitude;
import org.hibernate.search.annotations.Longitude;
import org.hibernate.search.annotations.Spatial;

@Indexed
@Spatial
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
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vacancylocations_id_seq")
    private Long id;

    @Longitude
    private Double longitude;

    @Latitude
    private Double latitude;

    @Field
    @NonNull
    private String location;

//    @JsonBackReference
    @JsonIgnore
    @IndexedEmbedded
    @ManyToOne
    @JoinColumn(name = "vacancyid")
    private Vacancy vacancy;

    @Override
    public String toString() {
        return "VacancyLocation: " + id;
    }

}
