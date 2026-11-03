package io.github.sfesantos.holidays;

import java.time.Year;
import java.util.Objects;

/**
 * Closed range of years in which a holiday is in force. Both bounds are inclusive.
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

    public static YearRange always() {
        return ALWAYS;
    }

    public static YearRange from(Year from) {
        return new YearRange(from, ALWAYS.until);
    }

    public static YearRange until(Year until) {
        return new YearRange(ALWAYS.from, until);
    }

    public static YearRange between(Year from, Year until) {
        return new YearRange(from, until);
    }

    public boolean contains(int year) {
        return year >= from.getValue() && year <= until.getValue();
    }

    public boolean contains(Year year) {
        return contains(year.getValue());
    }
}
