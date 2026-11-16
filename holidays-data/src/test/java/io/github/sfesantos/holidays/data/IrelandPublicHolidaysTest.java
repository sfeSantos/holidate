package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Ireland's public holidays under the Second Schedule to the Organisation of
 * Working Time Act 1997, with the February holiday (St. Brigid's Day) that
 * S.I. No. 50 of 2022 prescribed from 2023. The 2026 dates are the ones the
 * Workplace Relations Commission lists; 2025 and 2027 are derived from the
 * Schedule. The law does not move a public holiday that falls on a weekend:
 * the day stays where it is and the employee is owed another benefit, so
 * Saturdays and Sundays stay in these lists.
 */
class IrelandPublicHolidaysTest {

    @Test
    void schedule2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 3), d(2025, 3, 17), d(2025, 4, 21), d(2025, 5, 5), d(2025, 6, 2),
                d(2025, 8, 4), d(2025, 10, 27), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("IE", 2025));
    }

    @Test
    void schedule2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 2), d(2026, 3, 17), d(2026, 4, 6), d(2026, 5, 4), d(2026, 6, 1),
                d(2026, 8, 3), d(2026, 10, 26), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("IE", 2026));
    }

    @Test
    void schedule2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 1), d(2027, 3, 17), d(2027, 3, 29), d(2027, 5, 3), d(2027, 6, 7),
                d(2027, 8, 2), d(2027, 10, 25), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("IE", 2027));
    }

    @Test
    void aWeekendHolidayStaysOnItsDate() {
        var ireland = Observed.calendar("IE");
        var stStephensDay = ireland.holidayOn(d(2026, 12, 26)).orElseThrow();

        assertEquals("St. Stephen's Day", stStephensDay.name());
        assertFalse(stStephensDay.isMoved());
        assertTrue(ireland.isBusinessDay(d(2026, 12, 28)));
        assertTrue(ireland.isBusinessDay(d(2027, 12, 27)));
    }

    @Test
    void stBrigidsDayStartsIn2023() {
        var ireland = Observed.calendar("IE");

        assertFalse(ireland.isHoliday(d(2022, 2, 7)));
        assertTrue(ireland.isHoliday(d(2023, 2, 6)));
    }
}
