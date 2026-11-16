package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Uruguay's national holidays under Decreto-Ley 14.977, as IMPO prints it.
 * Article 18 of Ley 12.590 makes five of them paid days off ("feriados no
 * laborables"): 1 January, 1 May, 18 July, 25 August and 25 December. The
 * rest are ordinary working holidays, kept as {@code OBSERVANCE}. Uruguay
 * publishes no yearly list, so the dates are derived from the law. Semana de
 * Turismo is the whole week by law; the pack lists Monday to Friday. 1 March
 * 2025 is the presidential handover, a paid day off under Decreto 120/985.
 *
 * <p>Ley 16.805 moves 19 April, 18 May and 12 October to a Monday from
 * Tuesday to Friday and leaves them alone from Saturday to Monday, which no
 * rule gives. For 18 May and 12 October the pack uses the Monday of a
 * seven-day window from 2026 and pins 2025, when both fall on a Sunday, as
 * dated one-offs. 19 April stays on its date, which is right for 2025 to
 * 2027.
 */
class UruguayHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 3, 1), d(2025, 3, 3), d(2025, 3, 4), d(2025, 4, 14),
                d(2025, 4, 15), d(2025, 4, 16), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 19), d(2025, 5, 1),
                d(2025, 5, 18), d(2025, 6, 19), d(2025, 7, 18), d(2025, 8, 25), d(2025, 10, 12), d(2025, 11, 2),
                d(2025, 12, 25)),
                Observed.datesIn("UY", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 2, 16), d(2026, 2, 17), d(2026, 3, 30), d(2026, 3, 31),
                d(2026, 4, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 19), d(2026, 5, 1), d(2026, 5, 18),
                d(2026, 6, 19), d(2026, 7, 18), d(2026, 8, 25), d(2026, 10, 12), d(2026, 11, 2), d(2026, 12, 25)),
                Observed.datesIn("UY", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 2, 8), d(2027, 2, 9), d(2027, 3, 22), d(2027, 3, 23),
                d(2027, 3, 24), d(2027, 3, 25), d(2027, 3, 26), d(2027, 4, 19), d(2027, 5, 1), d(2027, 5, 17),
                d(2027, 6, 19), d(2027, 7, 18), d(2027, 8, 25), d(2027, 10, 11), d(2027, 11, 2), d(2027, 12, 25)),
                Observed.datesIn("UY", 2027));
    }

    @Test
    void batallaDeLasPiedrasIsPinnedIn2025AndFollowsTheRuleFrom2026() {
        var uruguay = Observed.calendar("UY");

        assertTrue(uruguay.isHoliday(d(2025, 5, 18)));
        assertFalse(uruguay.isHoliday(d(2025, 5, 19)));
        assertTrue(uruguay.isHoliday(d(2026, 5, 18)));
        assertEquals(HolidayType.OBSERVANCE, uruguay.holidayOn(d(2027, 5, 17)).orElseThrow().type());
        assertFalse(uruguay.isHoliday(d(2027, 5, 18)));
        assertTrue(uruguay.isHoliday(d(2028, 5, 22)));
    }

    @Test
    void workingHolidaysStayBusinessDays() {
        var uruguay = Observed.calendar("UY");

        assertEquals(HolidayType.OBSERVANCE, uruguay.holidayOn(d(2026, 1, 6)).orElseThrow().type());
        assertTrue(uruguay.isBusinessDay(d(2026, 1, 6)));
        assertTrue(uruguay.isBusinessDay(d(2026, 2, 16)));
        assertTrue(uruguay.isBusinessDay(d(2026, 4, 3)));
        assertTrue(uruguay.isBusinessDay(d(2026, 6, 19)));
        assertTrue(uruguay.isBusinessDay(d(2026, 11, 2)));
    }

    @Test
    void theFivePaidHolidaysAreDaysOff() {
        var uruguay = Observed.calendar("UY");

        assertFalse(uruguay.isBusinessDay(d(2026, 1, 1)));
        assertFalse(uruguay.isBusinessDay(d(2026, 5, 1)));
        assertFalse(uruguay.isBusinessDay(d(2025, 7, 18)));
        assertFalse(uruguay.isBusinessDay(d(2026, 8, 25)));
        assertFalse(uruguay.isBusinessDay(d(2026, 12, 25)));
    }

    @Test
    void presidentialHandoverIsAHolidayIn2025Only() {
        var uruguay = Observed.calendar("UY");

        assertEquals(HolidayType.PUBLIC, uruguay.holidayOn(d(2025, 3, 1)).orElseThrow().type());
        assertFalse(uruguay.isHoliday(d(2026, 3, 1)));
        assertFalse(uruguay.isHoliday(d(2027, 3, 1)));
    }
}
