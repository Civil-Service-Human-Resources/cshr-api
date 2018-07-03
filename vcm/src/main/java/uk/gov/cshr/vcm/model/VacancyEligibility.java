package uk.gov.cshr.vcm.model;

import java.util.List;

public enum VacancyEligibility {

	PUBLIC,
	ACROSS_GOVERNMENT,
	INTERNAL;

    private List<Long> departments;

    private String emailAddress;

    public List<Long> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Long> departments) {
        this.departments = departments;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
}
