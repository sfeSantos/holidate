package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Slovakia's state holidays and days of rest under § 1 and § 2 of Zákon č.
 * 241/1993 Z. z., read on Slov-Lex in the versions in force from 1 January
 * 2024 (530/2023 Z. z.) and 1 November 2025 (261/2025 Z. z.). There is no
 * yearly list, so the dates are derived from the law. State holidays that
 * are not days of rest are {@code OBSERVANCE}: 1 September from 2024, 28
 * October since it became a state holiday in 2021, and 17 November from
 * 2025. Under § 4b, 8 May and 15 September are not days of rest in 2026
 * only.
 */
class SlovakiaHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 8),
                d(2025, 7, 5), d(2025, 8, 29), d(2025, 9, 1), d(2025, 9, 15), d(2025, 10, 28), d(2025, 11, 1),
                d(2025, 11, 17), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("SK", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 8),
                d(2026, 7, 5), d(2026, 8, 29), d(2026, 9, 1), d(2026, 9, 15), d(2026, 10, 28), d(2026, 11, 1),
                d(2026, 11, 17), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("SK", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 8),
                d(2027, 7, 5), d(2027, 8, 29), d(2027, 9, 1), d(2027, 9, 15), d(2027, 10, 28), d(2027, 11, 1),
                d(2027, 11, 17), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("SK", 2027));
    }

    @Test
    void stateHolidaysWithoutADayOffStayBusinessDays() {
        var slovakia = Observed.calendar("SK");

        assertEquals(HolidayType.OBSERVANCE, slovakia.holidayOn(d(2025, 9, 1)).orElseThrow().type());
        assertTrue(slovakia.isBusinessDay(d(2025, 9, 1)));
        assertTrue(slovakia.isBusinessDay(d(2025, 10, 28)));
        assertTrue(slovakia.isBusinessDay(d(2025, 11, 17)));
        assertFalse(slovakia.isBusinessDay(d(2025, 8, 29)));
    }

    @Test
    void victoryDayAndOurLadyOfSorrowsAreWorkedIn2026Only() {
        var slovakia = Observed.calendar("SK");

        assertFalse(slovakia.isBusinessDay(d(2025, 5, 8)));
        assertFalse(slovakia.isBusinessDay(d(2025, 9, 15)));
        assertEquals(HolidayType.OBSERVANCE, slovakia.holidayOn(d(2026, 5, 8)).orElseThrow().type());
        assertTrue(slovakia.isBusinessDay(d(2026, 5, 8)));
        assertTrue(slovakia.isBusinessDay(d(2026, 9, 15)));
        assertFalse(slovakia.isBusinessDay(d(2028, 5, 8)));
        assertFalse(slovakia.isBusinessDay(d(2027, 9, 15)));
    }

    @Test
    void czechoslovakStateDayIsAStateHolidayFrom2021() {
        var slovakia = Observed.calendar("SK");

        assertFalse(slovakia.isHoliday(d(2020, 10, 28)));
        assertTrue(slovakia.isHoliday(d(2021, 10, 28)));
    }
}
