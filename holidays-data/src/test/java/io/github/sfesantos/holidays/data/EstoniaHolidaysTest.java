package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Estonia's national holiday and public holidays under pühade ja
 * tähtpäevade seadus, § 1 and § 2. No yearly list is published, so the dates
 * are derived from the act. Nothing moves when a holiday falls on a weekend.
 * The working days the law shortens before some of them are not holidays
 * and are left out.
 */
class EstoniaHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 24), d(2025, 4, 18), d(2025, 4, 20), d(2025, 5, 1), d(2025, 6, 8),
                d(2025, 6, 23), d(2025, 6, 24), d(2025, 8, 20), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("EE", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 24), d(2026, 4, 3), d(2026, 4, 5), d(2026, 5, 1), d(2026, 5, 24),
                d(2026, 6, 23), d(2026, 6, 24), d(2026, 8, 20), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("EE", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 24), d(2027, 3, 26), d(2027, 3, 28), d(2027, 5, 1), d(2027, 5, 16),
                d(2027, 6, 23), d(2027, 6, 24), d(2027, 8, 20), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("EE", 2027));
    }

    @Test
    void shortenedWorkingDaysAreNotHolidays() {
        var estonia = Observed.calendar("EE");

        assertTrue(estonia.isBusinessDay(d(2026, 2, 23)));
        assertTrue(estonia.isBusinessDay(d(2026, 6, 22)));
        assertTrue(estonia.isBusinessDay(d(2026, 12, 23)));
        assertTrue(estonia.isBusinessDay(d(2026, 12, 31)));
    }
}
