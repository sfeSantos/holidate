package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Canada's statutory holidays under each province's and territory's
 * employment standards law, with the dates the provincial lists give where
 * one is published (Alberta, British Columbia, Manitoba, Nova Scotia,
 * Saskatchewan, Yukon) and derived from the law elsewhere: Employment
 * Standards Code (AB, MB), Employment Standards Act (BC, NB, NT, ON, PE,
 * YT), Labour Standards Act (NL, section 14, and NU), Labour Standards Code
 * (NS), Loi sur les normes du travail, article 60, and Loi sur la fête
 * nationale (QC), Saskatchewan Employment Act (SK).
 *
 * <p>The national layer is not the federal list of the Canada Labour Code,
 * which binds federally regulated employers only. It holds the five days
 * that are a statutory day off in every province and territory. A holiday
 * stays on its date when it falls on a weekend, as the provincial lists
 * show it: the substitute day depends on each employee's schedule. The
 * exceptions are written into the law: Canada Day moves from Sunday to
 * Monday, and so do Quebec's fête nationale and Saskatchewan's New Year's
 * Day, Remembrance Day and Christmas Day.
 *
 * <p>Days an employer may choose to give are {@code OBSERVANCE}: Alberta's
 * optional general holidays, Quebec's Easter Monday (the law asks for Good
 * Friday or Easter Monday), and Remembrance Day in Manitoba and Nova
 * Scotia, which have their own Remembrance Day Acts outside the general
 * holiday list. Retail closing days and government office holidays are
 * left out.
 */
class CanadaProvincialHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(d(2025, 1, 1), d(2025, 4, 18), d(2025, 7, 1), d(2025, 9, 1), d(2025, 12, 25)),
                Observed.datesIn("CA", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(d(2026, 1, 1), d(2026, 4, 3), d(2026, 7, 1), d(2026, 9, 7), d(2026, 12, 25)),
                Observed.datesIn("CA", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(d(2027, 1, 1), d(2027, 3, 26), d(2027, 7, 1), d(2027, 9, 6), d(2027, 12, 25)),
                Observed.datesIn("CA", 2027));
    }

    @Test
    void alberta() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 19), d(2025, 7, 1),
                d(2025, 8, 4), d(2025, 9, 1), d(2025, 9, 30), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.datesIn("CA-AB", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 18), d(2026, 7, 1),
                d(2026, 8, 3), d(2026, 9, 7), d(2026, 9, 30), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25),
                d(2026, 12, 26)),
                Observed.datesIn("CA-AB", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 24), d(2027, 7, 1),
                d(2027, 8, 2), d(2027, 9, 6), d(2027, 9, 30), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25),
                d(2027, 12, 26)),
                Observed.datesIn("CA-AB", 2027));
    }

    @Test
    void britishColumbia() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 5, 19), d(2025, 7, 1), d(2025, 8, 4),
                d(2025, 9, 1), d(2025, 9, 30), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-BC", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 5, 18), d(2026, 7, 1), d(2026, 8, 3),
                d(2026, 9, 7), d(2026, 9, 30), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-BC", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 5, 24), d(2027, 7, 1), d(2027, 8, 2),
                d(2027, 9, 6), d(2027, 9, 30), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-BC", 2027));
    }

    @Test
    void manitoba() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 5, 19), d(2025, 7, 1), d(2025, 9, 1),
                d(2025, 9, 30), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-MB", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 5, 18), d(2026, 7, 1), d(2026, 9, 7),
                d(2026, 9, 30), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-MB", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 5, 24), d(2027, 7, 1), d(2027, 9, 6),
                d(2027, 9, 30), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-MB", 2027));
    }

    @Test
    void newBrunswick() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 7, 1), d(2025, 8, 4), d(2025, 9, 1),
                d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-NB", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 7, 1), d(2026, 8, 3), d(2026, 9, 7),
                d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-NB", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 7, 1), d(2027, 8, 2), d(2027, 9, 6),
                d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-NB", 2027));
    }

    @Test
    void newfoundlandAndLabrador() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 7, 1), d(2025, 9, 1), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-NL", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 7, 1), d(2026, 9, 7), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-NL", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 7, 1), d(2027, 9, 6), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-NL", 2027));
    }

    @Test
    void novaScotia() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 7, 1), d(2025, 9, 1), d(2025, 11, 11),
                d(2025, 12, 25)),
                Observed.datesIn("CA-NS", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 7, 1), d(2026, 9, 7), d(2026, 11, 11),
                d(2026, 12, 25)),
                Observed.datesIn("CA-NS", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 7, 1), d(2027, 9, 6), d(2027, 11, 11),
                d(2027, 12, 25)),
                Observed.datesIn("CA-NS", 2027));
    }

    @Test
    void northwestTerritories() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 5, 19), d(2025, 6, 21), d(2025, 7, 1), d(2025, 8, 4),
                d(2025, 9, 1), d(2025, 9, 30), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-NT", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 5, 18), d(2026, 6, 21), d(2026, 7, 1), d(2026, 8, 3),
                d(2026, 9, 7), d(2026, 9, 30), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-NT", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 5, 24), d(2027, 6, 21), d(2027, 7, 1), d(2027, 8, 2),
                d(2027, 9, 6), d(2027, 9, 30), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-NT", 2027));
    }

    @Test
    void nunavut() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 5, 19), d(2025, 7, 1), d(2025, 7, 9), d(2025, 8, 4),
                d(2025, 9, 1), d(2025, 9, 30), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-NU", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 5, 18), d(2026, 7, 1), d(2026, 7, 9), d(2026, 8, 3),
                d(2026, 9, 7), d(2026, 9, 30), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-NU", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 5, 24), d(2027, 7, 1), d(2027, 7, 9), d(2027, 8, 2),
                d(2027, 9, 6), d(2027, 9, 30), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-NU", 2027));
    }

    @Test
    void ontario() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 5, 19), d(2025, 7, 1), d(2025, 9, 1),
                d(2025, 10, 13), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("CA-ON", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 5, 18), d(2026, 7, 1), d(2026, 9, 7),
                d(2026, 10, 12), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("CA-ON", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 5, 24), d(2027, 7, 1), d(2027, 9, 6),
                d(2027, 10, 11), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("CA-ON", 2027));
    }

    @Test
    void princeEdwardIsland() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 7, 1), d(2025, 9, 1), d(2025, 9, 30),
                d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-PE", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 7, 1), d(2026, 9, 7), d(2026, 9, 30),
                d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-PE", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 7, 1), d(2027, 9, 6), d(2027, 9, 30),
                d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-PE", 2027));
    }

    @Test
    void quebec() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 19), d(2025, 6, 24), d(2025, 7, 1),
                d(2025, 9, 1), d(2025, 10, 13), d(2025, 12, 25)),
                Observed.datesIn("CA-QC", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 18), d(2026, 6, 24), d(2026, 7, 1),
                d(2026, 9, 7), d(2026, 10, 12), d(2026, 12, 25)),
                Observed.datesIn("CA-QC", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 24), d(2027, 6, 24), d(2027, 7, 1),
                d(2027, 9, 6), d(2027, 10, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-QC", 2027));
    }

    @Test
    void saskatchewan() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 2, 17), d(2025, 4, 18), d(2025, 5, 19), d(2025, 7, 1), d(2025, 8, 4),
                d(2025, 9, 1), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-SK", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 16), d(2026, 4, 3), d(2026, 5, 18), d(2026, 7, 1), d(2026, 8, 3),
                d(2026, 9, 7), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-SK", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 15), d(2027, 3, 26), d(2027, 5, 24), d(2027, 7, 1), d(2027, 8, 2),
                d(2027, 9, 6), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-SK", 2027));
    }

    @Test
    void yukon() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 18), d(2025, 5, 19), d(2025, 6, 21), d(2025, 7, 1), d(2025, 8, 18),
                d(2025, 9, 1), d(2025, 9, 30), d(2025, 10, 13), d(2025, 11, 11), d(2025, 12, 25)),
                Observed.datesIn("CA-YT", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 3), d(2026, 5, 18), d(2026, 6, 21), d(2026, 7, 1), d(2026, 8, 17),
                d(2026, 9, 7), d(2026, 9, 30), d(2026, 10, 12), d(2026, 11, 11), d(2026, 12, 25)),
                Observed.datesIn("CA-YT", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 26), d(2027, 5, 24), d(2027, 6, 21), d(2027, 7, 1), d(2027, 8, 16),
                d(2027, 9, 6), d(2027, 9, 30), d(2027, 10, 11), d(2027, 11, 11), d(2027, 12, 25)),
                Observed.datesIn("CA-YT", 2027));
    }

    @Test
    void victoriaDayIsTheMondayBefore25May() {
        var ontario = Observed.calendar("CA-ON");
        var quebec = Observed.calendar("CA-QC");

        assertEquals("Victoria Day", ontario.holidayOn(d(2026, 5, 18)).orElseThrow().name());
        assertFalse(ontario.isHoliday(d(2026, 5, 25)));
        assertEquals("Journée nationale des patriotes", quebec.holidayOn(d(2026, 5, 18)).orElseThrow().name());
    }

    @Test
    void canadaDayOnSundayMovesToMonday() {
        assertFalse(Observed.calendar("CA").isHoliday(d(2029, 7, 1)));
        assertEquals("Canada Day", Observed.calendar("CA").holidayOn(d(2029, 7, 2)).orElseThrow().name());
        assertEquals("Fête du Canada", Observed.calendar("CA-QC").holidayOn(d(2029, 7, 2)).orElseThrow().name());
        assertTrue(Observed.calendar("CA").isHoliday(d(2028, 7, 1)));
    }

    @Test
    void aSaturdayHolidayStaysOnSaturday() {
        var ontario = Observed.calendar("CA-ON");

        assertTrue(ontario.isHoliday(d(2027, 12, 25)));
        assertTrue(ontario.isHoliday(d(2027, 12, 26)));
        assertTrue(ontario.isBusinessDay(d(2027, 12, 27)));
    }

    @Test
    void quebecMovesTheFeteNationaleFromSundayToMonday() {
        var quebec = Observed.calendar("CA-QC");

        assertEquals("Fête nationale", quebec.holidayOn(d(2029, 6, 25)).orElseThrow().name());
        assertFalse(quebec.isHoliday(d(2029, 6, 24)));
    }

    @Test
    void saskatchewanMovesThreeHolidaysFromSundayToMonday() {
        var saskatchewan = Observed.calendar("CA-SK");

        assertEquals("Remembrance Day", saskatchewan.holidayOn(d(2029, 11, 12)).orElseThrow().name());
        assertEquals("Christmas Day", saskatchewan.holidayOn(d(2033, 12, 26)).orElseThrow().name());
        assertEquals("New Year's Day", saskatchewan.holidayOn(d(2034, 1, 2)).orElseThrow().name());
        assertTrue(Observed.calendar("CA-AB").isHoliday(d(2029, 11, 11)));
    }

    @Test
    void optionalDaysStayBusinessDays() {
        var alberta = Observed.calendar("CA-AB");

        assertEquals(HolidayType.OBSERVANCE, alberta.holidayOn(d(2026, 8, 3)).orElseThrow().type());
        assertTrue(alberta.isBusinessDay(d(2026, 4, 6)));
        assertTrue(alberta.isBusinessDay(d(2026, 8, 3)));
        assertTrue(alberta.isBusinessDay(d(2026, 9, 30)));
        assertTrue(Observed.calendar("CA-QC").isBusinessDay(d(2026, 4, 6)));
        assertFalse(Observed.calendar("CA-QC").isBusinessDay(d(2026, 4, 3)));
    }

    @Test
    void remembranceDayIsNotAGeneralHolidayInManitobaAndNovaScotia() {
        assertTrue(Observed.calendar("CA-MB").isBusinessDay(d(2026, 11, 11)));
        assertTrue(Observed.calendar("CA-NS").isBusinessDay(d(2026, 11, 11)));
        assertFalse(Observed.calendar("CA-NB").isBusinessDay(d(2026, 11, 11)));
        assertFalse(Observed.calendar("CA-ON").isHoliday(d(2026, 11, 11)));
    }

    @Test
    void newfoundlandAndLabradorKeepsMemorialDayOn1July() {
        assertEquals("Memorial Day", Observed.calendar("CA-NL").holidayOn(d(2026, 7, 1)).orElseThrow().name());
    }

    @Test
    void truthAndReconciliationDayStartsWhenEachLawAddedIt() {
        assertFalse(Observed.calendar("CA-NT").isHoliday(d(2021, 9, 30)));
        assertTrue(Observed.calendar("CA-NT").isHoliday(d(2022, 9, 30)));
        assertTrue(Observed.calendar("CA-NU").isHoliday(d(2022, 9, 30)));
        assertTrue(Observed.calendar("CA-PE").isHoliday(d(2022, 9, 30)));
        assertFalse(Observed.calendar("CA-BC").isHoliday(d(2022, 9, 30)));
        assertTrue(Observed.calendar("CA-BC").isHoliday(d(2023, 9, 30)));
        assertFalse(Observed.calendar("CA-YT").isHoliday(d(2022, 9, 30)));
        assertEquals("Orange Shirt Day", Observed.calendar("CA-MB").holidayOn(d(2023, 9, 30)).orElseThrow().name());
        assertFalse(Observed.calendar("CA-ON").isHoliday(d(2026, 9, 30)));
    }

    @Test
    void februaryHolidaysStartInTheirFirstYear() {
        assertFalse(Observed.calendar("CA-NB").isHoliday(d(2017, 2, 20)));
        assertTrue(Observed.calendar("CA-NB").isHoliday(d(2018, 2, 19)));
        assertFalse(Observed.calendar("CA-NS").isHoliday(d(2014, 2, 17)));
        assertTrue(Observed.calendar("CA-NS").isHoliday(d(2015, 2, 16)));
        assertFalse(Observed.calendar("CA-ON").isHoliday(d(2007, 2, 19)));
        assertTrue(Observed.calendar("CA-ON").isHoliday(d(2008, 2, 18)));
    }

    @Test
    void britishColumbiaFamilyDayMovedFromTheSecondToTheThirdMondayIn2019() {
        var britishColumbia = Observed.calendar("CA-BC");

        assertFalse(britishColumbia.isHoliday(d(2012, 2, 13)));
        assertEquals("Family Day", britishColumbia.holidayOn(d(2013, 2, 11)).orElseThrow().name());
        assertEquals("Family Day", britishColumbia.holidayOn(d(2018, 2, 12)).orElseThrow().name());
        assertFalse(britishColumbia.isHoliday(d(2018, 2, 19)));
        assertEquals("Family Day", britishColumbia.holidayOn(d(2019, 2, 18)).orElseThrow().name());
        assertFalse(britishColumbia.isHoliday(d(2019, 2, 11)));
    }
}
