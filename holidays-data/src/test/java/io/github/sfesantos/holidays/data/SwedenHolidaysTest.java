package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Sweden's public holidays under lag (1989:253) om allmänna helgdagar as
 * amended by SFS 2004:1320, which made nationaldagen a holiday and dropped
 * annandag pingst from 2005. No yearly list is published, so the dates are
 * derived from the act: midsommardagen is the Saturday from 20 to 26 June,
 * alla helgons dag the Saturday from 31 October to 6 November. Midsommarafton,
 * julafton and nyårsafton are customary days off the act does not name, kept
 * as {@code OBSERVANCE}.
 */
class SwedenHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1),
                d(2025, 5, 29), d(2025, 6, 6), d(2025, 6, 8), d(2025, 6, 20), d(2025, 6, 21), d(2025, 11, 1),
                d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26), d(2025, 12, 31)),
                Observed.datesIn("SE", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1),
                d(2026, 5, 14), d(2026, 5, 24), d(2026, 6, 6), d(2026, 6, 19), d(2026, 6, 20), d(2026, 10, 31),
                d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26), d(2026, 12, 31)),
                Observed.datesIn("SE", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1),
                d(2027, 5, 6), d(2027, 5, 16), d(2027, 6, 6), d(2027, 6, 25), d(2027, 6, 26), d(2027, 11, 6),
                d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26), d(2027, 12, 31)),
                Observed.datesIn("SE", 2027));
    }

    @Test
    void evesStayBusinessDays() {
        var sweden = Observed.calendar("SE");

        assertEquals(HolidayType.OBSERVANCE, sweden.holidayOn(d(2026, 6, 19)).orElseThrow().type());
        assertTrue(sweden.isBusinessDay(d(2026, 6, 19)));
        assertTrue(sweden.isBusinessDay(d(2026, 12, 24)));
        assertTrue(sweden.isBusinessDay(d(2026, 12, 31)));
        assertFalse(sweden.isBusinessDay(d(2026, 12, 25)));
    }

    @Test
    void midsummerAndAllSaintsStayInsideTheirWindows() {
        var sweden = Observed.calendar("SE");

        assertTrue(sweden.isHoliday(d(2020, 6, 20)));
        assertTrue(sweden.isHoliday(d(2021, 6, 26)));
        assertTrue(sweden.isHoliday(d(2020, 10, 31)));
        assertTrue(sweden.isHoliday(d(2021, 11, 6)));
    }

    @Test
    void nationaldagenReplacesAnnandagPingstIn2005() {
        var sweden = Observed.calendar("SE");

        assertTrue(sweden.isHoliday(d(2004, 5, 31)));
        assertFalse(sweden.isHoliday(d(2005, 5, 16)));
        assertFalse(sweden.isHoliday(d(2003, 6, 6)));
        assertTrue(sweden.isHoliday(d(2005, 6, 6)));
    }
}
