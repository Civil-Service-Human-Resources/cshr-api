package uk.gov.cshr.vcm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.EncodingType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;

//@AnalyzerDef(name = "customanalyzer",
//        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
//        filters = {
//            @TokenFilterDef(factory = LowerCaseFilterFactory.class)
//            ,
//    @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
//        @Parameter(name = "language", value = "English")
//    })
//        })
@Entity
@Indexed
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vacancies")
@SequenceGenerator(name = "vacancies_id_seq", sequenceName = "vacancies_id_seq", allocationSize = 1)
public class Vacancy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.AUTO, generator="vacancies_id_seq")
    private Long id;

    @NonNull
    private Long identifier;

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
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @NonNull
    private Date closingDate;

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

    @Column(name = "displaycsccontent")
    private Boolean displayCscContent;

    @Column(name = "selectionprocessdetails")
    private String selectionProcessDetails;

    @Column(name = "applyurl")
    @ApiModelProperty(notes = "URL linking to external system")
    private String applyURL;

    // @Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO, bridge=@FieldBridge(impl=DHLCSKeywordFieldBridge.class))
//    @Field(store = Store.YES)
//    @Field(bridge = @FieldBridge(impl = RegionFieldBridge.class), store = Store.YES, analyze = Analyze.NO)
//    @RegionFieldBridge
//    @Analyzer(definition = "customanalyzer")
    @Field(store = Store.YES)
    @Column(name = "regions")
    private String regions;

    @Column(name = "overseasjob")
    private Boolean overseasJob;

    @Column(name = "nationalitystatement")
    @Enumerated(EnumType.STRING)
    private NationalityStatement nationalityStatement;

//    @ContainedIn
//    @JsonManagedReference
//    @JsonIgnoreProperties("vacancy")
    @OneToMany(mappedBy = "vacancy", fetch = FetchType.EAGER, cascade = {
        CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
    @Builder.Default
//    @JsonIgnore
    private List<VacancyLocation> vacancyLocations = new ArrayList<>();

//    @Override
//    public String toString() {
//        return "Vacancy: " + id;
//    }
}
