package uk.gov.cshr.vcm.model;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "emailextensions")
@SequenceGenerator(name = "emailextensions_id_seq", sequenceName = "emailextensions_id_seq", allocationSize = 1)
@ApiModel(value = "EmailExtension", description = "Email extensions associated with a department")
public class EmailExtension implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO,  generator="emailextensions_id_seq")
    private Long id;

    @Column(name = "emailextension")
    private String emailExtension;

    @ManyToOne
    private Department department;

}
