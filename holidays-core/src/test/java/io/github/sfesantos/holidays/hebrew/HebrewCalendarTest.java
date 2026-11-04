package io.github.sfesantos.holidays.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HebrewCalendarTest {

    @ParameterizedTest
    @CsvSource({
        "5700, 1939-09-14", "5760, 1999-09-11", "5774, 2013-09-05", "5775, 2014-09-25",
        "5776, 2015-09-14", "5777, 2016-10-03", "5778, 2017-09-21", "5779, 2018-09-10",
        "5780, 2019-09-30", "5781, 2020-09-19", "5782, 2021-09-07", "5783, 2022-09-26",
        "5784, 2023-09-16", "5785, 2024-10-03", "5786, 2025-09-23", "5787, 2026-09-12"})
    void roshHashanahMatchesThePublishedDates(int year, LocalDate expected) {
        assertEquals(expected, HebrewCalendar.newYear(year));
        assertEquals(expected, HebrewCalendar.toLocalDate(year, 7, 1));
    }

    @Test
    void leapYearsFollowTheNineteenYearCycle() {
        assertTrue(HebrewCalendar.isLeapYear(5784));
        assertFalse(HebrewCalendar.isLeapYear(5786));
        assertTrue(HebrewCalendar.isLeapYear(5787));
        assertEquals(13, HebrewCalendar.monthsInYear(5787));
        assertEquals(12, HebrewCalendar.monthsInYear(5786));
    }

    @Test
    void yearLengthsAreTheSixAllowedOnes() {
        assertEquals(354, HebrewCalendar.daysInYear(5786));
        assertEquals(385, HebrewCalendar.daysInYear(5787));

        for (int year = 5600; year < 5900; year++) {
            int days = HebrewCalendar.daysInYear(year);
            boolean regular = days == 353 || days == 354 || days == 355;
            boolean leap = days == 383 || days == 384 || days == 385;

            assertTrue(HebrewCalendar.isLeapYear(year) ? leap : regular, "year " + year + " has " + days + " days");
        }
    }

    @Test
    void monthLengthsSumToTheYear() {
        for (int year = 5700; year < 5800; year++) {
            int sum = 0;

            for (int month = 1; month <= HebrewCalendar.monthsInYear(year); month++) {
                sum += HebrewCalendar.daysInMonth(year, month);
            }

            assertEquals(HebrewCalendar.daysInYear(year), sum, "year " + year);
        }
    }

    @Test
    void heshvanAndKislevVaryWithTheYearLength() {
        assertEquals(29, HebrewCalendar.daysInMonth(5786, 8));
        assertEquals(30, HebrewCalendar.daysInMonth(5787, 8));
        assertEquals(30, HebrewCalendar.daysInMonth(5786, 9));
        assertEquals(29, HebrewCalendar.daysInMonth(5786, 12));
        assertEquals(30, HebrewCalendar.daysInMonth(5787, 12));
        assertEquals(29, HebrewCalendar.daysInMonth(5787, 13));
    }

    @Test
    void festivalDates() {
        assertEquals(LocalDate.of(2026, 4, 2), HebrewCalendar.toLocalDate(5786, 1, 15));
        assertEquals(LocalDate.of(2026, 9, 21), HebrewCalendar.toLocalDate(5787, 7, 10));
        assertEquals(LocalDate.of(2026, 12, 5), HebrewCalendar.toLocalDate(5787, 9, 25));
        assertEquals(LocalDate.of(2024, 3, 24), HebrewCalendar.toLocalDate(5784, 13, 14));
    }

    @Test
    void yearOfADate() {
        assertEquals(5786, HebrewCalendar.yearOf(LocalDate.of(2026, 1, 1)));
        assertEquals(5786, HebrewCalendar.yearOf(LocalDate.of(2026, 9, 11)));
        assertEquals(5787, HebrewCalendar.yearOf(LocalDate.of(2026, 9, 12)));
    }

    @Test
    void adarResolvesByYear() {
        assertEquals(OptionalInt.of(12), HebrewMonth.ADAR.numberIn(5786));
        assertEquals(OptionalInt.of(13), HebrewMonth.ADAR.numberIn(5787));
        assertEquals(OptionalInt.empty(), HebrewMonth.ADAR_I.numberIn(5786));
        assertEquals(OptionalInt.of(12), HebrewMonth.ADAR_I.numberIn(5787));
        assertEquals(OptionalInt.of(7), HebrewMonth.TISHRI.numberIn(5787));
    }

    @Test
    void rejectsDaysAndMonthsThatDoNotExist() {
        assertThrows(IllegalArgumentException.class, () -> HebrewCalendar.toLocalDate(5786, 13, 1));
        assertThrows(IllegalArgumentException.class, () -> HebrewCalendar.toLocalDate(5786, 8, 30));
        assertThrows(IllegalArgumentException.class, () -> HebrewCalendar.toLocalDate(5786, 1, 0));
    }
}
