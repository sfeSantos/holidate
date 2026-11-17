package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Croatia's public holidays under article 1 of the Zakon o blagdanima,
 * spomendanima i neradnim danima u Republici Hrvatskoj (Narodne novine
 * 110/2019), in force since 1 January 2020. There is no yearly list, so the
 * dates are derived from the law. It names Easter Sunday, and article 4
 * says a holiday on a Sunday does not move. The 2020 law added 18 November,
 * put Dan državnosti on 30 May and turned 25 June and 8 October into
 * memorial days. Dan državnosti had been on 25 June since the amendment in
 * Narodne novine 96/2001, first observed in 2002.
 */
class CroatiaHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 30),
                d(2025, 6, 19), d(2025, 6, 22), d(2025, 8, 5), d(2025, 8, 15), d(2025, 11, 1), d(2025, 11, 18),
                d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("HR", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 30),
                d(2026, 6, 4), d(2026, 6, 22), d(2026, 8, 5), d(2026, 8, 15), d(2026, 11, 1), d(2026, 11, 18),
                d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("HR", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 27),
                d(2027, 5, 30), d(2027, 6, 22), d(2027, 8, 5), d(2027, 8, 15), d(2027, 11, 1), d(2027, 11, 18),
                d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("HR", 2027));
    }

    @Test
    void reformOf2020ChangedTheList() {
        var croatia = Observed.calendar("HR");

        assertFalse(croatia.isHoliday(d(2019, 11, 18)));
        assertTrue(croatia.isHoliday(d(2020, 11, 18)));
        assertFalse(croatia.isHoliday(d(2019, 5, 30)));
        assertTrue(croatia.isHoliday(d(2020, 5, 30)));
        assertTrue(croatia.isHoliday(d(2019, 10, 8)));
        assertFalse(croatia.isHoliday(d(2020, 10, 8)));
        assertFalse(croatia.isHoliday(d(2025, 6, 25)));
    }

    @Test
    void danDrzavnostiWasOn25JuneFrom2002To2019() {
        var croatia = Observed.calendar("HR");

        assertFalse(croatia.isHoliday(d(2001, 6, 25)));
        assertEquals("Dan državnosti", croatia.holidayOn(d(2002, 6, 25)).orElseThrow().name());
        assertEquals("Dan državnosti", croatia.holidayOn(d(2019, 6, 25)).orElseThrow().name());
        assertFalse(croatia.isHoliday(d(2020, 6, 25)));
        assertEquals("Dan državnosti", croatia.holidayOn(d(2020, 5, 30)).orElseThrow().name());
    }
}
