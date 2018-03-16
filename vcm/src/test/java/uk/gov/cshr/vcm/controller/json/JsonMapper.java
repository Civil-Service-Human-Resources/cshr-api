package uk.gov.cshr.vcm.controller.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import uk.gov.cshr.vcm.controller.VacancyPage;
import static uk.gov.cshr.vcm.controller.VacancySearchTests.BRISTOL_LATITUDE;
import static uk.gov.cshr.vcm.controller.VacancySearchTests.BRISTOL_LONGITUDE;
import uk.gov.cshr.vcm.model.Department;
import uk.gov.cshr.vcm.model.Vacancy;
import uk.gov.cshr.vcm.model.VacancyLocation;

public class JsonMapper {

    private static final Timestamp YESTERDAY = getTime(-1);
    private static final Timestamp TODAY = getTime(0);
    private static final Timestamp TOMORROW = getTime(+1);
    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30);

    @Test
    public void testVacancy() throws JsonProcessingException, IOException {

        Vacancy vacancy = createVacancyPrototype();
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(vacancy);

        System.out.println(json);

        Vacancy deserialisedVacancy = objectMapper.readValue(json, Vacancy.class);
        System.out.println("deserialisedVacancy=" + deserialisedVacancy);
    }

    @Test
    public void testVacancies() throws JsonProcessingException, IOException {

        List<Vacancy> vacanciesList = Arrays.asList(
                createVacancyPrototype(),
                createVacancyPrototype(),
                createVacancyPrototype()
        );

        PageImpl<Vacancy> vacancies = new PageImpl<>(vacanciesList, null, 1);

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(vacancies);

        System.out.println(json);

        PageImpl<Vacancy> deserialisedVacancy = objectMapper.readValue(json, VacancyPage.class);
        System.out.println("deserialisedVacancy=" + deserialisedVacancy);
    }

    private Vacancy createVacancyPrototype() {

        Department department = Department.builder()
                .name("Department One")
                .disabilityLogo("disabilityLogo")
                .build();

        Vacancy vacancy = Vacancy.builder()
                .department(department)
                .title("testTile1 SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                .grade("testGrade1 SearchQueryGrade")
                .responsibilities("testResponsibilities1")
                .workingHours("testWorkingHours1")
                .closingDate(THIRTY_DAYS_FROM_NOW)
                .publicOpeningDate(YESTERDAY)
                .contactName("testContactName1")
                .contactDepartment("testContactDepartment1")
                .contactEmail("testContactEmail1")
                .contactTelephone("testContactTelephone1")
                .eligibility("testEligibility1")
                .salaryMin(0)
                .salaryMax(10)
                .numberVacancies(1)
                .identifier(System.currentTimeMillis())
                .build();

        vacancy.setVacancyLocations(new ArrayList<>());

        VacancyLocation vacancyLocation = VacancyLocation.builder()
                .latitude(BRISTOL_LATITUDE)
                .longitude(BRISTOL_LONGITUDE)
                .location("testLocation1 SearchQueryLocation")
                .vacancy(vacancy)
                .build();

        vacancy.getVacancyLocations().add(vacancyLocation);

        return vacancy;
    }

    private static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }
}
