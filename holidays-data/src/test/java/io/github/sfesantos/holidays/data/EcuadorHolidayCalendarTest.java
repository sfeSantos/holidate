package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Ecuador's national days of rest under article 65 of the Código del Trabajo
 * as reformed by the Ley Orgánica Reformatoria of 2016 (Registro Oficial
 * Suplemento 906), with the dates of the government's "Calendario de
 * Feriados Nacionales 2025 - 2030". A holiday on Tuesday is taken on the
 * Monday before, one on Wednesday or Thursday on the Friday after, Saturday
 * on Friday and Sunday on Monday; 1 January, 25 December and Carnival
 * Tuesday never move, and 2 and 3 November move as a pair.
 *
 * <p>The lists also hold the days on which the President suspended work
 * nationwide without recovery: Decretos Ejecutivos 474 and 172 for 2025,
 * and 249, 354, 431 and 507 for 2026.
 */
class EcuadorHolidayCalendarTest {

    @Test
    void calendar2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 2), d(2025, 1, 3), d(2025, 3, 3), d(2025, 3, 4), d(2025, 4, 18),
                d(2025, 5, 2), d(2025, 5, 23), d(2025, 8, 11), d(2025, 10, 9), d(2025, 10, 10), d(2025, 11, 3),
                d(2025, 11, 4), d(2025, 12, 25)),
                Observed.datesIn("EC", 2025));
    }

    @Test
    void calendar2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 2), d(2026, 2, 16), d(2026, 2, 17), d(2026, 4, 3), d(2026, 4, 30),
                d(2026, 5, 1), d(2026, 5, 25), d(2026, 6, 26), d(2026, 8, 10), d(2026, 10, 9), d(2026, 11, 2),
                d(2026, 11, 3), d(2026, 11, 20), d(2026, 12, 25)),
                Observed.datesIn("EC", 2026));
    }

    @Test
    void calendar2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 26), d(2027, 4, 30), d(2027, 5, 24),
                d(2027, 8, 9), d(2027, 10, 8), d(2027, 11, 1), d(2027, 11, 2), d(2027, 12, 25)),
                Observed.datesIn("EC", 2027));
    }

    @Test
    void midweekHolidayMovesToFridayOrMonday() {
        var ecuador = Observed.calendar("EC");

        assertFalse(ecuador.isHoliday(d(2025, 5, 1)));
        assertTrue(ecuador.isHoliday(d(2025, 5, 2)));
        assertFalse(ecuador.isHoliday(d(2027, 8, 10)));
        assertTrue(ecuador.isHoliday(d(2027, 8, 9)));
    }

    @Test
    void novemberPairMovesTogether() {
        var ecuador = Observed.calendar("EC");

        assertFalse(ecuador.isHoliday(d(2025, 11, 2)));
        assertEquals("Día de los Difuntos", ecuador.holidayOn(d(2025, 11, 4)).orElseThrow().name());
        assertEquals("Independencia de Cuenca", ecuador.holidayOn(d(2027, 11, 1)).orElseThrow().name());
        assertFalse(ecuador.isHoliday(d(2027, 11, 3)));
    }

    @Test
    void weekendHolidayMovesToTheNearestWeekday() {
        var ecuador = Observed.calendar("EC");
        var pichincha = ecuador.holidayOn(d(2026, 5, 25)).orElseThrow();

        assertEquals(d(2026, 5, 24), pichincha.original());
        assertTrue(ecuador.isHoliday(d(2027, 4, 30)));
        assertFalse(ecuador.isBusinessDay(d(2027, 10, 8)));
    }

    @Test
    void christmasStaysOnASaturday() {
        var ecuador = Observed.calendar("EC");

        assertTrue(ecuador.isHoliday(d(2027, 12, 25)));
        assertTrue(ecuador.isBusinessDay(d(2027, 12, 24)));
    }
}
