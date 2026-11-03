package io.github.sfesantos.holidays;

/**
 * Classifies a holiday so callers can decide which kinds count as non-working days.
 */
public enum HolidayType {

    /** Statutory public holiday. Counts as a non-working day. */
    PUBLIC,

    /** Bank holiday. Counts as a non-working day. */
    BANK,

    /** Commemorative date with no day off. Does not count as a non-working day by default. */
    OBSERVANCE,

    /** Half working day. Does not count as a non-working day by default. */
    HALF_DAY
}
