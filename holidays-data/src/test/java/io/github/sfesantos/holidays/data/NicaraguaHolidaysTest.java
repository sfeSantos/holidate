package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Nicaragua's national holidays under Código del Trabajo (Ley 185) art. 66,
 * as amended by Ley 1118 (30 May, from 2022) and Ley 1272, in force since 20
 * January 2026, which added 18 January, 2 February, 21 February and 8
 * November. The dates are derived from the law; 18 January 2026 came before
 * the law and is not a holiday. Nothing moves when a holiday falls on a
 * weekend.
 */
class NicaraguaHolidaysTest {

    @Test
    void labourCode2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1), d(2025, 5, 30), d(2025, 7, 19),
                d(2025, 9, 14), d(2025, 9, 15), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("NI", 2025));
    }

    @Test
    void labourCode2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 2), d(2026, 2, 21), d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1),
                d(2026, 5, 30), d(2026, 7, 19), d(2026, 9, 14), d(2026, 9, 15), d(2026, 11, 8), d(2026, 12, 8),
                d(2026, 12, 25)),
                Observed.datesIn("NI", 2026));
    }

    @Test
    void labourCode2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 18), d(2027, 2, 2), d(2027, 2, 21), d(2027, 3, 25), d(2027, 3, 26),
                d(2027, 5, 1), d(2027, 5, 30), d(2027, 7, 19), d(2027, 9, 14), d(2027, 9, 15), d(2027, 11, 8),
                d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("NI", 2027));
    }

    @Test
    void holidaysOfLey1272StartIn2026() {
        var nicaragua = Observed.calendar("NI");

        assertFalse(nicaragua.isHoliday(d(2025, 2, 21)));
        assertFalse(nicaragua.isHoliday(d(2025, 11, 8)));
        assertFalse(nicaragua.isHoliday(d(2026, 1, 18)));
        assertTrue(nicaragua.isHoliday(d(2026, 2, 2)));
        assertTrue(nicaragua.isHoliday(d(2027, 1, 18)));
    }

    @Test
    void mothersDayIsAHolidayFrom2022() {
        var nicaragua = Observed.calendar("NI");

        assertFalse(nicaragua.isHoliday(d(2021, 5, 30)));
        assertTrue(nicaragua.isHoliday(d(2022, 5, 30)));
    }
}
