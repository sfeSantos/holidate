package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Guatemala's paid holidays under Código de Trabajo art. 127, with the dates
 * derived from the law since no yearly list is published. Decreto 42-2010 as
 * amended by Decreto 19-2018 moves the 30 June holiday to a Monday: the one
 * before when it falls on a Tuesday or Wednesday, the one after from Thursday
 * to Sunday. The Corte de Constitucionalidad (expediente 5536-2018, 17 March
 * 2020) took 1 May and 20 October out of that rule, so nothing else moves.
 * 24 and 31 December are holidays from noon.
 */
class GuatemalaHolidaysTest {

    @Test
    void labourCode2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 19), d(2025, 5, 1), d(2025, 6, 30),
                d(2025, 9, 15), d(2025, 10, 20), d(2025, 11, 1), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 31)),
                Observed.datesIn("GT", 2025));
    }

    @Test
    void labourCode2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 4), d(2026, 5, 1), d(2026, 6, 29),
                d(2026, 9, 15), d(2026, 10, 20), d(2026, 11, 1), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 31)),
                Observed.datesIn("GT", 2026));
    }

    @Test
    void labourCode2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 3, 27), d(2027, 5, 1), d(2027, 6, 28),
                d(2027, 9, 15), d(2027, 10, 20), d(2027, 11, 1), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 31)),
                Observed.datesIn("GT", 2027));
    }

    @Test
    void armyDayIsTakenOnTheNearestMonday() {
        var guatemala = Observed.calendar("GT");

        assertTrue(guatemala.isHoliday(d(2026, 6, 29)));
        assertFalse(guatemala.isHoliday(d(2026, 6, 30)));
        assertTrue(guatemala.isHoliday(d(2028, 7, 3)));
        assertTrue(guatemala.isHoliday(d(2030, 7, 1)));
    }

    @Test
    void revolutionDayStaysOnItsDate() {
        var guatemala = Observed.calendar("GT");

        assertTrue(guatemala.isHoliday(d(2026, 10, 20)));
        assertFalse(guatemala.isHoliday(d(2026, 10, 19)));
    }

    @Test
    void christmasEveAndNewYearsEveAreHalfDays() {
        var guatemala = Observed.calendar("GT");

        assertEquals(HolidayType.HALF_DAY, guatemala.holidayOn(d(2026, 12, 24)).orElseThrow().type());
        assertEquals(HolidayType.HALF_DAY, guatemala.holidayOn(d(2026, 12, 31)).orElseThrow().type());
    }
}
