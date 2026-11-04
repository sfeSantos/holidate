package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

/**
 * A named holiday: a {@link HolidayRule} plus the metadata a calendar needs.
 * The name is the holiday's identity across layers; a layer overrides or
 * removes a holiday by name. {@code observance} defaults to
 * {@link ObservanceRule#inherit()}, meaning the layer or calendar rule applies.
 */
public record Holiday(String name, HolidayType type, HolidayRule rule, YearRange validity, ObservanceRule observance) {

    public Holiday {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(rule, "rule must not be null");
        Objects.requireNonNull(validity, "validity must not be null");
        Objects.requireNonNull(observance, "observance must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    public static Holiday of(String name, HolidayRule rule) {
        return new Holiday(name, HolidayType.PUBLIC, rule, YearRange.always(), ObservanceRule.inherit());
    }

    public static Holiday fixed(String name, Month month, int dayOfMonth) {
        return of(name, HolidayRule.fixed(month, dayOfMonth));
    }

    public static Holiday fixed(String name, MonthDay monthDay) {
        return of(name, HolidayRule.fixed(monthDay));
    }

    public static Holiday easterRelative(String name, int offsetDays) {
        return of(name, HolidayRule.easterRelative(offsetDays));
    }

    public static Holiday custom(String name, Function<Year, Optional<LocalDate>> function) {
        return of(name, HolidayRule.custom(function));
    }

    /** See {@link HolidayRule#hijri(int, int)}. */
    public static Holiday hijri(String name, int month, int dayOfMonth) {
        return of(name, HolidayRule.hijri(month, dayOfMonth));
    }

    public Holiday ofType(HolidayType type) {
        return new Holiday(name, type, rule, validity, observance);
    }

    public Holiday validFrom(Year from) {
        return withValidity(YearRange.between(from, validity.until()));
    }

    public Holiday validUntil(Year until) {
        return withValidity(YearRange.between(validity.from(), until));
    }

    public Holiday validBetween(Year from, Year until) {
        return withValidity(YearRange.between(from, until));
    }

    public Holiday withObservance(ObservanceRule observance) {
        return new Holiday(name, type, rule, validity, observance);
    }

    public boolean inheritsObservance() {
        return observance == ObservanceRule.inherit();
    }

    /** The dates before any observance rule; empty outside the validity range or when the rule does not occur. */
    public List<LocalDate> datesIn(Year year, EasterAlgorithm easter) {
        Objects.requireNonNull(year, "year must not be null");

        if (!validity.contains(year)) {
            return List.of();
        }

        return rule.datesIn(year, easter);
    }

    /** The first date in the year; see {@link HolidayRule#dateIn}. */
    public Optional<LocalDate> dateIn(Year year, EasterAlgorithm easter) {
        var dates = datesIn(year, easter);

        return dates.isEmpty() ? Optional.empty() : Optional.of(dates.get(0));
    }

    private Holiday withValidity(YearRange validity) {
        return new Holiday(name, type, rule, validity, observance);
    }
}
