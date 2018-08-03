package uk.gov.cshr.vcm.model;

public enum DisabilityConfidenceLevel {

    EMPLOYER,
    LEADER,
    COMMITTED;

    public static DisabilityConfidenceLevel fromString(String value) {

        if ( value.trim().equalsIgnoreCase("employer") ) {
            return DisabilityConfidenceLevel.EMPLOYER;
        }
        else if ( value.trim().equalsIgnoreCase("leader") ) {
            return DisabilityConfidenceLevel.LEADER;
        }
        else if ( value.trim().equalsIgnoreCase("committed") ) {
            return DisabilityConfidenceLevel.COMMITTED;
        }
        else {
            return null;
        }
    }
}
