package uk.gov.cshr.vcm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OrderBy;
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
	@JoinTable(name = "department_acceptedemailextensions")
	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "acceptedemailextensions")
	private Set<String> acceptedEmailExtensions = new HashSet<>();
}
