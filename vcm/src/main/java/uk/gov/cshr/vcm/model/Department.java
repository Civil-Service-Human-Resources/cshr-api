package uk.gov.cshr.vcm.model;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.*;

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
@SequenceGenerator(name = "departments_sequence", sequenceName = "departments_sequence", allocationSize = 1)
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departments_sequence")
    private Long id;

    private @NonNull
    String name;
}
