package uk.gov.cshr.vcm.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import liquibase.util.csv.CSVReader;
import org.springframework.stereotype.Service;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.EmailExtension;
import uk.gov.cshr.vcm.repository.DepartmentRepository;

@Service
public class LoadDepartmentEmailsService {

	@Inject
	private DepartmentRepository departmentRepository;

    public void readEmails(InputStream inputStream) throws IOException {

        HashMap<Department, String> parentsMap = new HashMap<>();
        HashMap<String, Department> departmentsMap = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        CSVReader csvReader = new CSVReader(bufferedReader);

		// skip first line
		csvReader.readNext();

        String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {
            String name = nextRecord[1];
            String emails = nextRecord[8];
            String parent = nextRecord[12];

			Department department = departmentRepository.findByIdentifier(name);

            if ( department != null ) {

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
        }

        for (Map.Entry<Department, String> entry : parentsMap.entrySet()) {

            Department department = entry.getKey();

            String parent = entry.getValue();

            System.out.println("parent=" + parent);

            Department parentDepartment = departmentsMap.get(parent);
            department.setParent(parentDepartment);
            departmentRepository.save(department);
        }
    }
}
