package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Latvia's public holidays under the law "Par svētku, atceres un
 * atzīmējamām dienām", article 1. No yearly list is published, so the dates
 * are derived from the law. When 4 May or 18 November falls on a Saturday or
 * a Sunday, it stays a holiday and the next working day is a day off. The
 * same holds for the closing day of the Song and Dance Festival, a holiday
 * since the amendment of 2 October 2014. It is held about every five years
 * and the Cabinet sets the dates each time: 8 July 2018, 9 July 2023 (order
 * 92 of 2022) and 9 July 2028 (order 208 of 2026), all Sundays.
 */
class LatviaHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 4),
                d(2025, 5, 5), d(2025, 5, 11), d(2025, 6, 8), d(2025, 6, 23), d(2025, 6, 24), d(2025, 11, 18),
                d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26), d(2025, 12, 31)),
                Observed.datesIn("LV", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 4),
                d(2026, 5, 10), d(2026, 5, 24), d(2026, 6, 23), d(2026, 6, 24), d(2026, 11, 18), d(2026, 12, 24),
                d(2026, 12, 25), d(2026, 12, 26), d(2026, 12, 31)),
                Observed.datesIn("LV", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 4),
                d(2027, 5, 9), d(2027, 5, 16), d(2027, 6, 23), d(2027, 6, 24), d(2027, 11, 18), d(2027, 12, 24),
                d(2027, 12, 25), d(2027, 12, 26), d(2027, 12, 31)),
                Observed.datesIn("LV", 2027));
    }

    @Test
    void independenceDaysOnAWeekendStayAndGiveTheNextMonday() {
        var latvia = Observed.calendar("LV");
        var name = "Latvijas Republikas Proklamēšanas diena";

        assertTrue(latvia.isHoliday(d(2025, 5, 4)));
        assertEquals(name, latvia.holidayOn(d(2028, 11, 18)).orElseThrow().name());
        assertEquals(name, latvia.holidayOn(d(2028, 11, 20)).orElseThrow().name());

        assertFalse(latvia.isBusinessDay(d(2025, 5, 5)));
        assertTrue(latvia.isBusinessDay(d(2025, 5, 6)));
        assertFalse(latvia.isBusinessDay(d(2028, 11, 20)));
        assertTrue(latvia.isBusinessDay(d(2026, 5, 5)));
    }

    @Test
    void otherHolidaysOnAWeekendDoNotMove() {
        var latvia = Observed.calendar("LV");

        assertTrue(latvia.isHoliday(d(2027, 5, 1)));
        assertTrue(latvia.isBusinessDay(d(2027, 5, 3)));
        assertTrue(latvia.isBusinessDay(d(2027, 12, 27)));
    }

    @Test
    void songAndDanceFestivalClosingDayGivesTheMondayOff() {
        var latvia = Observed.calendar("LV");
        var name = "Vispārējo latviešu Dziesmu un deju svētku noslēguma diena";

        for (var monday : List.of(d(2018, 7, 9), d(2023, 7, 10), d(2028, 7, 10))) {
            var closingDay = latvia.holidayOn(monday).orElseThrow();

            assertEquals(name, closingDay.name());
            assertEquals(monday.minusDays(1), closingDay.original());
            assertEquals(name, latvia.holidayOn(closingDay.original()).orElseThrow().name());
        }

        assertTrue(latvia.holidaysIn(Year.of(2026)).stream().noneMatch(holiday -> holiday.name().equals(name)));
    }
}
