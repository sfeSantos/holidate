package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Honduras's paid holidays under Código del Trabajo art. 339, with the dates
 * derived from the law. Decreto 78-2015 moves the holidays of 3, 12 and 21
 * October to the Wednesday, Thursday and Friday that start on the first
 * Wednesday of October, the "Semana Morazánica": 1 to 3 October 2025 and 7
 * to 9 October 2026, as the Secretaría de Gobernación confirmed each year.
 */
class HondurasHolidaysTest {

    @Test
    void labourCode2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 14), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 19), d(2025, 5, 1),
                d(2025, 9, 15), d(2025, 10, 1), d(2025, 10, 2), d(2025, 10, 3), d(2025, 12, 25)),
                Observed.datesIn("HN", 2025));
    }

    @Test
    void labourCode2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 14), d(2026, 5, 1),
                d(2026, 9, 15), d(2026, 10, 7), d(2026, 10, 8), d(2026, 10, 9), d(2026, 12, 25)),
                Observed.datesIn("HN", 2026));
    }

    @Test
    void labourCode2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 3, 27), d(2027, 4, 14), d(2027, 5, 1),
                d(2027, 9, 15), d(2027, 10, 6), d(2027, 10, 7), d(2027, 10, 8), d(2027, 12, 25)),
                Observed.datesIn("HN", 2027));
    }

    @Test
    void octoberHolidaysMoveToTheSemanaMorazanica() {
        var honduras = Observed.calendar("HN");

        assertFalse(honduras.isHoliday(d(2026, 10, 6)));
        assertTrue(honduras.isHoliday(d(2026, 10, 7)));
        assertTrue(honduras.isHoliday(d(2026, 10, 9)));
        assertFalse(honduras.isHoliday(d(2026, 10, 12)));
        assertFalse(honduras.isHoliday(d(2026, 10, 21)));
    }
}
