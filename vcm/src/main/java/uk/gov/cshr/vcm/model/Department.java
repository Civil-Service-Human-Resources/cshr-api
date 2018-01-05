package uk.gov.cshr.vcm.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "departments")
@SequenceGenerator(name = "departments_id_seq", sequenceName = "departments_id_seq", allocationSize = 1)
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO,  generator="departments_id_seq")
    private Long id;

    private @NonNull
    String name;

    @Column(name = "disabilitylogo")
    private String disabilityLogo;
}
