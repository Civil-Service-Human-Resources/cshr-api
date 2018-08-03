package uk.gov.cshr.vcm.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.inject.Inject;
import liquibase.util.csv.CSVReader;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.DepartmentStatus;
import uk.gov.cshr.vcm.model.DisabilityConfidenceLevel;
import uk.gov.cshr.vcm.model.EmailExtension;
import uk.gov.cshr.vcm.repository.DepartmentRepository;

@Service
public class DepartmentService {

    public final static SimpleDateFormat d_MMM_yyyy = new SimpleDateFormat("d MMM yyyy");
    public final static SimpleDateFormat d_MMM_yyyy_dashed = new SimpleDateFormat("d-MMM-yyyy");
    public final static SimpleDateFormat d_MM_yy_slashed = new SimpleDateFormat("d/MM/yy");

	@Inject
	private DepartmentRepository departmentRepository;

    public void readDepartments(InputStream inputStream) throws IOException, ParseException {

        HashMap<Department, String> parentsMap = new HashMap<>();
        HashMap<String, Department> departmentsMap = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        CSVReader csvReader = new CSVReader(bufferedReader);

		// skip first line
		csvReader.readNext();

        String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {

            String identifier = nextRecord[1];
            String emails = nextRecord[8];
            String parent = nextRecord[12];
            String name = nextRecord[2];
            String departmentStatus = nextRecord[3];
            String disabilityConfidenceLevel = nextRecord[4];
            String disabilityConfidenceLevelLastUpdate = nextRecord[5];
            String logoNeeded = nextRecord[6];
            String logoPath = nextRecord[7];

			Department department = departmentRepository.findByIdentifier(identifier);

            if ( department == null ) {
                department = new Department();
                department.setId(System.currentTimeMillis());
                department.setIdentifier(identifier);
                department.setAcceptedEmailExtensions(new HashSet<>());
            }

            department.setName(name);
            department.setDepartmentStatus(DepartmentStatus.fromString(departmentStatus));
            department.setDisabilityConfidenceLevel(DisabilityConfidenceLevel.fromString(disabilityConfidenceLevel));
    
            department.setDisabilityConfidenceLevelLastUpdate(parseDate(disabilityConfidenceLevelLastUpdate));
            //department.setDisabilityLogo(null);
            department.setLogoNeeded(BooleanUtils.toBoolean(logoNeeded));
            department.setLogoPath(logoPath);

            department.getAcceptedEmailExtensions().clear();

            for (String string : Arrays.asList(emails.split("\n"))) {
                EmailExtension emailExtension = EmailExtension.builder()
                        .emailExtension(string)
                        .department(department)
                        .build();
                department.getAcceptedEmailExtensions().add(emailExtension);
            }

            departmentsMap.put(department.getName(), department);

            if ( parent != null && ! parent.trim().isEmpty() ) {
                parentsMap.put(department, parent);
            }

            departmentRepository.save(department);
        }

        for (Map.Entry<Department, String> entry : parentsMap.entrySet()) {

            Department department = entry.getKey();
            String parent = entry.getValue();

            Department parentDepartment = departmentsMap.get(parent);
            department.setParent(parentDepartment);
            departmentRepository.save(department);
        }
    }

    private Timestamp parseDate(String dateString) throws ParseException {

        if ( dateString == null || dateString.trim().isEmpty() ) {
            return null;
        }

        try {
            Date date = d_MMM_yyyy.parse(dateString.trim());
            return new Timestamp(date.getTime());
        }
        catch(ParseException e) {            
        }

        try {
            Date date = d_MMM_yyyy_dashed.parse(dateString.trim());
            return new Timestamp(date.getTime());
        }
        catch(ParseException e) {
        }

        try {
            Date date = d_MM_yy_slashed.parse(dateString.trim());
            return new Timestamp(date.getTime());
        }
        catch(ParseException e) {
        }

        throw new ParseException(dateString, 0);
    }
}
