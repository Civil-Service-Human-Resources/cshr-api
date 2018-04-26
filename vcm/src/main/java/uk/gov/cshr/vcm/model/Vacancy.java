package uk.gov.cshr.vcm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.EncodingType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

@AnalyzerDefs({
    @AnalyzerDef(
            name = "synonymn",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {
                @TokenFilterDef(factory = SynonymFilterFactory.class, params = {
                    @Parameter(name = "synonyms", value = "synonyms.txt"),
                    @Parameter(name = "ignoreCase", value = "true")
                }),
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = StopFilterFactory.class)
            })
})

@Entity
@Indexed
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vacancies")
@SequenceGenerator(name = "vacancies_id_seq", sequenceName = "vacancies_id_seq", allocationSize = 1)
@EqualsAndHashCode
public class Vacancy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vacancies_id_seq")
    @ApiModelProperty(notes = "Autogenerated on create.", readOnly = true)
    private Long id;

    @NonNull
    private Long identifier;

    @Fields({
        @Field(name = "title", store = Store.YES, analyzer = @Analyzer(definition = "synonymn")),
        @Field(name = "titleOriginal", store = Store.YES, analyze = Analyze.YES)
    })
    @Field(store = Store.YES)
    @NonNull
    private String title;

    @Field(store = Store.YES)
    @NonNull
    private String description;

    @Field
    @Column(name = "shortdescription")
    private String shortDescription;

    @NonNull
    private String grade;

    @Field
    @NonNull
    private String responsibilities;

    @NonNull
    private String workingHours;

    @Field(store = Store.YES, analyze = Analyze.NO)
    @DateBridge(resolution = Resolution.MINUTE, encoding = EncodingType.STRING)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @NonNull
    private Timestamp closingDate;

    @NonNull
    private String contactName;

    @NonNull
    private String contactDepartment;

    @NonNull
    private String contactEmail;

    @NonNull
    private String contactTelephone;

    @NonNull
    private String eligibility;

    @Field(store = Store.YES)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @Column(name = "government_opening_date")
    private Timestamp governmentOpeningDate;

    @Field(store = Store.YES)
    @DateBridge(resolution = Resolution.MILLISECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @Column(name = "internal_opening_date")
    private Timestamp internalOpeningDate;

    @Field(store = Store.YES, analyze = Analyze.NO)
    @DateBridge(resolution = Resolution.MINUTE, encoding = EncodingType.STRING)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @Column(name = "public_opening_date")
    private Date publicOpeningDate;

    @Field(store = Store.YES)
    @NonNull
    private Integer salaryMin;

    @Field(store = Store.YES)
    private Integer salaryMax;

    private Integer numberVacancies;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    @Column(name = "atsvendoridentifier")
    private String atsVendorIdentifier;

    @Transient
    @JsonIgnore
    @Field(store = Store.YES, name = "departmentID")
    public String getDepartmentID() {
        if (department != null) {
            return department.getId().toString();
        }
        else {
            return null;
        }
    }

    @Column(name = "displaycsccontent")
    private Boolean displayCscContent;

    @Column(name = "selectionprocessdetails")
    private String selectionProcessDetails;

    @Column(name = "applyurl")
    @ApiModelProperty(notes = "URL linking to external system")
    private String applyURL;

    @Field(store = Store.YES)
    @Column(name = "regions")
    private String regions;

    @Field(store = Store.YES, indexNullAs = "false")
    @Column(name = "overseasjob")
    private Boolean overseasJob;

    @Column(name = "nationalitystatement")
    @Enumerated(EnumType.STRING)
    private NationalityStatement nationalityStatement;

    @OneToMany(mappedBy = "vacancy", fetch = FetchType.EAGER, cascade = {
        CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
    private List<VacancyLocation> vacancyLocations = new ArrayList<>();

    @Column(name = "salaryoverridedescription")
    private String salaryOverrideDescription;

    @ApiModelProperty(notes = "A comma separated list of contract types",
                      example = "FULL_TIME, PART_TIME, CONTRACT, TEMPORARY, SEASONAL, INTERNSHIP")
    @Field(store = Store.YES)
    @Column(name = "contracttype")
    private String contractTypes;

    @ApiModelProperty(notes = "A comma separated list of working patterns",
                      example = "FLEXIBLE_WORKING, FULL_TIME, PART_TIME, JOB_SHARE, HOME_WORKING")
	@Field(store = Store.YES)
    @Column(name = "workingpattern")
    private String workingPatterns;

    @Column(name = "whatweoffer")
    private String whatWeOffer;

    @Column(name = "locationoverride")
    private String locationOverride;

    @Column(name = "personalspecification")
    private String personalSpecification;

	@Field(store = Store.YES, indexNullAs = "true")
	@Column(name = "active")
	@Builder.Default
    private Boolean active = Boolean.TRUE;
}

