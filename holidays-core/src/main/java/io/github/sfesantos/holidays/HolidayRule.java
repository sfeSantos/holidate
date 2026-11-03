package io.github.sfesantos.holidays;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

/**
 * How a holiday's date is derived for a given year.
 *
 * <p>A rule is pure data; {@link #dateIn(Year, EasterAlgorithm)} resolves it.
 * A rule yields at most one date per year and may yield none (for example a
 * 29th of February in a non-leap year).
 */
public sealed interface HolidayRule {

    /**
     * Resolves this rule for the given year.
     *
     * @param year   the year to resolve
     * @param easter the Easter algorithm used by Easter-relative rules
     * @return the date in that year, or empty when the rule does not occur
     */
    default Optional<LocalDate> dateIn(Year year, EasterAlgorithm easter) {
        Objects.requireNonNull(year, "year must not be null");
        Objects.requireNonNull(easter, "easter must not be null");

        return switch (this) {
            case FixedDate r -> fixedDate(r, year);
            case EasterRelative r -> Optional.of(easter.easterSunday(year.getValue()).plusDays(r.offsetDays()));
            case NthWeekdayOfMonth r -> nthWeekdayOfMonth(r, year);
            case WeekdayAfter r -> Optional.of(r.base().atYear(year.getValue()).with(TemporalAdjusters.next(r.dayOfWeek())));
            case WeekdayBefore r -> Optional.of(r.base().atYear(year.getValue()).with(TemporalAdjusters.previous(r.dayOfWeek())));
            case Custom r -> custom(r, year);
        };
    }

    /** Same day and month every year, for example 25 December. */
    static FixedDate fixed(MonthDay monthDay) {
        return new FixedDate(monthDay);
    }

    /** Same day and month every year, for example 25 December. */
    static FixedDate fixed(Month month, int dayOfMonth) {
        return new FixedDate(MonthDay.of(month, dayOfMonth));
    }

    /** A fixed number of days from Easter Sunday; negative offsets fall before it. */
    static EasterRelative easterRelative(int offsetDays) {
        return new EasterRelative(offsetDays);
    }

    /**
     * The n-th given weekday of a month. {@code n} counts from the start of the
     * month when positive and from the end when negative, so {@code -1} is "last".
     */
    static NthWeekdayOfMonth nthWeekdayOfMonth(int n, DayOfWeek dayOfWeek, Month month) {
        return new NthWeekdayOfMonth(n, dayOfWeek, month);
    }

    /** The first given weekday strictly after the base day. */
    static WeekdayAfter weekdayAfter(DayOfWeek dayOfWeek, MonthDay base) {
        return new WeekdayAfter(dayOfWeek, base);
    }

    /** The first given weekday strictly before the base day. */
    static WeekdayBefore weekdayBefore(DayOfWeek dayOfWeek, MonthDay base) {
        return new WeekdayBefore(dayOfWeek, base);
    }

    /** Any computation that the other rules cannot express. */
    static Custom custom(Function<Year, Optional<LocalDate>> function) {
        return new Custom(function);
    }

    private static Optional<LocalDate> fixedDate(FixedDate rule, Year year) {
        var monthDay = rule.monthDay();

        if (!monthDay.isValidYear(year.getValue())) {
            return Optional.empty();
        }

        return Optional.of(monthDay.atYear(year.getValue()));
    }

    private static Optional<LocalDate> nthWeekdayOfMonth(NthWeekdayOfMonth rule, Year year) {
        var firstOfMonth = year.atMonth(rule.month()).atDay(1);
        var date = firstOfMonth.with(TemporalAdjusters.dayOfWeekInMonth(rule.n(), rule.dayOfWeek()));

        if (date.getMonth() != rule.month()) {
            return Optional.empty();
        }

        return Optional.of(date);
    }

    private static Optional<LocalDate> custom(Custom rule, Year year) {
        return Objects.requireNonNull(rule.function().apply(year), "custom rule returned null");
    }

    /**
     * Same day and month every year.
     *
     * @param monthDay the day of the year
     */
    record FixedDate(MonthDay monthDay) implements HolidayRule {

        public FixedDate {
            Objects.requireNonNull(monthDay, "monthDay must not be null");
        }
    }

    /**
     * A fixed number of days from Easter Sunday.
     *
     * @param offsetDays days after Easter Sunday; negative values fall before it
     */
    record EasterRelative(int offsetDays) implements HolidayRule {

        private static final int MAX_OFFSET = 366;

        public EasterRelative {
            if (Math.abs(offsetDays) > MAX_OFFSET) {
                throw new IllegalArgumentException("offsetDays must be within +/-" + MAX_OFFSET + ": " + offsetDays);
            }
        }
    }

    /**
     * The n-th given weekday of a month.
     *
     * @param n         1 to 5 from the start of the month, -1 to -5 from the end
     * @param dayOfWeek the weekday
     * @param month     the month
     */
    record NthWeekdayOfMonth(int n, DayOfWeek dayOfWeek, Month month) implements HolidayRule {

        public NthWeekdayOfMonth {
            Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
            Objects.requireNonNull(month, "month must not be null");

            if (n == 0 || Math.abs(n) > 5) {
                throw new IllegalArgumentException("n must be in 1..5 or -5..-1: " + n);
            }
        }
    }

    /**
     * The first given weekday strictly after a base day. A 29 February base
     * resolves as 28 February in non-leap years.
     *
     * @param dayOfWeek the weekday to find
     * @param base      the day to start from, exclusive
     */
    record WeekdayAfter(DayOfWeek dayOfWeek, MonthDay base) implements HolidayRule {

        public WeekdayAfter {
            Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
            Objects.requireNonNull(base, "base must not be null");
        }
    }

    /**
     * The first given weekday strictly before a base day. A 29 February base
     * resolves as 28 February in non-leap years.
     *
     * @param dayOfWeek the weekday to find
     * @param base      the day to start from, exclusive
     */
    record WeekdayBefore(DayOfWeek dayOfWeek, MonthDay base) implements HolidayRule {

        public WeekdayBefore {
            Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
            Objects.requireNonNull(base, "base must not be null");
        }
    }

    /**
     * Any computation that the other rules cannot express.
     *
     * @param function returns the date for a year, or empty when the holiday does not occur
     */
    record Custom(Function<Year, Optional<LocalDate>> function) implements HolidayRule {

        public Custom {
            Objects.requireNonNull(function, "function must not be null");
        }
    }
}
