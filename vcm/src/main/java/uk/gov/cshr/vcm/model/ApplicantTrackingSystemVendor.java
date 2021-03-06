package uk.gov.cshr.vcm.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This entity represents an ApplicantTrackingSystem (ATS) that will be a source of vacancies that are loaded into the
 * CSHR job board.
 */
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "atsvendor")
@SequenceGenerator(name = "atsvendor_id_seq", sequenceName = "atsvendor_id_seq", allocationSize = 1)
public class ApplicantTrackingSystemVendor implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "atsvendor_id_seq")
    @ApiModelProperty(notes = "Autogenerated on create.", readOnly = true)
    private Long id;

    @Column(name = "clientidentifier")
    private String clientIdentifier;

    private String name;
}
