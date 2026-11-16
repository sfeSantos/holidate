package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Poland's days off under art. 1 of the Ustawa z dnia 18 stycznia 1951 r. o
 * dniach wolnych od pracy (Dz.U. 2020 poz. 1920), as amended by the act of 6
 * December 2024 (Dz.U. 2024 poz. 1965) that added 24 December from 2025.
 * There is no yearly list, so the dates are derived from the law. It names
 * Easter Sunday and Whit Sunday, and no holiday moves off a weekend.
 */
class PolandHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 3),
                d(2025, 6, 8), d(2025, 6, 19), d(2025, 8, 15), d(2025, 11, 1), d(2025, 11, 11), d(2025, 12, 24),
                d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("PL", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 3),
                d(2026, 5, 24), d(2026, 6, 4), d(2026, 8, 15), d(2026, 11, 1), d(2026, 11, 11), d(2026, 12, 24),
                d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("PL", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 3),
                d(2027, 5, 16), d(2027, 5, 27), d(2027, 8, 15), d(2027, 11, 1), d(2027, 11, 11), d(2027, 12, 24),
                d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("PL", 2027));
    }

    @Test
    void christmasEveIsADayOffFrom2025() {
        var poland = Observed.calendar("PL");

        assertFalse(poland.isHoliday(d(2024, 12, 24)));
        assertTrue(poland.isHoliday(d(2025, 12, 24)));
        assertFalse(poland.isBusinessDay(d(2025, 12, 24)));
    }

    @Test
    void epiphanyIsADayOffFrom2011() {
        var poland = Observed.calendar("PL");

        assertFalse(poland.isHoliday(d(2010, 1, 6)));
        assertTrue(poland.isHoliday(d(2011, 1, 6)));
    }
}
