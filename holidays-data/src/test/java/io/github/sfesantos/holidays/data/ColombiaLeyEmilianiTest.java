package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayOccurrence;

/**
 * Colombia's national holidays under Ley 51 de 1983 (the "Ley Emiliani"),
 * article 1, and Ley 2578 de 2026, article 6, which adds 9 July from 2026.
 * No yearly list is published, so the dates are derived from the two laws.
 * Ten days named in Ley 51 and the new 9 July are taken on the following
 * Monday when they do not fall on one, the three that hang on Easter
 * included; the rest stay on their date, weekend or not.
 */
class ColombiaLeyEmilianiTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 3, 24), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1),
                d(2025, 6, 2), d(2025, 6, 23), d(2025, 6, 30), d(2025, 6, 30), d(2025, 7, 20), d(2025, 8, 7),
                d(2025, 8, 18), d(2025, 10, 13), d(2025, 11, 3), d(2025, 11, 17), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("CO", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 12), d(2026, 3, 23), d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1),
                d(2026, 5, 18), d(2026, 6, 8), d(2026, 6, 15), d(2026, 6, 29), d(2026, 7, 13), d(2026, 7, 20),
                d(2026, 8, 7), d(2026, 8, 17), d(2026, 10, 12), d(2026, 11, 2), d(2026, 11, 16), d(2026, 12, 8),
                d(2026, 12, 25)),
                Observed.datesIn("CO", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 11), d(2027, 3, 22), d(2027, 3, 25), d(2027, 3, 26), d(2027, 5, 1),
                d(2027, 5, 10), d(2027, 5, 31), d(2027, 6, 7), d(2027, 7, 5), d(2027, 7, 12), d(2027, 7, 20),
                d(2027, 8, 7), d(2027, 8, 16), d(2027, 10, 18), d(2027, 11, 1), d(2027, 11, 15), d(2027, 12, 8),
                d(2027, 12, 25)),
                Observed.datesIn("CO", 2027));
    }

    @Test
    void movedHolidayStaysWhenItFallsOnMonday() {
        var colombia = Observed.calendar("CO");

        assertTrue(colombia.isHoliday(d(2025, 1, 6)));
        assertTrue(colombia.isHoliday(d(2026, 6, 29)));
        assertFalse(colombia.isHoliday(d(2026, 1, 6)));
    }

    @Test
    void sacredHeartAndSaintPeterSharedAMondayIn2025() {
        var names = Observed.calendar("CO")
                .holidaysInMonth(YearMonth.of(2025, 6))
                .stream()
                .filter(occurrence -> occurrence.observed().equals(d(2025, 6, 30)))
                .map(HolidayOccurrence::name)
                .toList();

        assertEquals(List.of("Sagrado Corazón de Jesús", "San Pedro y San Pablo"), names);
    }

    @Test
    void chiquinquiraIsAHolidayFrom2026() {
        var colombia = Observed.calendar("CO");

        assertFalse(colombia.isHoliday(d(2025, 7, 14)));
        assertTrue(colombia.isHoliday(d(2026, 7, 13)));
    }
}
