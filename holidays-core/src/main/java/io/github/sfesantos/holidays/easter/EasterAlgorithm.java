package io.github.sfesantos.holidays.easter;

import java.time.LocalDate;

/**
 * Computes Easter Sunday for a year. Implementations must be pure and thread-safe.
 * A calendar has one Easter, chosen on its builder; the default is Gregorian.
 */
@FunctionalInterface
public interface EasterAlgorithm {

    LocalDate easterSunday(int year);

    static EasterAlgorithm gregorian() {
        return GregorianEaster.INSTANCE;
    }

    /** Easter as the Orthodox churches observe it, on the Gregorian date of the Julian Easter. */
    static EasterAlgorithm orthodox() {
        return OrthodoxEaster.INSTANCE;
    }

    /** Easter with the month and day of the Julian calendar; see {@link JulianEaster}. */
    static EasterAlgorithm julian() {
        return JulianEaster.INSTANCE;
    }
}
