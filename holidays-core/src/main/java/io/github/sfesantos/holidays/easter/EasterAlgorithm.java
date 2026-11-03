package io.github.sfesantos.holidays.easter;

import java.time.LocalDate;

/**
 * Computes the date of Easter Sunday for a given year.
 *
 * <p>Implementations must be pure and thread-safe. The Gregorian (Western)
 * computation is the default; Julian and Orthodox variants can be plugged in
 * through this interface without touching the rule engine.
 */
@FunctionalInterface
public interface EasterAlgorithm {

    /**
     * Returns Easter Sunday of the given proleptic ISO year.
     *
     * @param year the year, as in {@link java.time.Year#getValue()}
     * @return the date of Easter Sunday in that year
     */
    LocalDate easterSunday(int year);

    /**
     * The Gregorian computus, valid for every year since 1583.
     *
     * @return the shared Gregorian implementation
     */
    static EasterAlgorithm gregorian() {
        return GregorianEaster.INSTANCE;
    }
}
