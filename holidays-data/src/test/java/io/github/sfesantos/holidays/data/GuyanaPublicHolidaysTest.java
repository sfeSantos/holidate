package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Guyana's public holidays under the Public Holidays Act, Cap. 19:07, as
 * the Ministry of Home Affairs notifies them: its "National Holidays" list
 * for 2025, 2026 and 2027 and the Minister's notice for each day, which
 * wins where the two differ (Eid-ul-Adha 2025, Youman Nabi 2026). A
 * holiday on a Sunday gives the Monday; one on a Saturday stays.
 *
 * <p>The 2027 list marks Eid-ul-Adha and Youman Nabi as tentative and gives
 * no substitute days, so the Mondays of 2027 follow from the Act. Monday
 * 1 September 2025 was a holiday for the general and regional elections.
 */
class GuyanaPublicHolidaysTest {

    @Test
    void notified2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 24), d(2025, 3, 14), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1),
                d(2025, 5, 5), d(2025, 5, 26), d(2025, 6, 7), d(2025, 7, 7), d(2025, 8, 1), d(2025, 9, 1),
                d(2025, 9, 5), d(2025, 10, 20), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("GY", 2025));
    }

    @Test
    void notified2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 23), d(2026, 3, 3), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 1),
                d(2026, 5, 5), d(2026, 5, 26), d(2026, 5, 27), d(2026, 7, 6), d(2026, 8, 1), d(2026, 8, 25),
                d(2026, 11, 9), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("GY", 2026));
    }

    @Test
    void notified2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 23), d(2027, 3, 22), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1),
                d(2027, 5, 5), d(2027, 5, 17), d(2027, 5, 26), d(2027, 7, 5), d(2027, 8, 2), d(2027, 8, 16),
                d(2027, 10, 28), d(2027, 12, 25), d(2027, 12, 27)),
                Observed.datesIn("GY", 2027));
    }

    @Test
    void sundayHolidayGivesTheMonday() {
        var guyana = Observed.calendar("GY");
        var republicDay = guyana.holidayOn(d(2025, 2, 24)).orElseThrow();
        var deepavali = guyana.holidayOn(d(2026, 11, 9)).orElseThrow();

        assertEquals(d(2025, 2, 23), republicDay.original());
        assertEquals(d(2026, 11, 8), deepavali.original());
    }

    @Test
    void saturdayHolidayGivesNoWeekdayOff() {
        var guyana = Observed.calendar("GY");

        assertTrue(guyana.isHoliday(d(2026, 8, 1)));
        assertTrue(guyana.isBusinessDay(d(2026, 7, 31)));
        assertTrue(guyana.isBusinessDay(d(2026, 8, 3)));
    }

    @Test
    void noticeWinsOverTheLunarTables() {
        var guyana = Observed.calendar("GY");

        assertFalse(guyana.isHoliday(d(2025, 6, 6)));
        assertTrue(guyana.isHoliday(d(2025, 6, 7)));
        assertFalse(guyana.isHoliday(d(2025, 9, 4)));
        assertTrue(guyana.isHoliday(d(2025, 9, 5)));
    }
}
