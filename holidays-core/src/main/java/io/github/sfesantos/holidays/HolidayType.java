package io.github.sfesantos.holidays;

/**
 * Only {@link #PUBLIC} and {@link #BANK} count as non-working days by default.
 */
public enum HolidayType {
    PUBLIC,
    BANK,
    OBSERVANCE,
    HALF_DAY
}
