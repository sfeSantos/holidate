package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Objects;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

/**
 * One stretch of a {@link Holiday}'s life: the rule, type and observance it
 * has in a range of years. {@code observance} defaults to
 * {@link ObservanceRule#inherit()}, meaning the layer or calendar rule
 * applies. {@code closesAt} only matters for a {@link HolidayType#HALF_DAY}:
 * it is when business closes that day, noon unless {@link #closingAt} says
 * otherwise.
 */
public record HolidayPeriod(HolidayType type, HolidayRule rule, YearRange validity, ObservanceRule observance,
        LocalTime closesAt) {

    public HolidayPeriod {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(rule, "rule must not be null");
        Objects.requireNonNull(validity, "validity must not be null");
        Objects.requireNonNull(observance, "observance must not be null");
        Objects.requireNonNull(closesAt, "closesAt must not be null");
    }

    public static HolidayPeriod of(HolidayRule rule) {
        return new HolidayPeriod(HolidayType.PUBLIC, rule, YearRange.always(), ObservanceRule.inherit(), LocalTime.NOON);
    }

    public HolidayPeriod ofType(HolidayType type) {
        return new HolidayPeriod(type, rule, validity, observance, closesAt);
    }

    /** Makes this a {@link HolidayType#HALF_DAY} on which business closes at the given time. */
    public HolidayPeriod closingAt(LocalTime closesAt) {
        return new HolidayPeriod(HolidayType.HALF_DAY, rule, validity, observance, closesAt);
    }

    public HolidayPeriod validFrom(Year from) {
        return withValidity(YearRange.between(from, validity.until()));
    }

    public HolidayPeriod validUntil(Year until) {
        return withValidity(YearRange.between(validity.from(), until));
    }

    public HolidayPeriod validBetween(Year from, Year until) {
        return withValidity(YearRange.between(from, until));
    }

    public HolidayPeriod withObservance(ObservanceRule observance) {
        return new HolidayPeriod(type, rule, validity, observance, closesAt);
    }

    public boolean inheritsObservance() {
        return observance == ObservanceRule.inherit();
    }

    /**
     * The years this period can yield a date in: its validity, narrowed to
     * the one year of the date when the rule is a single date.
     */
    public YearRange years() {
        var base = rule;

        while (base instanceof HolidayRule.Shifted shifted) {
            base = shifted.base();
        }

        if (base instanceof HolidayRule.OnDate onDate && validity.contains(onDate.date().getYear())) {
            var year = Year.of(onDate.date().getYear());

            return YearRange.between(year, year);
        }

        return validity;
    }

    /** The dates before any observance rule; empty outside the validity range or when the rule does not occur. */
    public List<LocalDate> datesIn(Year year, EasterAlgorithm easter) {
        Objects.requireNonNull(year, "year must not be null");

        return validity.contains(year) ? rule.datesIn(year, easter) : List.of();
    }

    private HolidayPeriod withValidity(YearRange validity) {
        return new HolidayPeriod(type, rule, validity, observance, closesAt);
    }
}
