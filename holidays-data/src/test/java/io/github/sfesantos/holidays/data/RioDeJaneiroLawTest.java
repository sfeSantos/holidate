package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * The state of Rio de Janeiro under Lei Estadual 5.243/2008 (São Jorge,
 * Carnival Tuesday and 20 November, the last one a state holiday since
 * 2002) and the city under its municipal calendar: São Sebastião on 20
 * January and Ash Wednesday as a half day.
 */
class RioDeJaneiroLawTest {

    @Test
    void state2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 3), d(2025, 3, 4), d(2025, 4, 18), d(2025, 4, 21), d(2025, 4, 23),
                d(2025, 5, 1), d(2025, 6, 19), d(2025, 9, 7), d(2025, 10, 12), d(2025, 11, 2), d(2025, 11, 15),
                d(2025, 11, 20), d(2025, 12, 25)),
                Observed.datesIn("BR-RJ", 2025));
    }

    @Test
    void state2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 2, 17), d(2026, 4, 3), d(2026, 4, 21), d(2026, 4, 23),
                d(2026, 5, 1), d(2026, 6, 4), d(2026, 9, 7), d(2026, 10, 12), d(2026, 11, 2), d(2026, 11, 15),
                d(2026, 11, 20), d(2026, 12, 25)),
                Observed.datesIn("BR-RJ", 2026));
    }

    @Test
    void state2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 26), d(2027, 4, 21), d(2027, 4, 23),
                d(2027, 5, 1), d(2027, 5, 27), d(2027, 9, 7), d(2027, 10, 12), d(2027, 11, 2), d(2027, 11, 15),
                d(2027, 11, 20), d(2027, 12, 25)),
                Observed.datesIn("BR-RJ", 2027));
    }

    @Test
    void city2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 20), d(2025, 3, 3), d(2025, 3, 4), d(2025, 3, 5), d(2025, 4, 18),
                d(2025, 4, 21), d(2025, 4, 23), d(2025, 5, 1), d(2025, 6, 19), d(2025, 9, 7), d(2025, 10, 12),
                d(2025, 11, 2), d(2025, 11, 15), d(2025, 11, 20), d(2025, 12, 25)),
                Observed.datesIn("BR-RJ-RIO", 2025));
    }

    @Test
    void city2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 20), d(2026, 2, 16), d(2026, 2, 17), d(2026, 2, 18), d(2026, 4, 3),
                d(2026, 4, 21), d(2026, 4, 23), d(2026, 5, 1), d(2026, 6, 4), d(2026, 9, 7), d(2026, 10, 12),
                d(2026, 11, 2), d(2026, 11, 15), d(2026, 11, 20), d(2026, 12, 25)),
                Observed.datesIn("BR-RJ-RIO", 2026));
    }

    @Test
    void city2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 20), d(2027, 2, 8), d(2027, 2, 9), d(2027, 2, 10), d(2027, 3, 26),
                d(2027, 4, 21), d(2027, 4, 23), d(2027, 5, 1), d(2027, 5, 27), d(2027, 9, 7), d(2027, 10, 12),
                d(2027, 11, 2), d(2027, 11, 15), d(2027, 11, 20), d(2027, 12, 25)),
                Observed.datesIn("BR-RJ-RIO", 2027));
    }

    @Test
    void theStateMakesCarnivalTuesdayARealHoliday() {
        var rio = Observed.calendar("BR-RJ-RIO");
        var tuesday = rio.holidayOn(d(2026, 2, 17)).orElseThrow();

        assertEquals(HolidayType.PUBLIC, tuesday.type());
        assertEquals("BR-RJ", tuesday.layer());
        assertFalse(rio.isBusinessDay(d(2026, 2, 17)));
        assertTrue(rio.isBusinessDay(d(2026, 2, 16)));
    }

    @Test
    void ashWednesdayIsAHalfDayInTheCity() {
        var rio = Observed.calendar("BR-RJ-RIO");

        assertTrue(rio.isHalfDay(d(2026, 2, 18)));
        assertTrue(rio.isBusinessDay(d(2026, 2, 18)));
        assertFalse(Observed.calendar("BR-RJ").isHoliday(d(2026, 2, 18)));
    }

    @Test
    void blackAwarenessDayWasAStateHolidayBefore2024() {
        assertTrue(Observed.calendar("BR-RJ").isHoliday(d(2023, 11, 20)));
        assertEquals("BR-RJ", Observed.calendar("BR-RJ").holidayOn(d(2026, 11, 20)).orElseThrow().layer());
    }
}
