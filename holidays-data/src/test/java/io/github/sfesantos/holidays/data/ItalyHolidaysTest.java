package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Italy's holidays under Legge 260/1949, articoli 1 and 2, as it stands on
 * Normattiva after Legge 54/1977 (which dropped five religious days and 4
 * November), D.P.R. 792/1985 (Epiphany back), Legge 336/2000 (2 June back
 * on its date from 2001) and Legge 151/2025 (4 October, San Francesco
 * d'Assisi, from 2026). No yearly list is published, so the dates are
 * derived from the law. Every Sunday is a holiday there, so Easter Sunday
 * is not named. Patron saint days are local and not covered.
 */
class ItalyHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 21), d(2025, 4, 25), d(2025, 5, 1), d(2025, 6, 2),
                d(2025, 8, 15), d(2025, 11, 1), d(2025, 12, 8), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("IT", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 6), d(2026, 4, 25), d(2026, 5, 1), d(2026, 6, 2),
                d(2026, 8, 15), d(2026, 10, 4), d(2026, 11, 1), d(2026, 12, 8), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("IT", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 29), d(2027, 4, 25), d(2027, 5, 1), d(2027, 6, 2),
                d(2027, 8, 15), d(2027, 10, 4), d(2027, 11, 1), d(2027, 12, 8), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("IT", 2027));
    }

    @Test
    void sanFrancescoIsAHolidayFrom2026() {
        var italy = Observed.calendar("IT");

        assertFalse(italy.isHoliday(d(2025, 10, 4)));
        assertTrue(italy.isHoliday(d(2026, 10, 4)));
        assertFalse(italy.isBusinessDay(d(2027, 10, 4)));
    }

    @Test
    void republicDayIsBackOnItsDateFrom2001() {
        var italy = Observed.calendar("IT");

        assertFalse(italy.isHoliday(d(2000, 6, 2)));
        assertTrue(italy.isHoliday(d(2001, 6, 2)));
    }
}
