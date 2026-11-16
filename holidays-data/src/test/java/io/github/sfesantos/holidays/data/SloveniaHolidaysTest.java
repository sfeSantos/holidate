package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Slovenia's holidays and work-free days under articles 1 and 2 of the
 * Zakon o praznikih in dela prostih dnevih v Republiki Sloveniji (ZPDPD),
 * read in the consolidated texts on PISRS up to ZPDPD-I (Uradni list RS,
 * št. 1567/26). There is no yearly list, so the dates are derived from the
 * law. Holidays that article 1 excludes from the work-free days are
 * {@code OBSERVANCE}. 8 June 2025 appears twice: it is both Whit Sunday and
 * dan Primoža Trubarja. Article 3 says a holiday on a Sunday does not move.
 */
class SloveniaHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 2), d(2025, 2, 8), d(2025, 4, 20), d(2025, 4, 21), d(2025, 4, 27),
                d(2025, 5, 1), d(2025, 5, 2), d(2025, 6, 8), d(2025, 6, 8), d(2025, 6, 25), d(2025, 8, 15),
                d(2025, 8, 17), d(2025, 9, 15), d(2025, 9, 23), d(2025, 10, 25), d(2025, 10, 31), d(2025, 11, 1),
                d(2025, 11, 10), d(2025, 11, 23), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("SI", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 2), d(2026, 2, 8), d(2026, 4, 5), d(2026, 4, 6), d(2026, 4, 27),
                d(2026, 5, 1), d(2026, 5, 2), d(2026, 5, 24), d(2026, 6, 8), d(2026, 6, 25), d(2026, 8, 15),
                d(2026, 8, 17), d(2026, 9, 15), d(2026, 9, 23), d(2026, 10, 25), d(2026, 10, 31), d(2026, 11, 1),
                d(2026, 11, 10), d(2026, 11, 23), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("SI", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 2), d(2027, 2, 8), d(2027, 3, 28), d(2027, 3, 29), d(2027, 4, 27),
                d(2027, 5, 1), d(2027, 5, 2), d(2027, 5, 16), d(2027, 6, 8), d(2027, 6, 25), d(2027, 8, 7),
                d(2027, 8, 15), d(2027, 8, 17), d(2027, 9, 15), d(2027, 9, 23), d(2027, 10, 25), d(2027, 10, 31),
                d(2027, 11, 1), d(2027, 11, 10), d(2027, 11, 23), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("SI", 2027));
    }

    @Test
    void holidaysThatAreNotWorkFreeStayBusinessDays() {
        var slovenia = Observed.calendar("SI");

        assertEquals(HolidayType.OBSERVANCE, slovenia.holidayOn(d(2026, 6, 8)).orElseThrow().type());
        assertTrue(slovenia.isBusinessDay(d(2026, 6, 8)));
        assertTrue(slovenia.isBusinessDay(d(2026, 8, 17)));
        assertTrue(slovenia.isBusinessDay(d(2026, 9, 15)));
        assertTrue(slovenia.isBusinessDay(d(2026, 9, 23)));
        assertTrue(slovenia.isBusinessDay(d(2026, 11, 10)));
        assertTrue(slovenia.isBusinessDay(d(2026, 11, 23)));
        assertFalse(slovenia.isBusinessDay(d(2026, 6, 25)));
    }

    @Test
    void laterHolidaysStartInTheirYear() {
        var slovenia = Observed.calendar("SI");

        assertFalse(slovenia.isHoliday(d(2016, 1, 2)));
        assertTrue(slovenia.isHoliday(d(2017, 1, 2)));
        assertFalse(slovenia.isHoliday(d(2024, 11, 10)));
        assertTrue(slovenia.isHoliday(d(2025, 11, 10)));
        assertFalse(slovenia.isHoliday(d(2026, 8, 7)));
        assertTrue(slovenia.isHoliday(d(2027, 8, 7)));
    }
}
