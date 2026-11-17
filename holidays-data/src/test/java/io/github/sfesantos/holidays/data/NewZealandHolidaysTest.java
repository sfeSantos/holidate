package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayOccurrence;

/**
 * New Zealand's national public holidays under the Holidays Act 2003,
 * sections 44 to 45A, with the dates Employment New Zealand publishes for
 * 2025, 2026 and 2027 (employment.govt.nz, "Public holidays and anniversary
 * dates"). A holiday on a Saturday or Sunday is listed on the weekday it
 * moves to for people who do not work weekends. Matariki has no rule: its
 * dates come from Schedule 1 of Te Kāhui o Matariki Public Holiday Act
 * 2022, which sets them up to 2052.
 */
class NewZealandHolidaysTest {

    @Test
    void employmentNewZealand2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 2), d(2025, 2, 6), d(2025, 4, 18), d(2025, 4, 21), d(2025, 4, 25),
                d(2025, 6, 2), d(2025, 6, 20), d(2025, 10, 27), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("NZ", 2025));
    }

    @Test
    void employmentNewZealand2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 2), d(2026, 2, 6), d(2026, 4, 3), d(2026, 4, 6), d(2026, 4, 27),
                d(2026, 6, 1), d(2026, 7, 10), d(2026, 10, 26), d(2026, 12, 25), d(2026, 12, 28)),
                Observed.datesIn("NZ", 2026));
    }

    @Test
    void employmentNewZealand2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 4), d(2027, 2, 8), d(2027, 3, 26), d(2027, 3, 29), d(2027, 4, 26),
                d(2027, 6, 7), d(2027, 6, 25), d(2027, 10, 25), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("NZ", 2027));
    }

    @Test
    void waitangiDayAndAnzacDayMoveToMonday() {
        var newZealand = Observed.calendar("NZ");

        assertEquals("Waitangi Day", newZealand.holidayOn(d(2027, 2, 8)).orElseThrow().name());
        assertEquals(d(2027, 2, 6), newZealand.holidayOn(d(2027, 2, 8)).orElseThrow().original());
        assertEquals("ANZAC Day", newZealand.holidayOn(d(2026, 4, 27)).orElseThrow().name());
        assertEquals("ANZAC Day", newZealand.holidayOn(d(2027, 4, 26)).orElseThrow().name());
    }

    @Test
    void christmasOnSaturdayTakesMondayAndBoxingDayTuesday() {
        var newZealand = Observed.calendar("NZ");

        assertEquals("Christmas Day", newZealand.holidayOn(d(2027, 12, 27)).orElseThrow().name());
        assertEquals("Boxing Day", newZealand.holidayOn(d(2027, 12, 28)).orElseThrow().name());
        assertTrue(newZealand.isBusinessDay(d(2027, 12, 29)));
    }

    @Test
    void newYearOnSaturdayMovesBothDays() {
        var newZealand = Observed.calendar("NZ");

        assertEquals(List.of(d(2028, 1, 3), d(2028, 1, 4)), Observed.datesIn("NZ", 2028).subList(0, 2));
        assertTrue(newZealand.isBusinessDay(d(2028, 1, 5)));
    }

    @Test
    void matarikiFollowsTheLegislatedDates() {
        var newZealand = Observed.calendar("NZ");

        assertFalse(newZealand.isHoliday(d(2021, 7, 2)));
        assertTrue(newZealand.isHoliday(d(2022, 6, 24)));
        assertTrue(newZealand.isHoliday(d(2028, 7, 14)));
        assertTrue(newZealand.isHoliday(d(2052, 6, 21)));
    }

    @Test
    void matarikiIsOneHolidayUnderOneNameInEveryLegislatedYear() {
        var newZealand = Observed.calendar("NZ");
        var layer = Observed.REGISTRY.layer("NZ").orElseThrow();
        var matariki = newZealand.holidaysBetween(d(2022, 1, 1), d(2053, 1, 1))
                .stream()
                .filter(holiday -> holiday.name().startsWith("Matariki"))
                .toList();

        assertEquals(1, layer.additions().stream().filter(holiday -> holiday.name().startsWith("Matariki")).count());
        assertEquals(31, matariki.size());
        assertEquals(List.of("Matariki"), matariki.stream().map(HolidayOccurrence::name).distinct().toList());
        assertEquals(IntStream.rangeClosed(2022, 2052).boxed().toList(),
                matariki.stream().map(holiday -> holiday.observed().getYear()).toList());
        assertEquals("Matariki", newZealand.holidayOn(d(2026, 7, 10)).orElseThrow().name());
        assertTrue(newZealand.holidaysIn(Year.of(2053)).stream().noneMatch(holiday -> holiday.name().equals("Matariki")));
    }
}
