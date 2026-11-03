package io.github.sfesantos.holidays.easter;

import java.time.LocalDate;

/**
 * Gregorian Easter via the anonymous algorithm published in Nature (1876),
 * as presented by Meeus, Jones and Butcher. Pure integer arithmetic, no
 * tables, valid for every Gregorian year.
 */
public final class GregorianEaster implements EasterAlgorithm {

    static final GregorianEaster INSTANCE = new GregorianEaster();

    private GregorianEaster() {}

    public static LocalDate of(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;

        return LocalDate.of(year, month, day);
    }

    @Override
    public LocalDate easterSunday(int year) {
        return of(year);
    }

    @Override
    public String toString() {
        return "GregorianEaster";
    }
}
