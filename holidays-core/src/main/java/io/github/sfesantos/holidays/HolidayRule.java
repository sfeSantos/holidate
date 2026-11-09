package io.github.sfesantos.holidays;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.hebrew.HebrewCalendar;
import io.github.sfesantos.holidays.hebrew.HebrewMonth;

/**
 * How a holiday's dates are derived for a given year. A rule is pure data.
 * Rules on the Gregorian calendar yield at most one date per year and may
 * yield none (a 29 February in a non-leap year, a fifth Monday in a month
 * that has four). Rules on a lunar calendar, whose year is shorter, may
 * yield two dates in one Gregorian year.
 */
public sealed interface HolidayRule {

    /** The dates of this holiday in the year, in order; empty when it does not occur. */
    default List<LocalDate> datesIn(Year year, EasterAlgorithm easter) {
        Objects.requireNonNull(year, "year must not be null");
        Objects.requireNonNull(easter, "easter must not be null");

        return switch (this) {
            case Hijri r -> hijri(r, year);
            case Hebrew r -> hebrew(r, year);
            default -> single(year, easter).map(List::of).orElse(List.of());
        };
    }

    /** The first date in the year, or empty. Lunar rules may have a second one; see {@link #datesIn}. */
    default Optional<LocalDate> dateIn(Year year, EasterAlgorithm easter) {
        var dates = datesIn(year, easter);

        return dates.isEmpty() ? Optional.empty() : Optional.of(dates.get(0));
    }

    private Optional<LocalDate> single(Year year, EasterAlgorithm easter) {
        return switch (this) {
            case FixedDate r -> fixedDate(r, year);
            case OnDate r -> r.date().getYear() == year.getValue() ? Optional.of(r.date()) : Optional.empty();
            case EasterRelative r -> Optional.of(easter.easterSunday(year.getValue()).plusDays(r.offsetDays()));
            case NthWeekdayOfMonth r -> nthWeekdayOfMonth(r, year);
            case WeekdayAfter r -> Optional.of(r.base().atYear(year.getValue()).with(TemporalAdjusters.next(r.dayOfWeek())));
            case WeekdayBefore r -> Optional.of(r.base().atYear(year.getValue()).with(TemporalAdjusters.previous(r.dayOfWeek())));
            case Custom r -> custom(r, year);
            case Hijri r -> throw new IllegalStateException("lunar rules resolve through datesIn");
            case Hebrew r -> throw new IllegalStateException("lunar rules resolve through datesIn");
        };
    }

    static FixedDate fixed(MonthDay monthDay) {
        return new FixedDate(monthDay);
    }

    static FixedDate fixed(Month month, int dayOfMonth) {
        return new FixedDate(MonthDay.of(month, dayOfMonth));
    }

    /** A date that happens once; see {@link OnDate}. */
    static OnDate on(LocalDate date) {
        return new OnDate(date);
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

    /**
     * A day of the Islamic calendar, {@code month} 1 (Muharram) to 12 (Dhu
     * al-Hijjah), resolved with the Umm al-Qura tables of
     * {@link HijrahChronology}. Local moon sightings may differ by a day.
     */
    static Hijri hijri(int month, int dayOfMonth) {
        return new Hijri(month, dayOfMonth);
    }

    /** A day of the Hebrew calendar; see {@link HebrewMonth} for how Adar is handled in leap years. */
    static Hebrew hebrew(HebrewMonth month, int dayOfMonth) {
        return new Hebrew(month, dayOfMonth);
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

    private static List<LocalDate> hebrew(Hebrew rule, Year year) {
        var dates = new ArrayList<LocalDate>(2);
        int first = year.getValue() + 3760;

        for (int hebrewYear = first; hebrewYear <= first + 1; hebrewYear++) {
            var month = rule.month().numberIn(hebrewYear);

            if (month.isPresent() && rule.dayOfMonth() <= HebrewCalendar.daysInMonth(hebrewYear, month.getAsInt())) {
                var date = HebrewCalendar.toLocalDate(hebrewYear, month.getAsInt(), rule.dayOfMonth());

                if (date.getYear() == year.getValue()) {
                    dates.add(date);
                }
            }
        }

        return List.copyOf(dates);
    }

    private static List<LocalDate> hijri(Hijri rule, Year year) {
        if (!Hijri.TABLE.contains(year)) {
            return List.of();
        }

        var dates = new ArrayList<LocalDate>(2);
        int first = HijrahDate.from(year.atDay(1)).get(ChronoField.YEAR);
        int last = HijrahDate.from(year.atDay(year.length())).get(ChronoField.YEAR);

        for (int hijrahYear = first; hijrahYear <= last; hijrahYear++) {
            var firstOfMonth = HijrahDate.of(hijrahYear, rule.month(), 1);

            if (rule.dayOfMonth() <= firstOfMonth.lengthOfMonth()) {
                var date = LocalDate.from(firstOfMonth.plus(rule.dayOfMonth() - 1, ChronoUnit.DAYS));

                if (date.getYear() == year.getValue()) {
                    dates.add(date);
                }
            }
        }

        return List.copyOf(dates);
    }

    record FixedDate(MonthDay monthDay) implements HolidayRule {

        public FixedDate {
            Objects.requireNonNull(monthDay, "monthDay must not be null");
        }
    }

    /** One date in one year: a royal wedding, a coronation, a one-off closure. Empty in every other year. */
    record OnDate(LocalDate date) implements HolidayRule {

        public OnDate {
            Objects.requireNonNull(date, "date must not be null");
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

    /** A 30th day only occurs in the years where that month has thirty days. */
    record Hijri(int month, int dayOfMonth) implements HolidayRule {

        /** The Gregorian years fully covered by the JDK's Umm al-Qura tables. */
        static final YearRange TABLE = tableYears();

        public Hijri {
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("month must be in 1..12: " + month);
            }

            if (dayOfMonth < 1 || dayOfMonth > 30) {
                throw new IllegalArgumentException("dayOfMonth must be in 1..30: " + dayOfMonth);
            }
        }

        private static YearRange tableYears() {
            var years = HijrahChronology.INSTANCE.range(ChronoField.YEAR);
            var min = LocalDate.from(HijrahDate.of((int) years.getMinimum(), 1, 1));
            var max = LocalDate.from(HijrahDate.of((int) years.getMaximum(), 12, 1));

            return YearRange.between(Year.of(min.getYear() + 1), Year.of(max.getYear() - 1));
        }
    }

    /** A 30th day only occurs in the years where that month has thirty days; {@code ADAR_I} only in leap years. */
    record Hebrew(HebrewMonth month, int dayOfMonth) implements HolidayRule {

        public Hebrew {
            Objects.requireNonNull(month, "month must not be null");

            if (dayOfMonth < 1 || dayOfMonth > 30) {
                throw new IllegalArgumentException("dayOfMonth must be in 1..30: " + dayOfMonth);
            }
        }
    }
}
