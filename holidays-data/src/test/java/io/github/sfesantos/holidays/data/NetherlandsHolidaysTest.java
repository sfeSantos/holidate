package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * The Netherlands as the Rijksoverheid lists its official holidays for 2026
 * and 2027 ("Wanneer zijn de officiële feestdagen in Nederland?"), backed by
 * Article 3 of the Algemene termijnenwet; 2025 is derived from the same
 * rules. Koningsdag is 27 April, or Saturday 26 April when the 27th is a
 * Sunday, as in 2025. The JSON format has no rule for that, so the pack has
 * a one-off for 2025 and a fixed 27 April from 2026, which is a day late in
 * 2031 and 2036. Goede Vrijdag is only equated with a holiday by Article
 * 3(2) and is a normal working day for most people, so it is an
 * {@code OBSERVANCE}. Bevrijdingsdag is named in Article 3(1) and is kept
 * {@code PUBLIC}, although whether it is a day off depends on the contract.
 */
class NetherlandsHolidaysTest {

    @Test
    void rijksoverheid2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 4, 26), d(2025, 5, 5),
                d(2025, 5, 29), d(2025, 6, 8), d(2025, 6, 9), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("NL", 2025));
    }

    @Test
    void rijksoverheid2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 4, 27), d(2026, 5, 5),
                d(2026, 5, 14), d(2026, 5, 24), d(2026, 5, 25), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("NL", 2026));
    }

    @Test
    void rijksoverheid2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 4, 27), d(2027, 5, 5),
                d(2027, 5, 6), d(2027, 5, 16), d(2027, 5, 17), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("NL", 2027));
    }

    @Test
    void goodFridayStaysABusinessDay() {
        var netherlands = Observed.calendar("NL");

        assertEquals(HolidayType.OBSERVANCE, netherlands.holidayOn(d(2026, 4, 3)).orElseThrow().type());
        assertTrue(netherlands.isBusinessDay(d(2026, 4, 3)));
        assertFalse(netherlands.isBusinessDay(d(2026, 5, 5)));
    }

    @Test
    void koningsdagIsSaturday26AprilIn2025AndMonday27AprilIn2026() {
        var netherlands = Observed.calendar("NL");

        assertEquals(DayOfWeek.SATURDAY, d(2025, 4, 26).getDayOfWeek());
        assertTrue(netherlands.isHoliday(d(2025, 4, 26)));
        assertFalse(netherlands.isHoliday(d(2025, 4, 27)));
        assertEquals(DayOfWeek.MONDAY, d(2026, 4, 27).getDayOfWeek());
        assertEquals("Koningsdag", netherlands.holidayOn(d(2026, 4, 27)).orElseThrow().name());
        assertFalse(netherlands.isBusinessDay(d(2026, 4, 27)));
    }
}
