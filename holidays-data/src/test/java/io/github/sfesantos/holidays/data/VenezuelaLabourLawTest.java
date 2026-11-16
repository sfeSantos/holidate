package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Venezuela's national holidays under article 184 of the Ley Orgánica del
 * Trabajo, los Trabajadores y las Trabajadoras (Gaceta Oficial
 * Extraordinario 6.076 of 2012) and article 1 of the Ley de Fiestas
 * Nacionales (Gaceta Oficial 29.541 of 1971). No yearly list of them is
 * published, so the dates are derived from the two laws. Nothing moves:
 * a holiday on a Saturday or Sunday stays there.
 *
 * <p>The lists also hold the days declared non-working for the whole
 * country: 19 and 20 October 2025 (Decreto 5.170, Gaceta Oficial
 * Extraordinario 6.933) and 18 March 2026. The bank holidays of the
 * Sudeban calendar are not part of the pack.
 */
class VenezuelaLabourLawTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 3), d(2025, 3, 4), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 19),
                d(2025, 5, 1), d(2025, 6, 24), d(2025, 7, 5), d(2025, 7, 24), d(2025, 10, 12), d(2025, 10, 19),
                d(2025, 10, 20), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 31)),
                Observed.datesIn("VE", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 2, 17), d(2026, 3, 18), d(2026, 4, 2), d(2026, 4, 3),
                d(2026, 4, 19), d(2026, 5, 1), d(2026, 6, 24), d(2026, 7, 5), d(2026, 7, 24), d(2026, 10, 12),
                d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 31)),
                Observed.datesIn("VE", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 25), d(2027, 3, 26), d(2027, 4, 19),
                d(2027, 5, 1), d(2027, 6, 24), d(2027, 7, 5), d(2027, 7, 24), d(2027, 10, 12), d(2027, 12, 24),
                d(2027, 12, 25), d(2027, 12, 31)),
                Observed.datesIn("VE", 2027));
    }

    @Test
    void sundayHolidayGivesNoMondayOff() {
        var venezuela = Observed.calendar("VE");

        assertTrue(venezuela.isHoliday(d(2026, 7, 5)));
        assertTrue(venezuela.isBusinessDay(d(2026, 7, 6)));
    }

    @Test
    void bankHolidaysAreBusinessDays() {
        var venezuela = Observed.calendar("VE");

        assertFalse(venezuela.isHoliday(d(2026, 1, 12)));
        assertFalse(venezuela.isHoliday(d(2026, 6, 29)));
    }
}
