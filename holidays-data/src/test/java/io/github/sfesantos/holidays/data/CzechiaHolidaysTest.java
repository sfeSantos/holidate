package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Czechia's state holidays and other holidays under § 1 and § 2 of Zákon č.
 * 245/2000 Sb., read in the consolidated text on e-Sbírka. There is no
 * yearly list, so the dates are derived from the law. 1 January appears
 * twice because the law names it both as a state holiday and as Nový rok.
 * Good Friday was added by Zákon č. 359/2015 Sb., first kept in 2016.
 */
class CzechiaHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 8),
                d(2025, 7, 5), d(2025, 7, 6), d(2025, 9, 28), d(2025, 10, 28), d(2025, 11, 17), d(2025, 12, 24),
                d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("CZ", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 8),
                d(2026, 7, 5), d(2026, 7, 6), d(2026, 9, 28), d(2026, 10, 28), d(2026, 11, 17), d(2026, 12, 24),
                d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("CZ", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 8),
                d(2027, 7, 5), d(2027, 7, 6), d(2027, 9, 28), d(2027, 10, 28), d(2027, 11, 17), d(2027, 12, 24),
                d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("CZ", 2027));
    }

    @Test
    void goodFridayIsAHolidayFrom2016() {
        var czechia = Observed.calendar("CZ");

        assertFalse(czechia.isHoliday(d(2015, 4, 3)));
        assertTrue(czechia.isHoliday(d(2016, 3, 25)));
    }
}
