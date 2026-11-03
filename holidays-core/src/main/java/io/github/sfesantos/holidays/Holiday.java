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
 *
 * <p>The name is the holiday's identity across layers: a layer that overrides
 * or removes a holiday refers to it by name. Names are free text in whatever
 * language the caller prefers.
 *
 * <p>Instances are immutable; the {@code with}-style methods return copies.
 *
 * @param name     identity of the holiday, not blank
 * @param type     what kind of holiday this is
 * @param rule     how the date is derived each year
 * @param validity the years in which the holiday is in force
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

    /** A {@link HolidayType#PUBLIC} holiday with no validity bounds. */
    public static Holiday of(String name, HolidayRule rule) {
        return new Holiday(name, HolidayType.PUBLIC, rule, YearRange.always());
    }

    /** Same day and month every year, for example 25 December. */
    public static Holiday fixed(String name, Month month, int dayOfMonth) {
        return of(name, HolidayRule.fixed(month, dayOfMonth));
    }

    /** Same day and month every year, for example 25 December. */
    public static Holiday fixed(String name, MonthDay monthDay) {
        return of(name, HolidayRule.fixed(monthDay));
    }

    /** A fixed number of days from Easter Sunday; negative offsets fall before it. */
    public static Holiday easterRelative(String name, int offsetDays) {
        return of(name, HolidayRule.easterRelative(offsetDays));
    }

    /** Any computation that the other rules cannot express. */
    public static Holiday custom(String name, Function<Year, Optional<LocalDate>> function) {
        return of(name, HolidayRule.custom(function));
    }

    /** A copy of this holiday with another type. */
    public Holiday ofType(HolidayType type) {
        return new Holiday(name, type, rule, validity);
    }

    /** A copy of this holiday in force from the given year on. */
    public Holiday validFrom(Year from) {
        return new Holiday(name, type, rule, YearRange.between(from, validity.until()));
    }

    /** A copy of this holiday in force up to the given year. */
    public Holiday validUntil(Year until) {
        return new Holiday(name, type, rule, YearRange.between(validity.from(), until));
    }

    /** A copy of this holiday in force only between the given years, both inclusive. */
    public Holiday validBetween(Year from, Year until) {
        return new Holiday(name, type, rule, YearRange.between(from, until));
    }

    /**
     * Resolves the date of this holiday in the given year, before any observance rule.
     *
     * @param year   the year to resolve
     * @param easter the Easter algorithm used by Easter-relative rules
     * @return the date, or empty when the holiday is out of its validity or does not occur
     */
    public Optional<LocalDate> dateIn(Year year, EasterAlgorithm easter) {
        Objects.requireNonNull(year, "year must not be null");

        if (!validity.contains(year)) {
            return Optional.empty();
        }

        return rule.dateIn(year, easter);
    }
}
