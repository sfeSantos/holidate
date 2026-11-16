package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Iceland's public holidays under lög nr. 88/1971 um 40 stunda vinnuviku,
 * article 6: the holy days of the national church (lög nr. 32/1997, article
 * 2), sumardagurinn fyrsti, 1 May, 17 June, and aðfangadagur and gamlársdagur
 * from 13:00, plus the first Monday of August since 1983. The dates are
 * derived from the acts. Sumardagurinn fyrsti is the first Thursday after 18
 * April.
 */
class IcelandHolidaysTest {

    @Test
    void law2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 17), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21), d(2025, 4, 24),
                d(2025, 5, 1), d(2025, 5, 29), d(2025, 6, 8), d(2025, 6, 9), d(2025, 6, 17), d(2025, 8, 4),
                d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26), d(2025, 12, 31)),
                Observed.datesIn("IS", 2025));
    }

    @Test
    void law2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 2), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6), d(2026, 4, 23),
                d(2026, 5, 1), d(2026, 5, 14), d(2026, 5, 24), d(2026, 5, 25), d(2026, 6, 17), d(2026, 8, 3),
                d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26), d(2026, 12, 31)),
                Observed.datesIn("IS", 2026));
    }

    @Test
    void law2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29), d(2027, 4, 22),
                d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 16), d(2027, 5, 17), d(2027, 6, 17), d(2027, 8, 2),
                d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26), d(2027, 12, 31)),
                Observed.datesIn("IS", 2027));
    }

    @Test
    void christmasEveAndNewYearsEveAreHalfDays() {
        var iceland = Observed.calendar("IS");

        assertEquals(HolidayType.HALF_DAY, iceland.holidayOn(d(2026, 12, 24)).orElseThrow().type());
        assertEquals(HolidayType.HALF_DAY, iceland.holidayOn(d(2026, 12, 31)).orElseThrow().type());
        assertEquals(HolidayType.PUBLIC, iceland.holidayOn(d(2026, 12, 25)).orElseThrow().type());
    }

    @Test
    void firstDayOfSummerIsTheThursdayFrom19To25April() {
        var iceland = Observed.calendar("IS");

        assertTrue(iceland.isHoliday(d(2018, 4, 19)));
        assertTrue(iceland.isHoliday(d(2019, 4, 25)));
        assertFalse(iceland.isHoliday(d(2018, 4, 26)));
    }
}
