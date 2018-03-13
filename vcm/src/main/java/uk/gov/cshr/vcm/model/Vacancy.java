package uk.gov.cshr.vcm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Latitude;
import org.hibernate.search.annotations.Longitude;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Spatial;
import org.hibernate.search.annotations.Store;
import uk.gov.cshr.vcm.service.RegionFieldBridge;

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
@Spatial
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

    @Field
    @NonNull
    private String title;

    @Field
    @NonNull
    private String description;

    @Field
    @Column(name = "shortdescription")
    private String shortDescription;

    @Field
    @NonNull
    private String location;

    @NonNull
    private String grade;

    @Field
    @NonNull
    private String responsibilities;

    @NonNull
    private String workingHours;

    @Field
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @Column(name = "government_opening_date")
    private Timestamp governmentOpeningDate;

    @DateBridge(resolution = Resolution.MILLISECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @Column(name = "internal_opening_date")
    private Timestamp internalOpeningDate;

    @DateBridge(resolution = Resolution.MILLISECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+0")
    @Column(name = "public_opening_date")
    private Timestamp publicOpeningDate;

    @Field
    @NonNull
    private Integer salaryMin;

    @Field
    private Integer salaryMax;

    private Integer numberVacancies;

    /**
     * If a vacancy has no longitude ensure it is null not 0 (zero) since 0 is a valid point in latitude
     */
    @Longitude
    private Double longitude;

    /**
     * If a vacancy has no latitude ensure it is null not 0 (zero) since 0 is a valid point in latitude
     */
    @Latitude
    private Double latitude;

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
    @Field(bridge = @FieldBridge(impl = RegionFieldBridge.class), store = Store.YES, analyze = Analyze.NO)
//    @RegionFieldBridge
//    @Analyzer(definition = "customanalyzer")
    @Column(name = "regions")
    private String regions;

    @Column(name = "nationalitystatement")
    @Enumerated(EnumType.STRING)
    private NationalityStatement nationalityStatement;
}
