package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * Read-only questions about holidays. Dates are compared against observed
 * dates, so a Sunday holiday moved to Monday makes {@code isHoliday} true
 * on the Monday, not the Sunday.
 */
public interface HolidayQueries {

    boolean isHoliday(LocalDate date);
    Optional<HolidayOccurrence> holidayOn(LocalDate date);
    /** Holidays whose original date falls in the year, ordered by original date. */
    List<HolidayOccurrence> holidaysIn(Year year);
}
