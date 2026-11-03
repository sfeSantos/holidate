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
 * How a holiday's date is derived for a given year. A rule is pure data and
 * yields at most one date per year; it may yield none (a 29 February in a
 * non-leap year, a fifth Monday in a month that has four).
 */
public sealed interface HolidayRule {

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

    static FixedDate fixed(MonthDay monthDay) {
        return new FixedDate(monthDay);
    }

    static FixedDate fixed(Month month, int dayOfMonth) {
        return new FixedDate(MonthDay.of(month, dayOfMonth));
    }

    static EasterRelative easterRelative(int offsetDays) {
        return new EasterRelative(offsetDays);
    }

    static NthWeekdayOfMonth nthWeekdayOfMonth(int n, DayOfWeek dayOfWeek, Month month) {
        return new NthWeekdayOfMonth(n, dayOfWeek, month);
    }

    static WeekdayAfter weekdayAfter(DayOfWeek dayOfWeek, MonthDay base) {
        return new WeekdayAfter(dayOfWeek, base);
    }

    static WeekdayBefore weekdayBefore(DayOfWeek dayOfWeek, MonthDay base) {
        return new WeekdayBefore(dayOfWeek, base);
    }

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

    record FixedDate(MonthDay monthDay) implements HolidayRule {

        public FixedDate {
            Objects.requireNonNull(monthDay, "monthDay must not be null");
        }
    }

    /** Negative offsets fall before Easter Sunday. */
    record EasterRelative(int offsetDays) implements HolidayRule {

        private static final int MAX_OFFSET = 366;

        public EasterRelative {
            if (Math.abs(offsetDays) > MAX_OFFSET) {
                throw new IllegalArgumentException("offsetDays must be within +/-" + MAX_OFFSET + ": " + offsetDays);
            }
        }
    }

    /** {@code n} is 1 to 5 from the start of the month, or -1 to -5 from the end ({@code -1} is "last"). */
    record NthWeekdayOfMonth(int n, DayOfWeek dayOfWeek, Month month) implements HolidayRule {

        public NthWeekdayOfMonth {
            Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
            Objects.requireNonNull(month, "month must not be null");

            if (n == 0 || Math.abs(n) > 5) {
                throw new IllegalArgumentException("n must be in 1..5 or -5..-1: " + n);
            }
        }
    }

    /** Strictly after the base day. A 29 February base resolves as 28 February in non-leap years. */
    record WeekdayAfter(DayOfWeek dayOfWeek, MonthDay base) implements HolidayRule {

        public WeekdayAfter {
            Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
            Objects.requireNonNull(base, "base must not be null");
        }
    }

    /** Strictly before the base day. A 29 February base resolves as 28 February in non-leap years. */
    record WeekdayBefore(DayOfWeek dayOfWeek, MonthDay base) implements HolidayRule {

        public WeekdayBefore {
            Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
            Objects.requireNonNull(base, "base must not be null");
        }
    }

    record Custom(Function<Year, Optional<LocalDate>> function) implements HolidayRule {

        public Custom {
            Objects.requireNonNull(function, "function must not be null");
        }
    }
}
