package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Lithuania's public holidays under Darbo kodeksas, article 123. No yearly
 * list is published, so the dates are derived from the article. Nothing
 * moves when a holiday falls on a weekend. 2 November is a holiday from 2020.
 */
class LithuaniaHolidaysTest {

    @Test
    void labourCode2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 16), d(2025, 3, 11), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1),
                d(2025, 5, 4), d(2025, 6, 1), d(2025, 6, 24), d(2025, 7, 6), d(2025, 8, 15), d(2025, 11, 1),
                d(2025, 11, 2), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("LT", 2025));
    }

    @Test
    void labourCode2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 3, 11), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1),
                d(2026, 5, 3), d(2026, 6, 7), d(2026, 6, 24), d(2026, 7, 6), d(2026, 8, 15), d(2026, 11, 1),
                d(2026, 11, 2), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("LT", 2026));
    }

    @Test
    void labourCode2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 16), d(2027, 3, 11), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1),
                d(2027, 5, 2), d(2027, 6, 6), d(2027, 6, 24), d(2027, 7, 6), d(2027, 8, 15), d(2027, 11, 1),
                d(2027, 11, 2), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("LT", 2027));
    }

    @Test
    void allSoulsDayIsAHolidayFrom2020() {
        var lithuania = Observed.calendar("LT");

        assertFalse(lithuania.isHoliday(d(2018, 11, 2)));
        assertTrue(lithuania.isBusinessDay(d(2018, 11, 2)));
        assertTrue(lithuania.isHoliday(d(2020, 11, 2)));
        assertFalse(lithuania.isBusinessDay(d(2020, 11, 2)));
    }
}
