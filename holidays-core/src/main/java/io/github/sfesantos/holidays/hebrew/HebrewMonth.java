package io.github.sfesantos.holidays.hebrew;

import java.util.OptionalInt;

/**
 * Months of the Hebrew calendar, numbered from Nisan as in the traditional
 * count. {@link #ADAR} is the Adar that carries the festivals: the only one
 * in a common year, Adar II in a leap year. {@link #ADAR_I} exists only in
 * leap years.
 */
public enum HebrewMonth {

    NISAN(1),
    IYYAR(2),
    SIVAN(3),
    TAMMUZ(4),
    AV(5),
    ELUL(6),
    TISHRI(7),
    HESHVAN(8),
    KISLEV(9),
    TEVET(10),
    SHEVAT(11),
    ADAR_I(12),
    ADAR(12);

    private final int number;

    HebrewMonth(int number) {
        this.number = number;
    }

    /** This month's number in the given year, 1 to 13, or empty when the year does not have it. */
    public OptionalInt numberIn(int hebrewYear) {
        boolean leap = HebrewCalendar.isLeapYear(hebrewYear);

        if (this == ADAR_I) {
            return leap ? OptionalInt.of(12) : OptionalInt.empty();
        }

        if (this == ADAR) {
            return OptionalInt.of(leap ? 13 : 12);
        }

        return OptionalInt.of(number);
    }
}
