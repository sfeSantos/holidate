package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Catalunya as the DOGC publishes it: Ordre EMT/85/2024 (2025), Ordre
 * EMT/66/2025 (2026) and Ordre EMT/52/2026 (2027). Catalunya does not move
 * a holiday that falls on a Sunday; the order simply leaves it out, so
 * these lists are compared without Sundays.
 */
class DogcCatalunyaHolidaysTest {

    @Test
    void dogc2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1), d(2025, 6, 24),
                d(2025, 8, 15), d(2025, 9, 11), d(2025, 11, 1), d(2025, 12, 6), d(2025, 12, 8), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.weekdayDatesIn("ES-CT", 2025));
    }

    @Test
    void dogc2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 1), d(2026, 6, 24),
                d(2026, 8, 15), d(2026, 9, 11), d(2026, 10, 12), d(2026, 12, 8), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.weekdayDatesIn("ES-CT", 2026));
    }

    @Test
    void dogc2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1), d(2027, 6, 24),
                d(2027, 9, 11), d(2027, 10, 12), d(2027, 11, 1), d(2027, 12, 6), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.weekdayDatesIn("ES-CT", 2027));
    }

    @Test
    void aSundayHolidayStaysOnSunday() {
        var catalunya = Observed.calendar("ES-CT");

        assertTrue(catalunya.isHoliday(d(2026, 11, 1)));
        assertTrue(catalunya.isBusinessDay(d(2026, 11, 2)));
    }
}
