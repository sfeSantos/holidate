package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Belgium's ten statutory holidays under the law of 4 January 1974 and the
 * royal decree of 18 April 1974, as the FOD Werkgelegenheid lists them, with
 * the dates the Royal Observatory publishes for 2025, 2026 and 2027. The
 * Observatory also lists Easter Sunday and Whit Sunday, which the decree
 * does not name. A holiday that falls on a Sunday or on a day the company
 * does not work is replaced by a day each sector or company picks, so the
 * pack keeps the date and moves nothing.
 */
class BelgiumHolidaysTest {

    @Test
    void wettelijkeFeestdagen2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 29), d(2025, 6, 9), d(2025, 7, 21),
                d(2025, 8, 15), d(2025, 11, 1), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("BE", 2025));
    }

    @Test
    void wettelijkeFeestdagen2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 14), d(2026, 5, 25), d(2026, 7, 21),
                d(2026, 8, 15), d(2026, 11, 1), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("BE", 2026));
    }

    @Test
    void wettelijkeFeestdagen2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 17), d(2027, 7, 21),
                d(2027, 8, 15), d(2027, 11, 1), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("BE", 2027));
    }

    @Test
    void aSundayHolidayIsNotMoved() {
        var belgium = Observed.calendar("BE");

        assertTrue(belgium.isHoliday(d(2026, 11, 1)));
        assertTrue(belgium.isBusinessDay(d(2026, 11, 2)));
    }
}
