package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Costa Rica's holidays under Código de Trabajo art. 148, which includes 31
 * August (Ley 10050) and 1 December (in place of 12 October, from 2020).
 * Ley 9875 moved some holidays to a Monday from 2020 to 2024 only. The
 * Ministerio de Trabajo y Seguridad Social stated that no holiday moves in
 * 2025 or in 2026 (comunicado CP-017-2026 for 31 August), and no law moves
 * any in 2027, so every holiday stays on its date. 2 August, 31 August and 1
 * December are holidays whose pay is not mandatory.
 */
class CostaRicaHolidaysTest {

    @Test
    void mtss2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 11), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1), d(2025, 7, 25),
                d(2025, 8, 2), d(2025, 8, 15), d(2025, 8, 31), d(2025, 9, 15), d(2025, 12, 1), d(2025, 12, 25)),
                Observed.datesIn("CR", 2025));
    }

    @Test
    void mtss2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 11), d(2026, 5, 1), d(2026, 7, 25),
                d(2026, 8, 2), d(2026, 8, 15), d(2026, 8, 31), d(2026, 9, 15), d(2026, 12, 1), d(2026, 12, 25)),
                Observed.datesIn("CR", 2026));
    }

    @Test
    void mtss2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 4, 11), d(2027, 5, 1), d(2027, 7, 25),
                d(2027, 8, 2), d(2027, 8, 15), d(2027, 8, 31), d(2027, 9, 15), d(2027, 12, 1), d(2027, 12, 25)),
                Observed.datesIn("CR", 2027));
    }

    @Test
    void holidayOnASundayIsNotMoved() {
        var costaRica = Observed.calendar("CR");

        assertTrue(costaRica.isHoliday(d(2027, 4, 11)));
        assertTrue(costaRica.isBusinessDay(d(2027, 4, 12)));
    }

    @Test
    void abolitionOfTheArmyDayStartsIn2020() {
        var costaRica = Observed.calendar("CR");

        assertFalse(costaRica.isHoliday(d(2019, 12, 1)));
        assertTrue(costaRica.isHoliday(d(2020, 12, 1)));
        assertFalse(costaRica.isHoliday(d(2026, 10, 12)));
    }
}
