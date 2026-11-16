package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Massachusetts: the federal list plus Patriots' Day, the third Monday in
 * April, a legal holiday under Massachusetts General Laws chapter 4,
 * section 7, clause Eighteenth. Its dates are derived from that rule and
 * agree with the state's published lists of legal holidays.
 *
 * <p>The other days follow the federal layer, so a Saturday holiday shows
 * on the Friday before. The state itself keeps a Saturday holiday on the
 * Saturday (4 July 2026, 19 June and 25 December 2027) and only moves a
 * Sunday one to Monday.
 */
class MassachusettsLegalHolidaysTest {

    @Test
    void massachusetts2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 20), d(2025, 2, 17), d(2025, 4, 21), d(2025, 5, 26), d(2025, 6, 19),
                d(2025, 7, 4), d(2025, 9, 1), d(2025, 10, 13), d(2025, 11, 11), d(2025, 11, 27), d(2025, 12, 25)),
                Observed.datesIn("US-MA", 2025));
    }

    @Test
    void massachusetts2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 19), d(2026, 2, 16), d(2026, 4, 20), d(2026, 5, 25), d(2026, 6, 19),
                d(2026, 7, 3), d(2026, 9, 7), d(2026, 10, 12), d(2026, 11, 11), d(2026, 11, 26), d(2026, 12, 25)),
                Observed.datesIn("US-MA", 2026));
    }

    @Test
    void massachusetts2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 18), d(2027, 2, 15), d(2027, 4, 19), d(2027, 5, 31), d(2027, 6, 18),
                d(2027, 7, 5), d(2027, 9, 6), d(2027, 10, 11), d(2027, 11, 11), d(2027, 11, 25), d(2027, 12, 24)),
                Observed.datesIn("US-MA", 2027));
    }

    @Test
    void patriotsDayIsADayOffOnlyInMassachusetts() {
        var patriotsDay = Observed.calendar("US-MA").holidayOn(d(2026, 4, 20)).orElseThrow();

        assertEquals("Patriots' Day", patriotsDay.name());
        assertFalse(Observed.calendar("US-MA").isBusinessDay(d(2026, 4, 20)));
        assertTrue(Observed.calendar("US").isBusinessDay(d(2026, 4, 20)));
    }
}
