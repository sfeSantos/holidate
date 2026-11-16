package io.github.sfesantos.holidays.easter;

import java.time.LocalDate;

/**
 * Easter as the Orthodox churches keep it: computed in the Julian calendar
 * and observed on the Gregorian date of that day. The two calendars are 13
 * days apart from 1900 to 2099 and one day more for each century year that
 * is not a leap year in the Gregorian calendar.
 */
public final class OrthodoxEaster implements EasterAlgorithm {

    static final OrthodoxEaster INSTANCE = new OrthodoxEaster();

    private OrthodoxEaster() {}

    public static LocalDate of(int year) {
        // Easter is never before March, so the leap day of a century year has already been skipped.
        var daysAhead = Math.floorDiv(year, 100) - Math.floorDiv(year, 400) - 2;

        return JulianEaster.of(year).plusDays(daysAhead);
    }

    @Override
    public LocalDate easterSunday(int year) {
        return of(year);
    }

    @Override
    public String toString() {
        return "OrthodoxEaster";
    }
}
