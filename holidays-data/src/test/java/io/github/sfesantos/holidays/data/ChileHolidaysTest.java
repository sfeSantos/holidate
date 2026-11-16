package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Chile's legal holidays under Ley 2.977 and the laws that added to it: Ley
 * 20.148 (16 July, from 2007), Ley 20.299 (31 October, from 2008), Ley 20.983
 * (Friday 17 September when the 18th is a Saturday, Monday 2 January when the
 * 1st is a Sunday) and Ley 21.357 (the day of the winter solstice). Chile
 * publishes no yearly list, so the dates are derived from the laws as the
 * Biblioteca del Congreso Nacional prints them; the solstice falls on 20 June
 * in 2025 and on 21 June in 2026 and 2027.
 *
 * <p>Ley 19.668 moves San Pedro y San Pablo (29 June) and Encuentro de Dos
 * Mundos (12 October) to a Monday from Tuesday to Friday and leaves them
 * alone at the weekend, which no rule gives. The pack uses the Monday of a
 * seven-day window from 2026 and pins 2025, when both fall on a Sunday, as
 * dated one-offs. The lists below are the legal holidays without election
 * days.
 */
class ChileHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 19), d(2025, 5, 1), d(2025, 5, 21), d(2025, 6, 20),
                d(2025, 6, 29), d(2025, 7, 16), d(2025, 8, 15), d(2025, 9, 18), d(2025, 9, 19), d(2025, 10, 12),
                d(2025, 10, 31), d(2025, 11, 1), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("CL", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 4), d(2026, 5, 1), d(2026, 5, 21), d(2026, 6, 21),
                d(2026, 6, 29), d(2026, 7, 16), d(2026, 8, 15), d(2026, 9, 18), d(2026, 9, 19), d(2026, 10, 12),
                d(2026, 10, 31), d(2026, 11, 1), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("CL", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 27), d(2027, 5, 1), d(2027, 5, 21), d(2027, 6, 21),
                d(2027, 6, 28), d(2027, 7, 16), d(2027, 8, 15), d(2027, 9, 17), d(2027, 9, 18), d(2027, 9, 19),
                d(2027, 10, 11), d(2027, 10, 31), d(2027, 11, 1), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("CL", 2027));
    }

    @Test
    void sanPedroYSanPabloIsPinnedIn2025AndFollowsTheRuleFrom2026() {
        var chile = Observed.calendar("CL");

        assertTrue(chile.isHoliday(d(2025, 6, 29)));
        assertFalse(chile.isHoliday(d(2025, 6, 30)));
        assertFalse(chile.isBusinessDay(d(2026, 6, 29)));
        assertFalse(chile.isBusinessDay(d(2027, 6, 28)));
        assertFalse(chile.isHoliday(d(2027, 6, 29)));
        assertFalse(chile.isBusinessDay(d(2029, 7, 2)));
    }

    @Test
    void newYearOnASundayIsObservedOnMonday() {
        var chile = Observed.calendar("CL");

        assertTrue(chile.isHoliday(d(2023, 1, 2)));
        assertFalse(chile.isHoliday(d(2027, 1, 2)));
    }

    @Test
    void fridayBeforeFiestasPatriasIsAHolidayIn2027Only() {
        var chile = Observed.calendar("CL");

        assertFalse(chile.isBusinessDay(d(2027, 9, 17)));
        assertFalse(chile.isHoliday(d(2025, 9, 17)));
        assertFalse(chile.isHoliday(d(2026, 9, 17)));
    }

    @Test
    void holidaysAddedSince2000StartInTheirYear() {
        var chile = Observed.calendar("CL");

        assertFalse(chile.isHoliday(d(2006, 7, 16)));
        assertTrue(chile.isHoliday(d(2007, 7, 16)));
        assertFalse(chile.isHoliday(d(2007, 10, 31)));
        assertTrue(chile.isHoliday(d(2008, 10, 31)));
    }
}
