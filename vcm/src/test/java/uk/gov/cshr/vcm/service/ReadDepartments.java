
package uk.gov.cshr.vcm.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import javax.inject.Inject;
import liquibase.util.csv.CSVReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.vcm.VcmApplication;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.repository.DepartmentRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VcmApplication.class)
@ContextConfiguration
public class ReadDepartments {

	@Inject
	private DepartmentRepository departmentRepository;

    @Test
    public void readDepartments() throws FileNotFoundException, IOException {

//        this.mvc = MockMvcBuilders
//                .webAppContextSetup(webApplicationContext)
//                .apply(SecurityMockMvcConfigurers.springSecurity())
//                .build();


        Reader reader = Files.newBufferedReader(Paths.get("/Users/gordon/Desktop/RPGDepartmentDataMaster.csv"));
        CSVReader csvReader = new CSVReader(reader);

		// skip first line
		csvReader.readNext();

        String[] nextRecord;

        while ((nextRecord = csvReader.readNext()) != null) {
            System.out.println("id : " + nextRecord[0]);
            String name = nextRecord[1];
            System.out.println("Phone : " + nextRecord[2]);
            String emails = nextRecord[8];
            System.out.println("==========================");

			Department department = new Department();
			department.setName(name);

			HashSet<String> emailSet = new HashSet<>();
			emailSet.addAll(Arrays.asList(emails.split("\n")));

			department.setAcceptedEmailExtensions(emailSet);

			departmentRepository.save(department);
        }

        // "@fsni.x.gsi.gov.uk
        //    @justice-ni.gov.uk
        //@parolecommni.x.gsi.gov.
        //@dojni.x.gsi.gov.uk
        //@justice-ni.x.gsi.gov.uk
        //@parolecomni.gsi.gov.uk
        //@PAROLECOMNI.X.GSI.GOV.UK


//        String pattern = "(.*)(\\d+)(.*)";
//        String pattern = "([^$]*)@fsni\\.([^$]*)\\.gsi\\.gov\\.uk";
//                          ([^$]*)@fsni\.([^$]*)\.gsi\.gov\.uk

//        String ss = "@fsni.x.gsi.gov.uk";
//        String pattern = ss.replace(".", "\\.").replace("x", "([^$]*)");
//        pattern = "([^$]*)" + pattern;
//
//        System.out.println("pattern=" + pattern);
//
//        Pattern r = Pattern.compile(pattern);
//
//        String email = "gordon.mackay.xyz@fsni.anything.gsi.gov.uk";
//
//        Matcher m = r.matcher(email);
//
//        Assert.assertTrue(m.find());
    }
}
