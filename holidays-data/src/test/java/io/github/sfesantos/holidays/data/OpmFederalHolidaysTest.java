package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The eleven federal holidays of 5 U.S.C. 6103(a) on the days OPM's federal
 * holiday schedule gives for 2025 to 2028. A holiday on a Saturday is
 * observed on the Friday before and one on a Sunday on the Monday after
 * (5 U.S.C. 6103(b) and Executive Order 11582).
 *
 * <p>A year's list holds the holidays that belong to that year, each on its
 * observed day, the way OPM prints them. 1 January 2028 is a Saturday, so
 * New Year's Day 2028 is observed on Friday 31 December 2027: that date
 * opens the 2028 list and is not in the 2027 one, although
 * {@code holidayOn} and {@code isBusinessDay} see it on the 2027 date.
 *
 * <p>Inauguration Day (20 January 2025) is on OPM's 2025 schedule but only
 * for federal employees in the Washington, DC, area (5 U.S.C. 6103(c)), so
 * the pack leaves it out.
 */
class OpmFederalHolidaysTest {

    @Test
    void opm2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 20), d(2025, 2, 17), d(2025, 5, 26), d(2025, 6, 19), d(2025, 7, 4),
                d(2025, 9, 1), d(2025, 10, 13), d(2025, 11, 11), d(2025, 11, 27), d(2025, 12, 25)),
                Observed.datesIn("US", 2025));
    }

    @Test
    void opm2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 19), d(2026, 2, 16), d(2026, 5, 25), d(2026, 6, 19), d(2026, 7, 3),
                d(2026, 9, 7), d(2026, 10, 12), d(2026, 11, 11), d(2026, 11, 26), d(2026, 12, 25)),
                Observed.datesIn("US", 2026));
    }

    @Test
    void opm2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 18), d(2027, 2, 15), d(2027, 5, 31), d(2027, 6, 18), d(2027, 7, 5),
                d(2027, 9, 6), d(2027, 10, 11), d(2027, 11, 11), d(2027, 11, 25), d(2027, 12, 24)),
                Observed.datesIn("US", 2027));
    }

    @Test
    void opm2028() {
        assertEquals(List.of(
                d(2027, 12, 31), d(2028, 1, 17), d(2028, 2, 21), d(2028, 5, 29), d(2028, 6, 19), d(2028, 7, 4),
                d(2028, 9, 4), d(2028, 10, 9), d(2028, 11, 10), d(2028, 11, 23), d(2028, 12, 25)),
                Observed.datesIn("US", 2028));
    }

    @Test
    void newYearsDay2028IsObservedInThePreviousYear() {
        var federal = Observed.calendar("US");
        var newYear = federal.holidayOn(d(2027, 12, 31)).orElseThrow();

        assertEquals("New Year's Day", newYear.name());
        assertEquals(d(2028, 1, 1), newYear.original());
        assertTrue(newYear.isMoved());
        assertFalse(federal.isBusinessDay(d(2027, 12, 31)));
        assertFalse(federal.isHoliday(d(2028, 1, 1)));
    }

    @Test
    void aSaturdayHolidayMovesToFridayAndASundayOneToMonday() {
        var federal = Observed.calendar("US");

        assertEquals("Independence Day", federal.holidayOn(d(2026, 7, 3)).orElseThrow().name());
        assertFalse(federal.isHoliday(d(2026, 7, 4)));
        assertEquals("Independence Day", federal.holidayOn(d(2027, 7, 5)).orElseThrow().name());
        assertFalse(federal.isHoliday(d(2027, 7, 4)));
    }

    @Test
    void juneteenthIsFederalFrom2021() {
        var federal = Observed.calendar("US");

        assertFalse(federal.isHoliday(d(2020, 6, 19)));
        assertTrue(federal.isHoliday(d(2021, 6, 18)));
        assertEquals("Juneteenth National Independence Day", federal.holidayOn(d(2022, 6, 20)).orElseThrow().name());
    }

    @Test
    void holidaysKeepTheirStatutoryNames() {
        var federal = Observed.calendar("US");

        assertEquals("Washington's Birthday", federal.holidayOn(d(2026, 2, 16)).orElseThrow().name());
        assertEquals("Columbus Day", federal.holidayOn(d(2026, 10, 12)).orElseThrow().name());
    }
}
