package uk.gov.cshr.vcm.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Provides some common utilities for testing.
 */
public final class TestUtils {
    private TestUtils() {}

    /**
     * Gets a timestamp value with a given offset from now.
     *
     * @param numberOfDaysFromNow number of days from today required - can be negative
     * @return a timestamp with a given offset from now
     */
    public static Timestamp getTime(int numberOfDaysFromNow) {
        Date date = Date.from(LocalDateTime.now().plusDays(numberOfDaysFromNow).atZone(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date.getTime());
    }
}
