package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

/**
 * A named holiday: a {@link HolidayRule} plus the metadata a calendar needs.
 * The name is the holiday's identity across layers; a layer overrides or
 * removes a holiday by name.
 */
public record Holiday(String name, HolidayType type, HolidayRule rule, YearRange validity) {

    public Holiday {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(rule, "rule must not be null");
        Objects.requireNonNull(validity, "validity must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }

    public static Holiday of(String name, HolidayRule rule) {
        return new Holiday(name, HolidayType.PUBLIC, rule, YearRange.always());
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

    public Holiday ofType(HolidayType type) {
        return new Holiday(name, type, rule, validity);
    }

    public Holiday validFrom(Year from) {
        return new Holiday(name, type, rule, YearRange.between(from, validity.until()));
    }

    public Holiday validUntil(Year until) {
        return new Holiday(name, type, rule, YearRange.between(validity.from(), until));
    }

    public Holiday validBetween(Year from, Year until) {
        return new Holiday(name, type, rule, YearRange.between(from, until));
    }

    /** The date before any observance rule; empty outside the validity range or when the rule does not occur. */
    public Optional<LocalDate> dateIn(Year year, EasterAlgorithm easter) {
        Objects.requireNonNull(year, "year must not be null");

        if (!validity.contains(year)) {
            return Optional.empty();
        }

        return rule.dateIn(year, easter);
    }
}
