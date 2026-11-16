package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Metropolitan France under Code du travail, article L3133-1, with the
 * names and the 2026 and 2027 dates of the Service-Public.fr page "Jours
 * fériés dans le secteur privé" (F2405). That page no longer lists 2025, so
 * 2025 is derived from the article. Lundi de Pentecôte is one of the eleven
 * legal holidays; an employer may pick it as the journée de solidarité, but
 * that is a company matter and the day stays {@code PUBLIC}. Alsace-Moselle
 * and the overseas departments are not covered.
 */
class FranceHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 8), d(2025, 5, 29), d(2025, 6, 9),
                d(2025, 7, 14), d(2025, 8, 15), d(2025, 11, 1), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("FR", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 8), d(2026, 5, 14), d(2026, 5, 25),
                d(2026, 7, 14), d(2026, 8, 15), d(2026, 11, 1), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("FR", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 8), d(2027, 5, 17),
                d(2027, 7, 14), d(2027, 8, 15), d(2027, 11, 1), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("FR", 2027));
    }

    @Test
    void whitMondayIsAPublicHoliday() {
        var france = Observed.calendar("FR");

        assertEquals(HolidayType.PUBLIC, france.holidayOn(d(2026, 5, 25)).orElseThrow().type());
        assertFalse(france.isBusinessDay(d(2026, 5, 25)));
    }

    @Test
    void aWeekendHolidayIsNotMoved() {
        var france = Observed.calendar("FR");

        assertTrue(france.isHoliday(d(2026, 11, 1)));
        assertTrue(france.isBusinessDay(d(2026, 11, 2)));
    }
}
