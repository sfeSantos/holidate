package io.github.sfesantos.holidays.hebrew;

import java.time.LocalDate;

/**
 * Arithmetic of the fixed Hebrew calendar, after Reingold and Dershowitz,
 * "Calendrical Calculations". Months are numbered 1 (Nisan) to 12 or 13
 * (Adar II in a leap year); the year begins in month 7, Tishri.
 */
public final class HebrewCalendar {

    /** 1 Tishri AM 1 as a Rata Die day number. */
    private static final long EPOCH_RD = -1373427;
    /** Rata Die of 1970-01-01, which is epoch day 0. */
    private static final long RD_OF_EPOCH_DAY_ZERO = 719163;
    private static final int TISHRI = 7;

    private HebrewCalendar() {}

    public static boolean isLeapYear(int year) {
        return Math.floorMod(7L * year + 1, 19) < 7;
    }

    public static int monthsInYear(int year) {
        return isLeapYear(year) ? 13 : 12;
    }

    public static int daysInYear(int year) {
        return (int) (newYearRd(year + 1) - newYearRd(year));
    }

    public static int daysInMonth(int year, int month) {
        if (month == 2 || month == 4 || month == 6 || month == 10 || month == 13) {
            return 29;
        }

        if (month == 12 && !isLeapYear(year)) {
            return 29;
        }

        int length = daysInYear(year);

        if (month == 8 && length % 10 != 5) {
            return 29;
        }

        if (month == 9 && length % 10 == 3) {
            return 29;
        }

        return 30;
    }

    /** 1 Tishri of the year. */
    public static LocalDate newYear(int year) {
        return LocalDate.ofEpochDay(newYearRd(year) - RD_OF_EPOCH_DAY_ZERO);
    }

    public static LocalDate toLocalDate(int year, int month, int dayOfMonth) {
        if (month < 1 || month > monthsInYear(year)) {
            throw new IllegalArgumentException("year " + year + " has no month " + month);
        }

        if (dayOfMonth < 1 || dayOfMonth > daysInMonth(year, month)) {
            throw new IllegalArgumentException("month " + month + " of year " + year + " has no day " + dayOfMonth);
        }

        long rd = newYearRd(year) + dayOfMonth - 1;

        if (month < TISHRI) {
            for (int m = TISHRI; m <= monthsInYear(year); m++) {
                rd += daysInMonth(year, m);
            }

            for (int m = 1; m < month; m++) {
                rd += daysInMonth(year, m);
            }
        } else {
            for (int m = TISHRI; m < month; m++) {
                rd += daysInMonth(year, m);
            }
        }

        return LocalDate.ofEpochDay(rd - RD_OF_EPOCH_DAY_ZERO);
    }

    /** The Hebrew year a Gregorian date falls in. */
    public static int yearOf(LocalDate date) {
        int approximate = date.getYear() + 3760;

        return date.isBefore(newYear(approximate + 1)) ? approximate : approximate + 1;
    }

    private static long newYearRd(int year) {
        return EPOCH_RD + elapsedDays(year) + yearLengthCorrection(year);
    }

    /** Days from the epoch to the molad-based new year, before the postponements that depend on the neighbours. */
    private static long elapsedDays(int year) {
        long monthsElapsed = Math.floorDiv(235L * year - 234, 19);
        long partsElapsed = 12084 + 13753 * monthsElapsed;
        long days = 29 * monthsElapsed + Math.floorDiv(partsElapsed, 25920);

        return Math.floorMod(3 * (days + 1), 7) < 3 ? days + 1 : days;
    }

    private static int yearLengthCorrection(int year) {
        long before = elapsedDays(year - 1);
        long thisYear = elapsedDays(year);
        long after = elapsedDays(year + 1);

        if (after - thisYear == 356) {
            return 2;
        }

        return thisYear - before == 382 ? 1 : 0;
    }
}
