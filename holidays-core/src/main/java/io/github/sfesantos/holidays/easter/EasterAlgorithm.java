package io.github.sfesantos.holidays.easter;

import java.time.LocalDate;

/**
 * Computes Easter Sunday for a year. Implementations must be pure and thread-safe.
 * Julian or Orthodox variants plug in here without touching the rule engine.
 */
@FunctionalInterface
public interface EasterAlgorithm {

    LocalDate easterSunday(int year);

    static EasterAlgorithm gregorian() {
        return GregorianEaster.INSTANCE;
    }
}
