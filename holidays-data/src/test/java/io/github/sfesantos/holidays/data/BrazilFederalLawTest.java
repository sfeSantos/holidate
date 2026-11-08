package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Brazil's national holidays under Lei 662/1949 as amended by Lei
 * 10.607/2002, Lei 6.802/1980 (12 October) and Lei 14.759/2023 (20
 * November, from 2024). Carnival and Corpus Christi are a federal "ponto
 * facultativo", an optional day off, kept as {@code OBSERVANCE}.
 */
class BrazilFederalLawTest {

    @Test
    void federal2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 3), d(2025, 3, 4), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1),
                d(2025, 6, 19), d(2025, 9, 7), d(2025, 10, 12), d(2025, 11, 2), d(2025, 11, 15), d(2025, 11, 20),
                d(2025, 12, 25)),
                Observed.datesIn("BR", 2025));
    }

    @Test
    void federal2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 2, 17), d(2026, 4, 3), d(2026, 4, 21), d(2026, 5, 1),
                d(2026, 6, 4), d(2026, 9, 7), d(2026, 10, 12), d(2026, 11, 2), d(2026, 11, 15), d(2026, 11, 20),
                d(2026, 12, 25)),
                Observed.datesIn("BR", 2026));
    }

    @Test
    void federal2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 26), d(2027, 4, 21), d(2027, 5, 1),
                d(2027, 5, 27), d(2027, 9, 7), d(2027, 10, 12), d(2027, 11, 2), d(2027, 11, 15), d(2027, 11, 20),
                d(2027, 12, 25)),
                Observed.datesIn("BR", 2027));
    }

    @Test
    void optionalDaysOffStayBusinessDays() {
        var brazil = Observed.calendar("BR");

        assertEquals(HolidayType.OBSERVANCE, brazil.holidayOn(d(2026, 2, 17)).orElseThrow().type());
        assertTrue(brazil.isBusinessDay(d(2026, 2, 17)));
        assertTrue(brazil.isBusinessDay(d(2026, 6, 4)));
        assertFalse(brazil.isBusinessDay(d(2026, 4, 21)));
    }

    @Test
    void blackAwarenessDayIsNationalFrom2024() {
        var brazil = Observed.calendar("BR");

        assertFalse(brazil.isHoliday(d(2023, 11, 20)));
        assertTrue(brazil.isHoliday(d(2024, 11, 20)));
    }
}
