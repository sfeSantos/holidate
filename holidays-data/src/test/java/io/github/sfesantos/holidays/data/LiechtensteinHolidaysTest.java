package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Liechtenstein's statutory holidays under Art. 18 Abs. 2 of the
 * Arbeitsgesetz (LGBl. 1967 Nr. 6), as consolidated on gesetze.li. The law
 * names the days and no yearly list exists, so the dates are derived from
 * it. Days that are customary but not in the law (Maria Lichtmess, Josefstag,
 * Karfreitag, 24 and 31 December) are not included.
 */
class LiechtensteinHolidaysTest {

    @Test
    void arbeitsgesetz2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 29), d(2025, 6, 9),
                d(2025, 6, 19), d(2025, 8, 15), d(2025, 9, 8), d(2025, 11, 1), d(2025, 12, 8), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.datesIn("LI", 2025));
    }

    @Test
    void arbeitsgesetz2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 14), d(2026, 5, 25),
                d(2026, 6, 4), d(2026, 8, 15), d(2026, 9, 8), d(2026, 11, 1), d(2026, 12, 8), d(2026, 12, 25),
                d(2026, 12, 26)),
                Observed.datesIn("LI", 2026));
    }

    @Test
    void arbeitsgesetz2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 17),
                d(2027, 5, 27), d(2027, 8, 15), d(2027, 9, 8), d(2027, 11, 1), d(2027, 12, 8), d(2027, 12, 25),
                d(2027, 12, 26)),
                Observed.datesIn("LI", 2027));
    }

    @Test
    void customaryDaysStayBusinessDays() {
        var liechtenstein = Observed.calendar("LI");

        assertTrue(liechtenstein.isBusinessDay(d(2026, 2, 2)));
        assertTrue(liechtenstein.isBusinessDay(d(2026, 3, 19)));
        assertTrue(liechtenstein.isBusinessDay(d(2026, 4, 3)));
    }
}
