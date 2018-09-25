package uk.gov.cshr.vcm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "departments_id_seq")
    private Long id;

    @Column(name = "identifier")
    private String identifier;

    @OrderBy
    private @NonNull
    String name;

    @Column(name = "disabilitylogo")
    private String disabilityLogo;

    @Column(name = "departmentstatus")
    @Enumerated(EnumType.STRING)
    private DepartmentStatus departmentStatus;

    @Column(name = "disabilityconfidencelevel")
    @Enumerated(EnumType.STRING)
    private DisabilityConfidenceLevel disabilityConfidenceLevel;

    @Column(name = "disabilityconfidencelevellastupdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    private Timestamp disabilityConfidenceLevelLastUpdate;

    @Column(name = "logoneeded")
    private Boolean logoNeeded;

    @Column(name = "logopath")
    private String logoPath;

    @Builder.Default
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE},
            mappedBy = "department", fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private Set<EmailExtension> acceptedEmailExtensions = new HashSet<>();

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
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
        final Department other = (Department) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @ManyToOne
    @JoinColumn(name = "parentdepartmentid")
    private Department parent;

    @Override
    public String toString() {
        return "Department{" + "id=" + id + ", name=" + name + '}';
    }

}