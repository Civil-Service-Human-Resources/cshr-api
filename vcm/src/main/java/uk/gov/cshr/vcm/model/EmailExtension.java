package uk.gov.cshr.vcm.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "acceptedemailextensions")
@SequenceGenerator(name = "acceptedemailextensions_id_seq", sequenceName = "acceptedemailextensions_id_seq", allocationSize = 1)
@ApiModel(value = "EmailExtension", description = "Email extensions associated with a department")
public class EmailExtension implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "acceptedemailextensions_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "emailextension")
    private String emailExtension;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "department_id")
    @NotNull
    private Department department;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
        return hash;
    }


    @Override
    public String toString() {
        return "EmailExtension{" + "id=" + id + ", emailExtension=" + emailExtension + ", department=" + department.getId() + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EmailExtension other = (EmailExtension) obj;
        if (!Objects.equals(this.emailExtension, other.emailExtension)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
