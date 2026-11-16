package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * El Salvador's paid holidays under Código de Trabajo art. 190, which
 * includes 10 May and 17 June, with the dates derived from that list. The
 * code moves nothing: a holiday on a weekend stays there. 3 and 5 August are
 * holidays in San Salvador only and are not in the national layer.
 */
class ElSalvadorHolidaysTest {

    @Test
    void labourCode2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 19), d(2025, 5, 1), d(2025, 5, 10),
                d(2025, 6, 17), d(2025, 8, 6), d(2025, 9, 15), d(2025, 11, 2), d(2025, 12, 25)),
                Observed.datesIn("SV", 2025));
    }

    @Test
    void labourCode2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 4), d(2026, 5, 1), d(2026, 5, 10),
                d(2026, 6, 17), d(2026, 8, 6), d(2026, 9, 15), d(2026, 11, 2), d(2026, 12, 25)),
                Observed.datesIn("SV", 2026));
    }

    @Test
    void labourCode2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 3, 27), d(2027, 5, 1), d(2027, 5, 10),
                d(2027, 6, 17), d(2027, 8, 6), d(2027, 9, 15), d(2027, 11, 2), d(2027, 12, 25)),
                Observed.datesIn("SV", 2027));
    }

    @Test
    void mothersDayOnASundayIsNotMoved() {
        var elSalvador = Observed.calendar("SV");

        assertTrue(elSalvador.isHoliday(d(2026, 5, 10)));
        assertTrue(elSalvador.isBusinessDay(d(2026, 5, 11)));
    }

    @Test
    void sanSalvadorFestivitiesAreNotNational() {
        var elSalvador = Observed.calendar("SV");

        assertFalse(elSalvador.isHoliday(d(2026, 8, 3)));
        assertFalse(elSalvador.isHoliday(d(2026, 8, 5)));
        assertTrue(elSalvador.isHoliday(d(2026, 8, 6)));
    }
}
