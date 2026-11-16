package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Finland's public holidays as the University of Helsinki Almanac Office
 * lists them: the church holidays of kirkkolaki plus vappu and
 * itsenäisyyspäivä. Juhannuspäivä is the Saturday from 20 to 26 June and
 * pyhäinpäivä the Saturday from 31 October to 6 November. Juhannusaatto and
 * jouluaatto are days off under most collective agreements, not by law, and
 * are kept as {@code OBSERVANCE}.
 */
class FinlandHolidaysTest {

    @Test
    void almanac2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 5, 1),
                d(2025, 5, 29), d(2025, 6, 8), d(2025, 6, 20), d(2025, 6, 21), d(2025, 11, 1), d(2025, 12, 6),
                d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("FI", 2025));
    }

    @Test
    void almanac2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 5, 1),
                d(2026, 5, 14), d(2026, 5, 24), d(2026, 6, 19), d(2026, 6, 20), d(2026, 10, 31), d(2026, 12, 6),
                d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("FI", 2026));
    }

    @Test
    void almanac2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 5, 1),
                d(2027, 5, 6), d(2027, 5, 16), d(2027, 6, 25), d(2027, 6, 26), d(2027, 11, 6), d(2027, 12, 6),
                d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("FI", 2027));
    }

    @Test
    void evesStayBusinessDays() {
        var finland = Observed.calendar("FI");

        assertEquals(HolidayType.OBSERVANCE, finland.holidayOn(d(2026, 6, 19)).orElseThrow().type());
        assertTrue(finland.isBusinessDay(d(2026, 6, 19)));
        assertTrue(finland.isBusinessDay(d(2026, 12, 24)));
        assertFalse(finland.isBusinessDay(d(2026, 12, 25)));
    }

    @Test
    void midsummerAndAllSaintsStayInsideTheirWindows() {
        var finland = Observed.calendar("FI");

        assertTrue(finland.isHoliday(d(2020, 6, 20)));
        assertTrue(finland.isHoliday(d(2021, 6, 26)));
        assertTrue(finland.isHoliday(d(2020, 10, 31)));
        assertTrue(finland.isHoliday(d(2021, 11, 6)));
    }
}
