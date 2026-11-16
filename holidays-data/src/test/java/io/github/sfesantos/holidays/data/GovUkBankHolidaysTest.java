package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The United Kingdom's bank holidays as GOV.UK publishes them at
 * gov.uk/bank-holidays for its three divisions: England and Wales, Scotland
 * and Northern Ireland. {@code GB-ENG} and {@code GB-WLS} are both pinned to
 * the England and Wales list. A bank holiday that falls on a Saturday or a
 * Sunday gets a substitute day, the next weekday that is not a bank holiday
 * itself. {@code GB} alone holds what the four nations share and is not a
 * list GOV.UK publishes. Scotland's list for 2026 carries the bank holiday
 * proclaimed for 15 June, the World Cup.
 */
class GovUkBankHolidaysTest {

    @Test
    void englandAndWales2025() {
        var published = List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 5), d(2025, 5, 26), d(2025, 8, 25),
                d(2025, 12, 25), d(2025, 12, 26));

        assertEquals(published, Observed.datesIn("GB-ENG", 2025));
        assertEquals(published, Observed.datesIn("GB-WLS", 2025));
    }

    @Test
    void englandAndWales2026() {
        var published = List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 4), d(2026, 5, 25), d(2026, 8, 31),
                d(2026, 12, 25), d(2026, 12, 28));

        assertEquals(published, Observed.datesIn("GB-ENG", 2026));
        assertEquals(published, Observed.datesIn("GB-WLS", 2026));
    }

    @Test
    void englandAndWales2027() {
        var published = List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 3), d(2027, 5, 31), d(2027, 8, 30),
                d(2027, 12, 27), d(2027, 12, 28));

        assertEquals(published, Observed.datesIn("GB-ENG", 2027));
        assertEquals(published, Observed.datesIn("GB-WLS", 2027));
    }

    @Test
    void scotland2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 2), d(2025, 4, 18), d(2025, 5, 5), d(2025, 5, 26), d(2025, 8, 4),
                d(2025, 12, 1), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("GB-SCT", 2025));
    }

    @Test
    void scotland2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 2), d(2026, 4, 3), d(2026, 5, 4), d(2026, 5, 25), d(2026, 6, 15),
                d(2026, 8, 3), d(2026, 11, 30), d(2026, 12, 25), d(2026, 12, 28)),
                Observed.datesIn("GB-SCT", 2026));
    }

    @Test
    void scotland2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 4), d(2027, 3, 26), d(2027, 5, 3), d(2027, 5, 31), d(2027, 8, 2),
                d(2027, 11, 30), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("GB-SCT", 2027));
    }

    @Test
    void northernIreland2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 17), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 5), d(2025, 5, 26),
                d(2025, 7, 14), d(2025, 8, 25), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("GB-NIR", 2025));
    }

    @Test
    void northernIreland2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 3, 17), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 4), d(2026, 5, 25),
                d(2026, 7, 13), d(2026, 8, 31), d(2026, 12, 25), d(2026, 12, 28)),
                Observed.datesIn("GB-NIR", 2026));
    }

    @Test
    void northernIreland2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 17), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 3), d(2027, 5, 31),
                d(2027, 7, 12), d(2027, 8, 30), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("GB-NIR", 2027));
    }

    @Test
    void sharedBase2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 5, 5), d(2025, 5, 26), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("GB", 2025));
    }

    @Test
    void sharedBase2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 5, 4), d(2026, 5, 25), d(2026, 12, 25), d(2026, 12, 28)),
                Observed.datesIn("GB", 2026));
    }

    @Test
    void sharedBase2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 5, 3), d(2027, 5, 31), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("GB", 2027));
    }

    @Test
    void boxingDayOnASaturdayMovesToMonday() {
        var england = Observed.calendar("GB-ENG");
        var boxingDay = england.holidayOn(d(2026, 12, 28)).orElseThrow();

        assertEquals("Boxing Day", boxingDay.name());
        assertEquals(d(2026, 12, 26), boxingDay.original());
        assertTrue(boxingDay.isMoved());
        assertFalse(england.isHoliday(d(2026, 12, 26)));
    }

    @Test
    void christmasAndBoxingDayOnAWeekendTakeMondayAndTuesday() {
        var england = Observed.calendar("GB-ENG");
        var christmas = england.holidayOn(d(2027, 12, 27)).orElseThrow();
        var boxingDay = england.holidayOn(d(2027, 12, 28)).orElseThrow();

        assertEquals("Christmas Day", christmas.name());
        assertTrue(christmas.isMoved());
        assertEquals("Boxing Day", boxingDay.name());
        assertTrue(boxingDay.isMoved());
        assertTrue(england.isBusinessDay(d(2027, 12, 29)));
    }

    @Test
    void scotlandHasTheSecondOfJanuaryAndNoEasterMonday() {
        var scotland = Observed.calendar("GB-SCT");
        var secondOfJanuary = scotland.holidayOn(d(2027, 1, 4)).orElseThrow();

        assertEquals("2nd January", secondOfJanuary.name());
        assertEquals(d(2027, 1, 2), secondOfJanuary.original());
        assertFalse(scotland.isHoliday(d(2026, 4, 6)));
        assertFalse(scotland.isHoliday(d(2027, 3, 29)));
    }

    @Test
    void scotlandsNewYearSubstitutesChain() {
        assertEquals(List.of(d(2028, 1, 3), d(2028, 1, 4)), Observed.datesIn("GB-SCT", 2028).subList(0, 2));
        assertEquals(List.of(d(2023, 1, 2), d(2023, 1, 3)), Observed.datesIn("GB-SCT", 2023).subList(0, 2));
    }

    @Test
    void stAndrewsDayOnASundayMovesToMonday() {
        var scotland = Observed.calendar("GB-SCT");

        assertTrue(scotland.holidayOn(d(2025, 12, 1)).orElseThrow().isMoved());
        assertFalse(scotland.isHoliday(d(2006, 11, 30)));
        assertTrue(scotland.isHoliday(d(2007, 11, 30)));
    }

    @Test
    void scotlandsSummerBankHolidayIsTheFirstMondayOfAugust() {
        assertTrue(Observed.calendar("GB-SCT").isHoliday(d(2026, 8, 3)));
        assertFalse(Observed.calendar("GB-SCT").isHoliday(d(2026, 8, 31)));
        assertTrue(Observed.calendar("GB-ENG").isHoliday(d(2026, 8, 31)));
    }

    @Test
    void theWorldCupBankHolidayIsScotlandIn2026Only() {
        assertTrue(Observed.calendar("GB-SCT").isHoliday(d(2026, 6, 15)));
        assertFalse(Observed.calendar("GB-SCT").isHoliday(d(2027, 6, 15)));
        assertFalse(Observed.calendar("GB-ENG").isHoliday(d(2026, 6, 15)));
    }

    @Test
    void northernIrelandSubstitutesTheTwelfthAndStPatricksDay() {
        var northernIreland = Observed.calendar("GB-NIR");

        assertEquals(d(2025, 7, 12), northernIreland.holidayOn(d(2025, 7, 14)).orElseThrow().original());
        assertEquals(d(2026, 7, 12), northernIreland.holidayOn(d(2026, 7, 13)).orElseThrow().original());
        assertEquals(d(2024, 3, 17), northernIreland.holidayOn(d(2024, 3, 18)).orElseThrow().original());
    }
}
