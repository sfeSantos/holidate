package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Hungary's public holidays under § 102 (1) of the Labour Code (2012. évi
 * I. törvény). The official database could not be reached, so the text
 * comes from the consolidated version on Jogtár, checked against press
 * reports of the vote. There is no yearly list, so the dates are derived
 * from the law. Good Friday is a holiday from 2017 and 24 December from
 * 2026 (2026. évi LXXII. törvény, in force on 14 November 2026). The rest
 * days that a yearly decree swaps with working Saturdays are not public
 * holidays and are left out.
 */
class HungaryHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 15), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1), d(2025, 6, 9),
                d(2025, 8, 20), d(2025, 10, 23), d(2025, 11, 1), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("HU", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 3, 15), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 25),
                d(2026, 8, 20), d(2026, 10, 23), d(2026, 11, 1), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("HU", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 15), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 17),
                d(2027, 8, 20), d(2027, 10, 23), d(2027, 11, 1), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("HU", 2027));
    }

    @Test
    void christmasEveIsAHolidayFrom2026() {
        var hungary = Observed.calendar("HU");

        assertFalse(hungary.isHoliday(d(2025, 12, 24)));
        assertTrue(hungary.isHoliday(d(2026, 12, 24)));
        assertFalse(hungary.isBusinessDay(d(2026, 12, 24)));
    }

    @Test
    void goodFridayIsAHolidayFrom2017() {
        var hungary = Observed.calendar("HU");

        assertFalse(hungary.isHoliday(d(2016, 3, 25)));
        assertTrue(hungary.isHoliday(d(2017, 4, 14)));
    }
}
