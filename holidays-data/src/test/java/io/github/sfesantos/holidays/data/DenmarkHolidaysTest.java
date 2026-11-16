package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Denmark's holidays. No single act lists the "helligdage"; lov nr. 214 of
 * 28 February 2023 (bill L 13, 2022-23) spells them out where it amends
 * lukkeloven § 2: nytårsdag, skærtorsdag, langfredag, 1. og 2. påskedag,
 * Kr. himmelfartsdag, 1. og 2. pinsedag, 1. og 2. juledag. The dates are
 * derived from that list and agree with the closing days Danmarks
 * Nationalbank publishes for 2026 and 2027. The same act ended store bededag
 * as a holiday from 1 January 2024. Grundlovsdag and juleaftensdag close the
 * shops under lukkeloven but are not helligdage, so they are kept as
 * {@code OBSERVANCE}.
 */
class DenmarkHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 29),
                d(2025, 6, 5), d(2025, 6, 8), d(2025, 6, 9), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("DK", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 14),
                d(2026, 5, 24), d(2026, 5, 25), d(2026, 6, 5), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("DK", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 6),
                d(2027, 5, 16), d(2027, 5, 17), d(2027, 6, 5), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("DK", 2027));
    }

    @Test
    void storeBededagEndsWith2023() {
        var denmark = Observed.calendar("DK");

        assertTrue(denmark.isHoliday(d(2023, 5, 5)));
        assertFalse(denmark.isHoliday(d(2024, 4, 26)));
        assertFalse(denmark.isHoliday(d(2026, 5, 1)));
    }

    @Test
    void shopClosingDaysStayBusinessDays() {
        var denmark = Observed.calendar("DK");

        assertEquals(HolidayType.OBSERVANCE, denmark.holidayOn(d(2026, 6, 5)).orElseThrow().type());
        assertTrue(denmark.isBusinessDay(d(2026, 6, 5)));
        assertTrue(denmark.isBusinessDay(d(2026, 12, 24)));
        assertFalse(denmark.isBusinessDay(d(2026, 12, 25)));
    }
}
