package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.TemporalQuery;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Read-only questions about holidays. Dates are compared against observed
 * dates, so a Sunday holiday moved to Monday makes {@code isHoliday} true
 * on the Monday, not the Sunday.
 *
 * <p>Navigation methods ({@code nextHoliday}, {@code previousHoliday} and
 * their variants) look at most one hundred years away from the given date
 * and return empty beyond that, so a calendar whose rules fire nothing in
 * that window never triggers an unbounded search.
 */
public interface HolidayQueries {

    boolean isHoliday(LocalDate date);
    Optional<HolidayOccurrence> holidayOn(LocalDate date);
    /** Holidays whose original date falls in the year, ordered by original date. */
    List<HolidayOccurrence> holidaysIn(Year year);
    /** Holidays observed from {@code from} inclusive to {@code to} exclusive, ordered by observed date. */
    List<HolidayOccurrence> holidaysBetween(LocalDate from, LocalDate to);
    /** The first holiday observed strictly after the date. */
    Optional<HolidayOccurrence> nextHoliday(LocalDate date);
    /** The first holiday observed on or after the date. */
    Optional<HolidayOccurrence> nextHolidayInclusive(LocalDate date);
    /** The last holiday observed strictly before the date. */
    Optional<HolidayOccurrence> previousHoliday(LocalDate date);
    /** The last holiday observed on or before the date. */
    Optional<HolidayOccurrence> previousHolidayInclusive(LocalDate date);
    /** Days from the date to the next holiday observed strictly after it; empty when there is none. */
    OptionalLong daysUntilNextHoliday(LocalDate date);
    /** A query answering {@link #isHoliday(LocalDate)} for any temporal that holds a date. */
    TemporalQuery<Boolean> isHolidayQuery();
    /** A query answering {@link #holidayOn(LocalDate)} for any temporal that holds a date. */
    TemporalQuery<Optional<HolidayOccurrence>> holidayOnQuery();
}
