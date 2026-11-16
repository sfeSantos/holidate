package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Malta under the National Holidays and Other Public Holidays Act (Cap.
 * 252): the five national holidays of article 2 and the nine public
 * holidays of the Schedule. No yearly list is published, so the dates are
 * derived from the Act. A holiday on a weekend is not moved; since 2021
 * article 6 gives a day of leave instead, which is not a calendar date.
 */
class MaltaHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 10), d(2025, 3, 19), d(2025, 3, 31), d(2025, 4, 18), d(2025, 5, 1),
                d(2025, 6, 7), d(2025, 6, 29), d(2025, 8, 15), d(2025, 9, 8), d(2025, 9, 21), d(2025, 12, 8),
                d(2025, 12, 13), d(2025, 12, 25)),
                Observed.datesIn("MT", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 10), d(2026, 3, 19), d(2026, 3, 31), d(2026, 4, 3), d(2026, 5, 1),
                d(2026, 6, 7), d(2026, 6, 29), d(2026, 8, 15), d(2026, 9, 8), d(2026, 9, 21), d(2026, 12, 8),
                d(2026, 12, 13), d(2026, 12, 25)),
                Observed.datesIn("MT", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 10), d(2027, 3, 19), d(2027, 3, 26), d(2027, 3, 31), d(2027, 5, 1),
                d(2027, 6, 7), d(2027, 6, 29), d(2027, 8, 15), d(2027, 9, 8), d(2027, 9, 21), d(2027, 12, 8),
                d(2027, 12, 13), d(2027, 12, 25)),
                Observed.datesIn("MT", 2027));
    }

    @Test
    void aWeekendHolidayIsNotMoved() {
        var malta = Observed.calendar("MT");

        assertTrue(malta.isHoliday(d(2026, 6, 7)));
        assertTrue(malta.isBusinessDay(d(2026, 6, 8)));
        assertTrue(malta.isBusinessDay(d(2026, 8, 17)));
    }
}
