package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Belize's public and bank holidays as the Government Press Office notifies
 * them each year under the Holidays Act, Chapter 289: PR#163-24 (2025),
 * PR#218-25 (2026) and PR#271-26 (2027). A holiday on a Sunday is kept on the
 * Monday (section 3(2)). 9 March and 12 October go to the Monday before when
 * they fall from Tuesday to Thursday and to the Monday after on a Friday
 * (section 3(3)). The 2027 notice also moves Labour Day from Saturday 1 May
 * to Monday 3 May. 12 March 2025 was a holiday by notice under section 9,
 * for the general election.
 */
class BelizeHolidaysTest {

    @Test
    void pressOffice2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 15), d(2025, 3, 10), d(2025, 3, 12), d(2025, 4, 18), d(2025, 4, 19),
                d(2025, 4, 21), d(2025, 5, 1), d(2025, 8, 1), d(2025, 9, 10), d(2025, 9, 22), d(2025, 10, 13),
                d(2025, 11, 19), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("BZ", 2025));
    }

    @Test
    void pressOffice2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 15), d(2026, 3, 9), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 6),
                d(2026, 5, 1), d(2026, 8, 1), d(2026, 9, 10), d(2026, 9, 21), d(2026, 10, 12), d(2026, 11, 19),
                d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("BZ", 2026));
    }

    @Test
    void pressOffice2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 15), d(2027, 3, 8), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 29),
                d(2027, 5, 3), d(2027, 8, 2), d(2027, 9, 10), d(2027, 9, 21), d(2027, 10, 11), d(2027, 11, 19),
                d(2027, 12, 25), d(2027, 12, 27)),
                Observed.datesIn("BZ", 2027));
    }

    @Test
    void sundayHolidayIsKeptOnTheMonday() {
        var belize = Observed.calendar("BZ");

        assertFalse(belize.isHoliday(d(2025, 9, 21)));
        assertTrue(belize.isHoliday(d(2025, 9, 22)));
        assertTrue(belize.isHoliday(d(2026, 8, 1)));
    }

    @Test
    void heroesDayGoesToTheMondayBeforeFromTuesdayToThursday() {
        var belize = Observed.calendar("BZ");

        assertTrue(belize.isHoliday(d(2027, 3, 8)));
        assertFalse(belize.isHoliday(d(2027, 3, 9)));
        assertTrue(belize.isHoliday(d(2029, 3, 12)));
    }

    @Test
    void electionDayIsAHolidayIn2025Only() {
        var belize = Observed.calendar("BZ");

        assertTrue(belize.isHoliday(d(2025, 3, 12)));
        assertFalse(belize.isHoliday(d(2026, 3, 12)));
    }

    @Test
    void georgePriceDayAndEmancipationDayStartIn2021() {
        var belize = Observed.calendar("BZ");

        assertFalse(belize.isHoliday(d(2020, 1, 15)));
        assertTrue(belize.isHoliday(d(2021, 1, 15)));
        assertFalse(belize.isHoliday(d(2019, 8, 1)));
        assertTrue(belize.isHoliday(d(2023, 8, 1)));
    }
}
