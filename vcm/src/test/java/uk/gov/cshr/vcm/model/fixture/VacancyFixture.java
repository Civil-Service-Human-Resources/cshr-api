package uk.gov.cshr.vcm.model.fixture;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

import uk.gov.cshr.vcm.model.Vacancy;

public class VacancyFixture {
    private static Vacancy PROTOTYPE;

    private static final Timestamp THIRTY_DAYS_FROM_NOW = getTime(30, 0);

    static {
        buildPrototype();
    }

    public static Vacancy getPrototype() {
        return PROTOTYPE;
    }

    private static void buildPrototype() {
        PROTOTYPE = Vacancy.builder()
                .identifier(12345L)
                .title("testTit1e SearchQueryTitle")
                .description("testDescription1 SearchQueryDescription")
                .location("testLocation1 SearchQueryLocation")
                .grade("testGrade1 SearchQueryGrade")
                .role("testRole1 SearchQueryRole")
                .responsibilities("testResponsibilities1")
                .workingHours("testWorkingHours1")
                .closingDate(THIRTY_DAYS_FROM_NOW)
                .contactName("testContactName1")
                .contactDepartment("testContactDepartment1")
                .contactEmail("testContactEmail1")
                .contactTelephone("testContactTelephone1")
                .eligibility("testEligibility1")
                .salaryMin(0)
                .numberVacancies(1)
                .build();
    }

    private static Timestamp getTime(int numberOfDaysFromNow, int hoursFromNow) {
        java.util.Date date = java.util.Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).plusHours(hoursFromNow).atZone(ZoneId.systemDefault()).toInstant());

        return new Timestamp(date.getTime());
    }
}
