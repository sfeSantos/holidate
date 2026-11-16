package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Argentina's national holidays under Ley 27.399, checked against the
 * calendar the Ministerio del Interior publishes at argentina.gob.ar/feriados
 * for 2025 and 2026. The 2027 calendar is not published yet, so that year is
 * derived from the law. Jueves Santo and the "días no laborables con fines
 * turísticos" (Decreto 1027/2024 for 2025, Resolución 164/2025 for 2026) are
 * non-working days at the employer's choice, kept as {@code OBSERVANCE}. 9
 * November 2026 is the national holiday of Decreto 1103/2026.
 *
 * <p>Three of the four "feriados trasladables" are not in the pack, because
 * no rule gives their dates for the three years: 17 August, 12 October and 20
 * November. The lists below are the official ones without those three and
 * without the days that bind one religious community only.
 */
class ArgentinaHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 3), d(2025, 3, 4), d(2025, 3, 24), d(2025, 4, 2), d(2025, 4, 17),
                d(2025, 4, 18), d(2025, 5, 1), d(2025, 5, 2), d(2025, 5, 25), d(2025, 6, 16), d(2025, 6, 20),
                d(2025, 7, 9), d(2025, 8, 15), d(2025, 11, 21), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("AR", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 2, 17), d(2026, 3, 23), d(2026, 3, 24), d(2026, 4, 2),
                d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1), d(2026, 5, 25), d(2026, 6, 15), d(2026, 6, 20),
                d(2026, 7, 9), d(2026, 7, 10), d(2026, 11, 9), d(2026, 12, 7), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("AR", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 24), d(2027, 3, 25), d(2027, 3, 26),
                d(2027, 4, 2), d(2027, 5, 1), d(2027, 5, 25), d(2027, 6, 20), d(2027, 6, 21), d(2027, 7, 9),
                d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("AR", 2027));
    }

    @Test
    void guemesMovesToTheMondayTheLawNames() {
        var argentina = Observed.calendar("AR");

        assertTrue(argentina.isHoliday(d(2025, 6, 16)));
        assertFalse(argentina.isHoliday(d(2025, 6, 17)));
        assertTrue(argentina.isHoliday(d(2026, 6, 15)));
        assertTrue(argentina.isHoliday(d(2027, 6, 21)));
        assertFalse(argentina.isHoliday(d(2027, 6, 17)));
    }

    @Test
    void nonWorkingDaysStayBusinessDays() {
        var argentina = Observed.calendar("AR");

        assertEquals(HolidayType.OBSERVANCE, argentina.holidayOn(d(2025, 4, 17)).orElseThrow().type());
        assertTrue(argentina.isBusinessDay(d(2025, 4, 17)));
        assertTrue(argentina.isBusinessDay(d(2025, 5, 2)));
        assertTrue(argentina.isBusinessDay(d(2026, 12, 7)));
        assertFalse(argentina.isBusinessDay(d(2026, 12, 8)));
        assertFalse(argentina.isBusinessDay(d(2026, 2, 17)));
    }

    @Test
    void papalVisitIsAHolidayIn2026Only() {
        var argentina = Observed.calendar("AR");

        assertFalse(argentina.isBusinessDay(d(2026, 11, 9)));
        assertFalse(argentina.isHoliday(d(2025, 11, 9)));
        assertFalse(argentina.isHoliday(d(2027, 11, 9)));
    }
}
