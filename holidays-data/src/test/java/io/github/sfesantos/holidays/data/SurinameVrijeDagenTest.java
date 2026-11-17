package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Suriname's fifteen national days off under the Besluit Vrije Dagen 1971
 * (G.B. 1971 no. 78) as last amended by S.B. 2021 no. 27. The decree has
 * no substitute day: a holiday on a Saturday or Sunday stays there.
 *
 * <p>Chinees Nieuwjaar, Holi-dag, Divali and the two Ied days are set each
 * year by a notice of the Ministerie van Binnenlandse Zaken. The 2025 and
 * 2026 dates are those notices as the press reported them; Divali 2026 is
 * from published calendars. For 2027 nothing is announced yet, so the list
 * has no Chinees Nieuwjaar, Holi-dag or Divali, and the Ied days are the
 * Umm al-Qura dates, which a notice may move by a day.
 */
class SurinameVrijeDagenTest {

    @Test
    void decree2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 29), d(2025, 3, 14), d(2025, 3, 31), d(2025, 4, 18), d(2025, 4, 21),
                d(2025, 5, 1), d(2025, 6, 6), d(2025, 6, 20), d(2025, 7, 1), d(2025, 8, 9), d(2025, 10, 10),
                d(2025, 10, 20), d(2025, 11, 25), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("SR", 2025));
    }

    @Test
    void decree2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 17), d(2026, 3, 3), d(2026, 3, 20), d(2026, 4, 3), d(2026, 4, 6),
                d(2026, 5, 1), d(2026, 5, 27), d(2026, 7, 1), d(2026, 8, 9), d(2026, 10, 10), d(2026, 11, 8),
                d(2026, 11, 25), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("SR", 2026));
    }

    @Test
    void decree2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 9), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 16),
                d(2027, 7, 1), d(2027, 8, 9), d(2027, 10, 10), d(2027, 11, 25), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("SR", 2027));
    }

    @Test
    void iedUlFitre2025FollowsTheNoticeNotTheTables() {
        var suriname = Observed.calendar("SR");

        assertFalse(suriname.isHoliday(d(2025, 3, 30)));
        assertEquals("Ied Ul Fitre", suriname.holidayOn(d(2025, 3, 31)).orElseThrow().name());
        assertEquals("Ied Ul Fitre", suriname.holidayOn(d(2026, 3, 20)).orElseThrow().name());
    }

    @Test
    void sundayHolidayGivesNoMondayOff() {
        var suriname = Observed.calendar("SR");

        assertTrue(suriname.isHoliday(d(2026, 8, 9)));
        assertTrue(suriname.isBusinessDay(d(2026, 8, 10)));
    }

    @Test
    void recentDaysStartInTheirYear() {
        var suriname = Observed.calendar("SR");

        assertFalse(suriname.isHoliday(d(2006, 8, 9)));
        assertTrue(suriname.isHoliday(d(2007, 8, 9)));
        assertFalse(suriname.isHoliday(d(2011, 10, 10)));
        assertTrue(suriname.isHoliday(d(2012, 10, 10)));
    }
}
