package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.hebrew.HebrewMonth;

/**
 * A named holiday. The name is the holiday's identity across layers; a layer
 * overrides or removes a holiday by name. Most holidays have one
 * {@link HolidayPeriod}. A holiday whose rule, type or observance changed
 * over time, that was suspended for some years or whose date is set year by
 * year has several, and their years must not overlap: at most one period is
 * in force in any year.
 */
public record Holiday(String name, List<HolidayPeriod> periods) {

    public Holiday {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(periods, "periods must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        periods = List.copyOf(periods);

        if (periods.isEmpty()) {
            throw new IllegalArgumentException("holiday '" + name + "' needs at least one period");
        }

        requireDisjoint(name, periods);
    }

    public static Holiday of(String name, HolidayRule rule) {
        Objects.requireNonNull(rule, "rule must not be null");

        return new Holiday(name, List.of(HolidayPeriod.of(rule)));
    }

    public static Holiday of(String name, HolidayPeriod... periods) {
        return new Holiday(name, List.of(periods));
    }

    public static Holiday fixed(String name, Month month, int dayOfMonth) {
        return of(name, HolidayRule.fixed(month, dayOfMonth));
    }

    public static Holiday fixed(String name, MonthDay monthDay) {
        return of(name, HolidayRule.fixed(monthDay));
    }

    /** See {@link HolidayRule#on(LocalDate)}. */
    public static Holiday on(String name, LocalDate date) {
        return of(name, HolidayRule.on(date));
    }

    /** A holiday whose date is set year by year: one period per date, each in a year of its own. */
    public static Holiday onDates(String name, LocalDate... dates) {
        var periods = new ArrayList<HolidayPeriod>();

        for (var date : dates) {
            periods.add(HolidayPeriod.of(HolidayRule.on(date)));
        }

        return new Holiday(name, periods);
    }

    public static Holiday easterRelative(String name, int offsetDays) {
        return of(name, HolidayRule.easterRelative(offsetDays));
    }

    /** See {@link HolidayRule.Custom} for what the function may return. */
    public static Holiday custom(String name, Function<Year, Optional<LocalDate>> function) {
        return of(name, HolidayRule.custom(function));
    }

    /** See {@link HolidayRule#hijri(int, int)}. */
    public static Holiday hijri(String name, int month, int dayOfMonth) {
        return of(name, HolidayRule.hijri(month, dayOfMonth));
    }

    /** See {@link HolidayRule#hebrew(HebrewMonth, int)}. */
    public static Holiday hebrew(String name, HebrewMonth month, int dayOfMonth) {
        return of(name, HolidayRule.hebrew(month, dayOfMonth));
    }

    /** This holiday with one more period, which must not overlap the ones it has. */
    public Holiday and(HolidayPeriod period) {
        Objects.requireNonNull(period, "period must not be null");
        var extended = new ArrayList<>(periods);
        extended.add(period);

        return new Holiday(name, extended);
    }

    /** Applies to every period. */
    public Holiday ofType(HolidayType type) {
        return withEach(period -> period.ofType(type));
    }

    /** Makes every period a {@link HolidayType#HALF_DAY} on which business closes at the given time. */
    public Holiday closingAt(LocalTime closesAt) {
        return withEach(period -> period.closingAt(closesAt));
    }

    /** Applies to every period. */
    public Holiday withObservance(ObservanceRule observance) {
        return withEach(period -> period.withObservance(observance));
    }

    /** Only for a holiday with one period; one with several sets the years on each {@link HolidayPeriod}. */
    public Holiday validFrom(Year from) {
        return new Holiday(name, List.of(single().validFrom(from)));
    }

    /** Only for a holiday with one period; see {@link #validFrom}. */
    public Holiday validUntil(Year until) {
        return new Holiday(name, List.of(single().validUntil(until)));
    }

    /** Only for a holiday with one period; see {@link #validFrom}. */
    public Holiday validBetween(Year from, Year until) {
        return new Holiday(name, List.of(single().validBetween(from, until)));
    }

    /** The period in force in the year, if any. */
    public Optional<HolidayPeriod> periodIn(Year year) {
        Objects.requireNonNull(year, "year must not be null");

        for (var period : periods) {
            if (period.years().contains(year)) {
                return Optional.of(period);
            }
        }

        return Optional.empty();
    }

    /** The dates before any observance rule; empty when no period covers the year or its rule does not occur. */
    public List<LocalDate> datesIn(Year year, EasterAlgorithm easter) {
        var period = periodIn(year);

        if (period.isEmpty()) {
            return List.of();
        }

        try {
            return period.get().datesIn(year, easter);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("holiday '" + name + "': " + e.getMessage(), e);
        }
    }

    /** The first date in the year; see {@link HolidayRule#dateIn}. */
    public Optional<LocalDate> dateIn(Year year, EasterAlgorithm easter) {
        var dates = datesIn(year, easter);

        return dates.isEmpty() ? Optional.empty() : Optional.of(dates.get(0));
    }

    private HolidayPeriod single() {
        if (periods.size() != 1) {
            throw new IllegalStateException("holiday '" + name + "' has " + periods.size()
                    + " periods; set the years on each period instead");
        }

        return periods.get(0);
    }

    private Holiday withEach(UnaryOperator<HolidayPeriod> change) {
        return new Holiday(name, periods.stream()
                .map(change)
                .toList());
    }

    private static void requireDisjoint(String name, List<HolidayPeriod> periods) {
        for (var later = 1; later < periods.size(); later++) {
            var laterYears = periods.get(later).years();

            for (var earlier = 0; earlier < later; earlier++) {
                var earlierYears = periods.get(earlier).years();

                if (!laterYears.from().isAfter(earlierYears.until()) && !earlierYears.from().isAfter(laterYears.until())) {
                    throw new IllegalArgumentException("holiday '" + name + "' has overlapping periods: "
                            + describe(earlierYears) + " and " + describe(laterYears));
                }
            }
        }
    }

    private static String describe(YearRange years) {
        var from = years.from().getValue();
        var until = years.until().getValue();

        if (from == until) {
            return Integer.toString(from);
        }

        if (from == Year.MIN_VALUE && until == Year.MAX_VALUE) {
            return "every year";
        }

        if (from == Year.MIN_VALUE) {
            return "until " + until;
        }

        return until == Year.MAX_VALUE ? "from " + from : from + " to " + until;
    }
}
