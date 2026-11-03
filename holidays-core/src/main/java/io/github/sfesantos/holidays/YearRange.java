package io.github.sfesantos.holidays;

import java.time.Year;
import java.util.Objects;

/**
 * Closed range of years in which a holiday is in force.
 *
 * @param from  first year, inclusive
 * @param until last year, inclusive
 */
public record YearRange(Year from, Year until) {

    private static final YearRange ALWAYS = new YearRange(Year.of(Year.MIN_VALUE), Year.of(Year.MAX_VALUE));

    public YearRange {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(until, "until must not be null");

        if (from.isAfter(until)) {
            throw new IllegalArgumentException("from " + from + " is after until " + until);
        }
    }

    /** Every representable year. */
    public static YearRange always() {
        return ALWAYS;
    }

    /** From the given year on, with no upper bound. */
    public static YearRange from(Year from) {
        return new YearRange(from, ALWAYS.until);
    }

    /** Up to the given year, with no lower bound. */
    public static YearRange until(Year until) {
        return new YearRange(ALWAYS.from, until);
    }

    /** Both bounds inclusive. */
    public static YearRange between(Year from, Year until) {
        return new YearRange(from, until);
    }

    /** Whether the given year falls inside this range. */
    public boolean contains(int year) {
        return year >= from.getValue() && year <= until.getValue();
    }

    /** Whether the given year falls inside this range. */
    public boolean contains(Year year) {
        return contains(year.getValue());
    }
}
