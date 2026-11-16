package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Panama's days of national celebration and mourning under Código de
 * Trabajo art. 46, including 20 December (Ley 291 of 2022), with the dates
 * derived from the law. Under art. 47 a holiday on a Sunday gives the Monday
 * off: 21 December 2026 and 29 November 2027. Bridge days decreed for public
 * offices and the day a president takes office are not in the pack.
 */
class PanamaHolidaysTest {

    @Test
    void labourCode2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 9), d(2025, 3, 4), d(2025, 4, 18), d(2025, 5, 1), d(2025, 11, 3),
                d(2025, 11, 5), d(2025, 11, 10), d(2025, 11, 28), d(2025, 12, 8), d(2025, 12, 20), d(2025, 12, 25)),
                Observed.datesIn("PA", 2025));
    }

    @Test
    void labourCode2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 9), d(2026, 2, 17), d(2026, 4, 3), d(2026, 5, 1), d(2026, 11, 3),
                d(2026, 11, 5), d(2026, 11, 10), d(2026, 11, 28), d(2026, 12, 8), d(2026, 12, 21), d(2026, 12, 25)),
                Observed.datesIn("PA", 2026));
    }

    @Test
    void labourCode2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 9), d(2027, 2, 9), d(2027, 3, 26), d(2027, 5, 1), d(2027, 11, 3),
                d(2027, 11, 5), d(2027, 11, 10), d(2027, 11, 29), d(2027, 12, 8), d(2027, 12, 20), d(2027, 12, 25)),
                Observed.datesIn("PA", 2027));
    }

    @Test
    void sundayHolidayGivesTheMondayOff() {
        var panama = Observed.calendar("PA");

        assertFalse(panama.isBusinessDay(d(2026, 12, 21)));
        assertFalse(panama.isBusinessDay(d(2027, 11, 29)));
        assertTrue(panama.isHoliday(d(2026, 11, 28)));
        assertTrue(panama.isBusinessDay(d(2026, 11, 30)));
    }

    @Test
    void nationalMourningDayStartsIn2022() {
        var panama = Observed.calendar("PA");

        assertFalse(panama.isHoliday(d(2021, 12, 20)));
        assertTrue(panama.isHoliday(d(2022, 12, 20)));
    }
}
