package io.github.sfesantos.holidays;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalQuery;
import java.util.Objects;
import java.util.Optional;

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
    /** The opening hours of an ordinary business day; {@link BusinessHours#allDay()} unless the builder set them. */
    BusinessHours businessHours();
    /**
     * The hours business is open on that date: empty on weekends and
     * non-working holidays, cut short on a {@link HolidayType#HALF_DAY}.
     * A half day whose closing time is not after the opening time is closed.
     */
    Optional<BusinessHours> businessHoursOn(LocalDate date);
    /** A business day on which a half-day holiday closes business early. */
    boolean isHalfDay(LocalDate date);
    boolean isOpen(LocalDateTime dateTime);
    /** Whether business is open at that instant, read in the given zone. */
    default boolean isOpen(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant must not be null");
        Objects.requireNonNull(zone, "zone must not be null");

        return isOpen(LocalDateTime.ofInstant(instant, zone));
    }
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
    /**
     * The date a payment or event scheduled for {@code date} actually falls
     * on under the convention: unchanged when it is a business day, otherwise
     * moved as the {@link BusinessDayConvention} says.
     */
    default LocalDate adjust(LocalDate date, BusinessDayConvention convention) {
        return BusinessDayConvention.apply(convention, date, this);
    }

    /**
     * The business day an instruction received at that instant settles on:
     * the {@link CutOff} turns the instant into a booking date in the market's
     * zone, the convention moves it off holidays and weekends.
     */
    default LocalDate adjust(Instant instant, CutOff cutOff, BusinessDayConvention convention) {
        Objects.requireNonNull(cutOff, "cutOff must not be null");

        return adjust(cutOff.dateOf(instant), convention);
    }

    /** As {@link #adjust(Instant, CutOff, BusinessDayConvention)} for a local time already in the cut-off's zone. */
    default LocalDate adjust(LocalDateTime dateTime, CutOff cutOff, BusinessDayConvention convention) {
        Objects.requireNonNull(cutOff, "cutOff must not be null");

        return adjust(cutOff.dateOf(dateTime), convention);
    }

    /** An adjuster applying {@link #adjust(LocalDate, BusinessDayConvention)} to any temporal that holds a date. */
    default TemporalAdjuster adjuster(BusinessDayConvention convention) {
        return temporal -> temporal.with(ChronoField.EPOCH_DAY, adjust(LocalDate.from(temporal), convention).toEpochDay());
    }

    default boolean isBusinessDayToday(Clock clock) {
        return isBusinessDay(LocalDate.now(clock));
    }

    default LocalDate nextBusinessDay(Clock clock) {
        return nextBusinessDay(LocalDate.now(clock));
    }

    default LocalDate addBusinessDays(Clock clock, int days) {
        return addBusinessDays(LocalDate.now(clock), days);
    }

    TemporalAdjuster nextBusinessDayAdjuster();
    TemporalAdjuster nextOrSameBusinessDayAdjuster();
    TemporalAdjuster previousBusinessDayAdjuster();
    TemporalAdjuster previousOrSameBusinessDayAdjuster();
    TemporalAdjuster addBusinessDaysAdjuster(int days);
    /** A query answering {@link #isBusinessDay(LocalDate)} for any temporal that holds a date. */
    TemporalQuery<Boolean> isBusinessDayQuery();
}
