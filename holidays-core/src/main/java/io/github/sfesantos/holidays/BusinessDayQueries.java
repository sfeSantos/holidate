package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalQuery;

/**
 * Read-only questions about working days. A business day is neither a
 * weekend day nor a holiday of a non-working type.
 *
 * <p>The adjusters work on any temporal that holds a date, so
 * {@code dateTime.with(calendar.nextBusinessDayAdjuster())} keeps the time
 * of day and moves only the date.
 */
public interface BusinessDayQueries {

    Weekend weekend();
    boolean isWeekend(LocalDate date);
    boolean isBusinessDay(LocalDate date);
    /** The first business day strictly after the date. */
    LocalDate nextBusinessDay(LocalDate date);
    /** The date itself when it is a business day, otherwise the first business day after it. */
    LocalDate nextOrSameBusinessDay(LocalDate date);
    /** The last business day strictly before the date. */
    LocalDate previousBusinessDay(LocalDate date);
    /** The date itself when it is a business day, otherwise the last business day before it. */
    LocalDate previousOrSameBusinessDay(LocalDate date);
    /**
     * The date reached by moving {@code days} business days forward, or
     * backward when negative. Zero returns the date unchanged, even when it
     * is not a business day; one is {@link #nextBusinessDay(LocalDate)}.
     */
    LocalDate addBusinessDays(LocalDate date, int days);
    /**
     * Business days from {@code from} inclusive to {@code to} exclusive, the
     * same convention as {@code ChronoUnit.DAYS.between}. Negative when
     * {@code from} is after {@code to}.
     */
    long businessDaysBetween(LocalDate from, LocalDate to);
    TemporalAdjuster nextBusinessDayAdjuster();
    TemporalAdjuster nextOrSameBusinessDayAdjuster();
    TemporalAdjuster previousBusinessDayAdjuster();
    TemporalAdjuster previousOrSameBusinessDayAdjuster();
    TemporalAdjuster addBusinessDaysAdjuster(int days);
    /** A query answering {@link #isBusinessDay(LocalDate)} for any temporal that holds a date. */
    TemporalQuery<Boolean> isBusinessDayQuery();
}
