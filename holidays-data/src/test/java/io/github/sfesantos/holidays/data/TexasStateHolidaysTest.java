package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Texas: the federal list plus the state holidays of Texas Government Code
 * section 662.003(b). The dates come from the law and agree with the
 * Comptroller's state holiday schedules for fiscal 2026 and 2027.
 *
 * <p>On Confederate Heroes Day, Texas Independence Day, San Jacinto Day,
 * Emancipation Day in Texas and Lyndon Baines Johnson Day an agency keeps
 * enough staff to do business (section 662.004), so they are
 * {@code OBSERVANCE}. The Friday after Thanksgiving, 24 December and 26
 * December close the agencies. A state holiday on a Saturday or Sunday is
 * not moved (section 662.005).
 *
 * <p>A date shows twice when a state day meets a federal one: 19 June
 * (Emancipation Day in Texas and Juneteenth), 19 January 2026 (the Monday
 * of the Birthday of Martin Luther King, Jr.) and 24 December 2027
 * (Christmas Day observed).
 */
class TexasStateHolidaysTest {

    @Test
    void texas2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 19), d(2025, 1, 20), d(2025, 2, 17), d(2025, 3, 2), d(2025, 4, 21),
                d(2025, 5, 26), d(2025, 6, 19), d(2025, 6, 19), d(2025, 7, 4), d(2025, 8, 27), d(2025, 9, 1),
                d(2025, 10, 13), d(2025, 11, 11), d(2025, 11, 27), d(2025, 11, 28), d(2025, 12, 24),
                d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("US-TX", 2025));
    }

    @Test
    void texas2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 19), d(2026, 1, 19), d(2026, 2, 16), d(2026, 3, 2), d(2026, 4, 21),
                d(2026, 5, 25), d(2026, 6, 19), d(2026, 6, 19), d(2026, 7, 3), d(2026, 8, 27), d(2026, 9, 7),
                d(2026, 10, 12), d(2026, 11, 11), d(2026, 11, 26), d(2026, 11, 27), d(2026, 12, 24),
                d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("US-TX", 2026));
    }

    @Test
    void texas2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 18), d(2027, 1, 19), d(2027, 2, 15), d(2027, 3, 2), d(2027, 4, 21),
                d(2027, 5, 31), d(2027, 6, 18), d(2027, 6, 19), d(2027, 7, 5), d(2027, 8, 27), d(2027, 9, 6),
                d(2027, 10, 11), d(2027, 11, 11), d(2027, 11, 25), d(2027, 11, 26), d(2027, 12, 24),
                d(2027, 12, 24), d(2027, 12, 26)),
                Observed.datesIn("US-TX", 2027));
    }

    @Test
    void partialStaffingDaysStayBusinessDays() {
        var texas = Observed.calendar("US-TX");
        var sanJacinto = texas.holidayOn(d(2026, 4, 21)).orElseThrow();

        assertEquals("San Jacinto Day", sanJacinto.name());
        assertEquals(HolidayType.OBSERVANCE, sanJacinto.type());
        assertTrue(texas.isBusinessDay(d(2026, 4, 21)));
        assertTrue(texas.isBusinessDay(d(2026, 3, 2)));
        assertTrue(texas.isBusinessDay(d(2027, 1, 19)));
        assertTrue(texas.isBusinessDay(d(2026, 8, 27)));
    }

    @Test
    void theDaysAroundThanksgivingAndChristmasCloseTheAgencies() {
        var texas = Observed.calendar("US-TX");

        assertFalse(texas.isBusinessDay(d(2025, 11, 28)));
        assertFalse(texas.isBusinessDay(d(2025, 12, 24)));
        assertFalse(texas.isBusinessDay(d(2025, 12, 26)));
        assertTrue(Observed.calendar("US").isBusinessDay(d(2025, 12, 26)));
    }

    @Test
    void aStateHolidayOnAWeekendIsNotMoved() {
        var texas = Observed.calendar("US-TX");

        assertEquals("Texas Independence Day", texas.holidayOn(d(2025, 3, 2)).orElseThrow().name());
        assertFalse(texas.isHoliday(d(2025, 3, 3)));
        assertTrue(texas.isHoliday(d(2026, 12, 26)));
        assertTrue(texas.isBusinessDay(d(2026, 12, 28)));
    }

    @Test
    void juneteenthStaysADayOffNextToEmancipationDay() {
        var texas = Observed.calendar("US-TX");

        assertFalse(texas.isBusinessDay(d(2026, 6, 19)));
        assertTrue(texas.isHoliday(d(2020, 6, 19)));
        assertTrue(texas.isBusinessDay(d(2020, 6, 19)));
    }
}
