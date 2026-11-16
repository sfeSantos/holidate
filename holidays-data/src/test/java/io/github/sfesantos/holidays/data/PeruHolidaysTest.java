package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Peru's national holidays under article 6 of Decreto Legislativo 713 as
 * last replaced by Ley 31822, checked against the lists at gob.pe/feriados
 * for 2025 and 2026. The 2027 list is not published yet, so that year is
 * derived from the law. Article 7 keeps every holiday on its date. The "días
 * no laborables" decreed for the public sector are not holidays and are not
 * in the pack.
 */
class PeruHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1), d(2025, 6, 7), d(2025, 6, 29),
                d(2025, 7, 23), d(2025, 7, 28), d(2025, 7, 29), d(2025, 8, 6), d(2025, 8, 30), d(2025, 10, 8),
                d(2025, 11, 1), d(2025, 12, 8), d(2025, 12, 9), d(2025, 12, 25)),
                Observed.datesIn("PE", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1), d(2026, 6, 7), d(2026, 6, 29),
                d(2026, 7, 23), d(2026, 7, 28), d(2026, 7, 29), d(2026, 8, 6), d(2026, 8, 30), d(2026, 10, 8),
                d(2026, 11, 1), d(2026, 12, 8), d(2026, 12, 9), d(2026, 12, 25)),
                Observed.datesIn("PE", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 5, 1), d(2027, 6, 7), d(2027, 6, 29),
                d(2027, 7, 23), d(2027, 7, 28), d(2027, 7, 29), d(2027, 8, 6), d(2027, 8, 30), d(2027, 10, 8),
                d(2027, 11, 1), d(2027, 12, 8), d(2027, 12, 9), d(2027, 12, 25)),
                Observed.datesIn("PE", 2027));
    }

    @Test
    void recentHolidaysStartInTheYearTheyWereFirstObserved() {
        var peru = Observed.calendar("PE");

        assertFalse(peru.isHoliday(d(2021, 12, 9)));
        assertTrue(peru.isHoliday(d(2022, 12, 9)));
        assertFalse(peru.isHoliday(d(2021, 8, 6)));
        assertTrue(peru.isHoliday(d(2022, 8, 6)));
        assertFalse(peru.isHoliday(d(2022, 7, 23)));
        assertTrue(peru.isHoliday(d(2023, 7, 23)));
        assertFalse(peru.isHoliday(d(2023, 6, 7)));
        assertTrue(peru.isHoliday(d(2024, 6, 7)));
    }

    @Test
    void publicSectorNonWorkingDaysAreNotHolidays() {
        var peru = Observed.calendar("PE");

        assertTrue(peru.isBusinessDay(d(2025, 5, 2)));
        assertTrue(peru.isBusinessDay(d(2025, 12, 26)));
        assertTrue(peru.isBusinessDay(d(2026, 7, 27)));
    }
}
