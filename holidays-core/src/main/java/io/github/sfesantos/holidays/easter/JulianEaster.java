package io.github.sfesantos.holidays.easter;

import java.time.LocalDate;

/**
 * Easter in the Julian calendar, by the algorithm Meeus gives for it. The
 * month and the day of the result are those of the Julian calendar, carried
 * in a {@link LocalDate}: 2026 gives 03-30, the day the Julian calendar calls
 * 30 March. {@link OrthodoxEaster} gives the day that is on the civil
 * calendar.
 */
public final class JulianEaster implements EasterAlgorithm {

    static final JulianEaster INSTANCE = new JulianEaster();

    private JulianEaster() {}

    public static LocalDate of(int year) {
        var leapCycle = Math.floorMod(year, 4);
        var weekCycle = Math.floorMod(year, 7);
        var metonicYear = Math.floorMod(year, 19);
        var paschalMoon = (19 * metonicYear + 15) % 30;
        var daysToSunday = (2 * leapCycle + 4 * weekCycle - paschalMoon + 34) % 7;
        var monthAndDay = paschalMoon + daysToSunday + 114;

        return LocalDate.of(year, monthAndDay / 31, monthAndDay % 31 + 1);
    }

    @Override
    public LocalDate easterSunday(int year) {
        return of(year);
    }

    @Override
    public String toString() {
        return "JulianEaster";
    }
}
