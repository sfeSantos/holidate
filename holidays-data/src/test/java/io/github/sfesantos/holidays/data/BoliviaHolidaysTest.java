package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Bolivia's national holidays under Decreto Supremo 2750 as amended by
 * Decreto Supremo 5019 and Decreto Supremo 5483: article 3 moves a holiday
 * that falls on a Sunday to the Monday after. 2026 is checked against the
 * calendar in article 5 of Decreto Supremo 5521 (Gaceta Oficial). 2025 and
 * 2027 are derived from the decrees, 2027 with no decree of its own yet. The
 * extra holidays are those of Decreto Supremo 5438 (7 August 2025), 5506 (26
 * December 2025), 5510 (2 January 2026) and 5521 (5 June and 7 August 2026).
 *
 * <p>22 January, Día de la Creación del Estado Plurinacional de Bolivia, is
 * not in the pack: Decreto Supremo 5521 moved it to Friday 23 January for
 * 2026 only, and no rule gives the three years. The lists below are the
 * national holidays without it.
 */
class BoliviaHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 3), d(2025, 3, 4), d(2025, 4, 18), d(2025, 5, 1), d(2025, 6, 19),
                d(2025, 6, 21), d(2025, 8, 6), d(2025, 8, 7), d(2025, 11, 3), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("BO", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 2), d(2026, 2, 16), d(2026, 2, 17), d(2026, 4, 3), d(2026, 5, 1),
                d(2026, 6, 4), d(2026, 6, 5), d(2026, 6, 22), d(2026, 8, 6), d(2026, 8, 7), d(2026, 11, 2),
                d(2026, 12, 25)),
                Observed.datesIn("BO", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 26), d(2027, 5, 1), d(2027, 5, 27),
                d(2027, 6, 21), d(2027, 8, 6), d(2027, 11, 2), d(2027, 12, 25)),
                Observed.datesIn("BO", 2027));
    }

    @Test
    void holidayOnASundayMovesToMonday() {
        var bolivia = Observed.calendar("BO");
        var andeanNewYear = bolivia.holidayOn(d(2026, 6, 22)).orElseThrow();

        assertEquals(d(2026, 6, 21), andeanNewYear.original());
        assertFalse(bolivia.isHoliday(d(2026, 6, 21)));
        assertEquals(d(2025, 11, 2), bolivia.holidayOn(d(2025, 11, 3)).orElseThrow().original());
    }

    @Test
    void holidayOnASaturdayStays() {
        var bolivia = Observed.calendar("BO");

        assertTrue(bolivia.isHoliday(d(2025, 6, 21)));
        assertTrue(bolivia.isBusinessDay(d(2025, 6, 23)));
    }

    @Test
    void decreedHolidaysHappenOnce() {
        var bolivia = Observed.calendar("BO");

        assertFalse(bolivia.isBusinessDay(d(2026, 1, 2)));
        assertFalse(bolivia.isBusinessDay(d(2026, 8, 7)));
        assertFalse(bolivia.isHoliday(d(2027, 8, 7)));
        assertFalse(bolivia.isHoliday(d(2026, 12, 26)));
    }
}
