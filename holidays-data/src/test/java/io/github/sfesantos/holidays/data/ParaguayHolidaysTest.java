package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Paraguay's national holidays under Ley 7544/2025, as the Biblioteca y
 * Archivo Central del Congreso prints it. Article 3 lets the executive move
 * 1 March, 12 June, 20 June and 29 September to a Monday by decree each year,
 * and article 4 lets it add up to three holidays a year. 2025 and 2026 follow
 * the decrees as the press reported them (Decreto 3047 and Decreto 5040 for
 * 2025, Decreto 6215 and Decreto 6280 for 2026). No decree for 2027 is out
 * yet, so that year is derived from the law.
 *
 * <p>12 June, 20 June and 29 September are not in the pack, because no rule
 * gives the dates the decrees chose. The lists below are the national
 * holidays without those three.
 */
class ParaguayHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 3), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1), d(2025, 5, 14),
                d(2025, 5, 15), d(2025, 8, 15), d(2025, 9, 5), d(2025, 12, 8), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("PY", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 3, 2), d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1), d(2026, 5, 14),
                d(2026, 5, 15), d(2026, 6, 30), d(2026, 8, 15), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("PY", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 5, 1), d(2027, 5, 14),
                d(2027, 5, 15), d(2027, 8, 15), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("PY", 2027));
    }

    @Test
    void heroesDayIsObservedOnTheMondayTheDecreesChose() {
        var paraguay = Observed.calendar("PY");

        assertFalse(paraguay.isHoliday(d(2025, 3, 1)));
        assertFalse(paraguay.isBusinessDay(d(2025, 3, 3)));
        assertFalse(paraguay.isHoliday(d(2026, 3, 1)));
        assertFalse(paraguay.isBusinessDay(d(2026, 3, 2)));
    }

    @Test
    void decreedHolidaysHappenOnce() {
        var paraguay = Observed.calendar("PY");

        assertFalse(paraguay.isBusinessDay(d(2025, 9, 5)));
        assertFalse(paraguay.isBusinessDay(d(2025, 12, 26)));
        assertFalse(paraguay.isBusinessDay(d(2026, 6, 30)));
        assertTrue(paraguay.isBusinessDay(d(2027, 6, 30)));
        assertFalse(paraguay.isHoliday(d(2026, 12, 26)));
    }
}
