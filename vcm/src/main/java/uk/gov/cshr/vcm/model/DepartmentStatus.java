package uk.gov.cshr.vcm.model;

public enum DepartmentStatus {

    ACTIVE,
    DEACTIVATED;

    public static DepartmentStatus fromString(String value) {

        if (value.trim().equalsIgnoreCase("deactivated")) {
            return DepartmentStatus.DEACTIVATED;
        } else {
            return DepartmentStatus.ACTIVE;
        }
    }
}
