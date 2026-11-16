package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Norway's public holidays under lov om helligdager og helligdagsfred
 * (LOV-1995-02-24-12) § 2 and lov om 1 og 17 mai som høgtidsdager
 * (LOV-1947-04-26-1). No yearly list is published, so the dates are derived
 * from the two acts. Nothing moves when a holiday falls on a weekend.
 */
class NorwayHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1),
                d(2025, 5, 17), d(2025, 5, 29), d(2025, 6, 8), d(2025, 6, 9), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("NO", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1),
                d(2026, 5, 14), d(2026, 5, 17), d(2026, 5, 24), d(2026, 5, 25), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("NO", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1),
                d(2027, 5, 6), d(2027, 5, 16), d(2027, 5, 17), d(2027, 5, 17), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("NO", 2027));
    }

    @Test
    void constitutionDayMeetsWhitMondayIn2027() {
        var norway = Observed.calendar("NO");

        assertTrue(norway.isHoliday(d(2027, 5, 17)));
        assertFalse(norway.isBusinessDay(d(2027, 5, 17)));
        assertTrue(norway.isBusinessDay(d(2027, 5, 18)));
    }
}
