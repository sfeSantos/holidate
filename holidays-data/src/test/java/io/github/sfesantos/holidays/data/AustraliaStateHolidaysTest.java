package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Australia's public holidays as each state and territory publishes or
 * legislates them: the Holidays Act 1958 (ACT), the NSW Government list
 * under the Public Holidays Act 2010, the Northern Territory Government
 * list, the Queensland Government list under the Holidays Act 1983, the
 * SafeWork SA list under the Public Holidays Act 2023, the Statutory
 * Holidays Act 2000 (Tasmania, section 4), the Public Holidays Act 1993
 * (Victoria, section 6) and the Western Australian Government list. Where a
 * list starts at 2026 the 2025 dates are derived from the same rules. The
 * national layer holds only the days every state and territory observes.
 *
 * <p>When New Year's Day, Christmas Day or Boxing Day falls on a weekend,
 * the acts keep it there and add a public holiday on the next Monday or
 * Tuesday, so both dates are in these lists. Tasmania does that for
 * Christmas Day only and moves the other two. Australia Day moves to the
 * Monday everywhere. Anzac Day stays on the weekend with no extra day in
 * most states; Western Australia adds the Monday, and the ACT, the Northern
 * Territory and Queensland move a Sunday to the Monday. NSW declared an
 * additional day for Anzac Day in 2026 and 2027 only, and the ACT declared
 * Sunday 25 April 2027 a holiday on top of its Monday (NI2026-387). The
 * pack holds the rules in force now: the years before a state changed from
 * moving to adding are not modelled.
 *
 * <p>The evening part-day holidays of the Northern Territory, Queensland
 * and South Australia are {@code HALF_DAY}. Regional and local days are
 * left out, and so is the bank holiday of NSW and the ACT. Victoria
 * declares the Friday before the AFL Grand Final each year: 2025 and 2026
 * are in the pack, 2027 had no date when this was written.
 */
class AustraliaStateHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 4, 18), d(2025, 4, 21), d(2025, 4, 25), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.datesIn("AU", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 4, 3), d(2026, 4, 6), d(2026, 4, 25), d(2026, 12, 25),
                d(2026, 12, 26), d(2026, 12, 28)),
                Observed.datesIn("AU", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 26), d(2027, 3, 29), d(2027, 4, 25), d(2027, 12, 25),
                d(2027, 12, 26), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU", 2027));
    }

    @Test
    void australianCapitalTerritory() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 3, 10), d(2025, 4, 18), d(2025, 4, 19), d(2025, 4, 20),
                d(2025, 4, 21), d(2025, 4, 25), d(2025, 6, 2), d(2025, 6, 9), d(2025, 10, 6), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.datesIn("AU-ACT", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 3, 9), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 5),
                d(2026, 4, 6), d(2026, 4, 25), d(2026, 6, 1), d(2026, 6, 8), d(2026, 10, 5), d(2026, 12, 25),
                d(2026, 12, 26), d(2026, 12, 28)),
                Observed.datesIn("AU-ACT", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 8), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 28),
                d(2027, 3, 29), d(2027, 4, 25), d(2027, 4, 26), d(2027, 5, 31), d(2027, 6, 14), d(2027, 10, 4),
                d(2027, 12, 25), d(2027, 12, 26), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU-ACT", 2027));
    }

    @Test
    void newSouthWales() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 4, 18), d(2025, 4, 19), d(2025, 4, 20), d(2025, 4, 21),
                d(2025, 4, 25), d(2025, 6, 9), d(2025, 10, 6), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("AU-NSW", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 5), d(2026, 4, 6),
                d(2026, 4, 25), d(2026, 4, 27), d(2026, 6, 8), d(2026, 10, 5), d(2026, 12, 25), d(2026, 12, 26),
                d(2026, 12, 28)),
                Observed.datesIn("AU-NSW", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 28), d(2027, 3, 29),
                d(2027, 4, 25), d(2027, 4, 26), d(2027, 6, 14), d(2027, 10, 4), d(2027, 12, 25), d(2027, 12, 26),
                d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU-NSW", 2027));
    }

    @Test
    void northernTerritory() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 4, 18), d(2025, 4, 19), d(2025, 4, 20), d(2025, 4, 21),
                d(2025, 4, 25), d(2025, 5, 5), d(2025, 6, 9), d(2025, 8, 4), d(2025, 12, 24), d(2025, 12, 25),
                d(2025, 12, 26), d(2025, 12, 31)),
                Observed.datesIn("AU-NT", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 5), d(2026, 4, 6),
                d(2026, 4, 25), d(2026, 5, 4), d(2026, 6, 8), d(2026, 8, 3), d(2026, 12, 24), d(2026, 12, 25),
                d(2026, 12, 26), d(2026, 12, 28), d(2026, 12, 31)),
                Observed.datesIn("AU-NT", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 28), d(2027, 3, 29),
                d(2027, 4, 26), d(2027, 5, 3), d(2027, 6, 14), d(2027, 8, 2), d(2027, 12, 24), d(2027, 12, 25),
                d(2027, 12, 26), d(2027, 12, 27), d(2027, 12, 28), d(2027, 12, 31)),
                Observed.datesIn("AU-NT", 2027));
    }

    @Test
    void queensland() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 4, 18), d(2025, 4, 19), d(2025, 4, 20), d(2025, 4, 21),
                d(2025, 4, 25), d(2025, 5, 5), d(2025, 10, 6), d(2025, 12, 24), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("AU-QLD", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 5), d(2026, 4, 6),
                d(2026, 4, 25), d(2026, 5, 4), d(2026, 10, 5), d(2026, 12, 24), d(2026, 12, 25), d(2026, 12, 26),
                d(2026, 12, 28)),
                Observed.datesIn("AU-QLD", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 28), d(2027, 3, 29),
                d(2027, 4, 26), d(2027, 5, 3), d(2027, 10, 4), d(2027, 12, 24), d(2027, 12, 25), d(2027, 12, 26),
                d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU-QLD", 2027));
    }

    @Test
    void southAustralia() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 3, 10), d(2025, 4, 18), d(2025, 4, 19), d(2025, 4, 20),
                d(2025, 4, 21), d(2025, 4, 25), d(2025, 6, 9), d(2025, 10, 6), d(2025, 12, 24), d(2025, 12, 25),
                d(2025, 12, 26), d(2025, 12, 31)),
                Observed.datesIn("AU-SA", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 3, 9), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 5),
                d(2026, 4, 6), d(2026, 4, 25), d(2026, 6, 8), d(2026, 10, 5), d(2026, 12, 24), d(2026, 12, 25),
                d(2026, 12, 26), d(2026, 12, 28), d(2026, 12, 31)),
                Observed.datesIn("AU-SA", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 8), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 28),
                d(2027, 3, 29), d(2027, 4, 25), d(2027, 6, 14), d(2027, 10, 4), d(2027, 12, 24), d(2027, 12, 25),
                d(2027, 12, 26), d(2027, 12, 27), d(2027, 12, 28), d(2027, 12, 31)),
                Observed.datesIn("AU-SA", 2027));
    }

    @Test
    void tasmania() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 3, 10), d(2025, 4, 18), d(2025, 4, 21), d(2025, 4, 25),
                d(2025, 6, 9), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("AU-TAS", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 3, 9), d(2026, 4, 3), d(2026, 4, 6), d(2026, 4, 25),
                d(2026, 6, 8), d(2026, 12, 25), d(2026, 12, 28)),
                Observed.datesIn("AU-TAS", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 8), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29),
                d(2027, 4, 25), d(2027, 6, 14), d(2027, 12, 25), d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU-TAS", 2027));
    }

    @Test
    void victoria() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 3, 10), d(2025, 4, 18), d(2025, 4, 19), d(2025, 4, 20),
                d(2025, 4, 21), d(2025, 4, 25), d(2025, 6, 9), d(2025, 9, 26), d(2025, 11, 4), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.datesIn("AU-VIC", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 3, 9), d(2026, 4, 3), d(2026, 4, 4), d(2026, 4, 5),
                d(2026, 4, 6), d(2026, 4, 25), d(2026, 6, 8), d(2026, 9, 25), d(2026, 11, 3), d(2026, 12, 25),
                d(2026, 12, 26), d(2026, 12, 28)),
                Observed.datesIn("AU-VIC", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 8), d(2027, 3, 26), d(2027, 3, 27), d(2027, 3, 28),
                d(2027, 3, 29), d(2027, 4, 25), d(2027, 6, 14), d(2027, 11, 2), d(2027, 12, 25), d(2027, 12, 26),
                d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU-VIC", 2027));
    }

    @Test
    void westernAustralia() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 27), d(2025, 3, 3), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 21),
                d(2025, 4, 25), d(2025, 6, 2), d(2025, 9, 29), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("AU-WA", 2025));
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 26), d(2026, 3, 2), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 6),
                d(2026, 4, 25), d(2026, 4, 27), d(2026, 6, 1), d(2026, 9, 28), d(2026, 12, 25), d(2026, 12, 26),
                d(2026, 12, 28)),
                Observed.datesIn("AU-WA", 2026));
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 26), d(2027, 3, 1), d(2027, 3, 26), d(2027, 3, 28), d(2027, 3, 29),
                d(2027, 4, 25), d(2027, 4, 26), d(2027, 6, 7), d(2027, 9, 27), d(2027, 12, 25), d(2027, 12, 26),
                d(2027, 12, 27), d(2027, 12, 28)),
                Observed.datesIn("AU-WA", 2027));
    }

    @Test
    void australiaDayOnSundayMovesToMonday() {
        var national = Observed.calendar("AU");

        assertFalse(national.isHoliday(d(2025, 1, 26)));
        assertEquals("Australia Day", national.holidayOn(d(2025, 1, 27)).orElseThrow().name());
    }

    @Test
    void anzacDayOnSaturdayAddsAMondayOnlyInWesternAustralia() {
        assertEquals("Anzac Day", Observed.calendar("AU-WA").holidayOn(d(2026, 4, 25)).orElseThrow().name());
        assertEquals("Anzac Day", Observed.calendar("AU-WA").holidayOn(d(2026, 4, 27)).orElseThrow().name());

        for (var state : List.of("AU-ACT", "AU-NT", "AU-QLD", "AU-SA", "AU-TAS", "AU-VIC")) {
            assertTrue(Observed.calendar(state).isHoliday(d(2026, 4, 25)), state);
            assertTrue(Observed.calendar(state).isBusinessDay(d(2026, 4, 27)), state);
        }
    }

    @Test
    void anzacDayOnSundayGivesAMondayInFourJurisdictions() {
        for (var state : List.of("AU-ACT", "AU-NT", "AU-QLD", "AU-WA")) {
            assertEquals("Anzac Day", Observed.calendar(state).holidayOn(d(2027, 4, 26)).orElseThrow().name(), state);
            assertEquals("Anzac Day", Observed.calendar(state).holidayOn(d(2032, 4, 26)).orElseThrow().name(), state);
        }

        for (var state : List.of("AU-NT", "AU-QLD")) {
            assertFalse(Observed.calendar(state).isHoliday(d(2027, 4, 25)), state);
        }

        assertTrue(Observed.calendar("AU-WA").isHoliday(d(2032, 4, 25)));

        for (var state : List.of("AU-SA", "AU-TAS", "AU-VIC")) {
            assertTrue(Observed.calendar(state).isHoliday(d(2027, 4, 25)), state);
            assertTrue(Observed.calendar(state).isBusinessDay(d(2027, 4, 26)), state);
        }
    }

    @Test
    void newSouthWalesAddsADayForAnzacDayIn2026And2027Only() {
        var newSouthWales = Observed.calendar("AU-NSW");

        assertTrue(newSouthWales.isHoliday(d(2026, 4, 25)));
        assertFalse(newSouthWales.isBusinessDay(d(2026, 4, 27)));
        assertTrue(newSouthWales.isHoliday(d(2027, 4, 25)));
        assertFalse(newSouthWales.isBusinessDay(d(2027, 4, 26)));
        assertTrue(newSouthWales.isHoliday(d(2032, 4, 25)));
        assertTrue(newSouthWales.isBusinessDay(d(2032, 4, 26)));
    }

    @Test
    void theCapitalTerritoryKeepsAnzacDayOnSundayIn2027Only() {
        var capitalTerritory = Observed.calendar("AU-ACT");

        assertTrue(capitalTerritory.isHoliday(d(2027, 4, 25)));
        assertFalse(capitalTerritory.isHoliday(d(2021, 4, 25)));
        assertFalse(capitalTerritory.isHoliday(d(2032, 4, 25)));
    }

    @Test
    void christmasOnSaturdayStaysAndGivesMondayAndTuesday() {
        var queensland = Observed.calendar("AU-QLD");
        var southAustralia = Observed.calendar("AU-SA");

        assertEquals("Christmas Day", queensland.holidayOn(d(2027, 12, 25)).orElseThrow().name());
        assertEquals("Boxing Day", queensland.holidayOn(d(2027, 12, 26)).orElseThrow().name());
        assertEquals("Christmas Day", queensland.holidayOn(d(2027, 12, 27)).orElseThrow().name());
        assertEquals("Boxing Day", queensland.holidayOn(d(2027, 12, 28)).orElseThrow().name());
        assertEquals("Proclamation Day", southAustralia.holidayOn(d(2027, 12, 26)).orElseThrow().name());
        assertEquals("Proclamation Day", southAustralia.holidayOn(d(2027, 12, 28)).orElseThrow().name());
        assertTrue(queensland.isBusinessDay(d(2027, 12, 29)));
    }

    @Test
    void newSouthWalesKeepsChristmasDayOnSaturdayAndAddsTheMonday() {
        var newSouthWales = Observed.calendar("AU-NSW");
        var added = newSouthWales.holidayOn(d(2027, 12, 27)).orElseThrow();

        assertTrue(newSouthWales.isHoliday(d(2027, 12, 25)));
        assertEquals("Christmas Day", added.name());
        assertEquals(d(2027, 12, 25), added.original());
        assertFalse(newSouthWales.isBusinessDay(d(2027, 12, 27)));
    }

    @Test
    void christmasOnSundayAddsTheTuesdayAfterBoxingDay() {
        var newSouthWales = Observed.calendar("AU-NSW");

        assertEquals("Christmas Day", newSouthWales.holidayOn(d(2033, 12, 25)).orElseThrow().name());
        assertEquals("Boxing Day", newSouthWales.holidayOn(d(2033, 12, 26)).orElseThrow().name());
        assertEquals("Christmas Day", newSouthWales.holidayOn(d(2033, 12, 27)).orElseThrow().name());
        assertTrue(newSouthWales.isBusinessDay(d(2033, 12, 28)));
    }

    @Test
    void newYearOnSaturdayStaysAndGivesMonday() {
        var victoria = Observed.calendar("AU-VIC");

        assertEquals("New Year's Day", victoria.holidayOn(d(2028, 1, 1)).orElseThrow().name());
        assertEquals("New Year's Day", victoria.holidayOn(d(2028, 1, 3)).orElseThrow().name());
    }

    @Test
    void tasmaniaKeepsOnlyChristmasDayOnTheWeekend() {
        var tasmania = Observed.calendar("AU-TAS");

        assertTrue(tasmania.isHoliday(d(2027, 12, 25)));
        assertFalse(tasmania.isHoliday(d(2027, 12, 26)));
        assertEquals("Christmas Day", tasmania.holidayOn(d(2027, 12, 27)).orElseThrow().name());
        assertEquals("Boxing Day", tasmania.holidayOn(d(2027, 12, 28)).orElseThrow().name());
        assertFalse(tasmania.isHoliday(d(2026, 12, 26)));
        assertFalse(tasmania.isHoliday(d(2028, 1, 1)));
        assertEquals("New Year's Day", tasmania.holidayOn(d(2028, 1, 3)).orElseThrow().name());
    }

    @Test
    void eveningPartDaysStayBusinessDays() {
        var northernTerritory = Observed.calendar("AU-NT");
        var queensland = Observed.calendar("AU-QLD");
        var southAustralia = Observed.calendar("AU-SA");

        assertEquals(HolidayType.HALF_DAY, northernTerritory.holidayOn(d(2026, 12, 31)).orElseThrow().type());
        assertEquals(LocalTime.of(19, 0), northernTerritory.holidayOn(d(2026, 12, 24)).orElseThrow().closesAt());
        assertEquals(LocalTime.of(18, 0), queensland.holidayOn(d(2026, 12, 24)).orElseThrow().closesAt());
        assertEquals(LocalTime.of(19, 0), southAustralia.holidayOn(d(2026, 12, 31)).orElseThrow().closesAt());
        assertTrue(southAustralia.isBusinessDay(d(2026, 12, 24)));
        assertFalse(queensland.isHoliday(d(2026, 12, 31)));
        assertFalse(queensland.isHoliday(d(2018, 12, 24)));
    }

    @Test
    void kingsBirthdayDiffersByState() {
        assertEquals("King's Birthday", Observed.calendar("AU-NSW").holidayOn(d(2026, 6, 8)).orElseThrow().name());
        assertEquals("King's Birthday", Observed.calendar("AU-QLD").holidayOn(d(2026, 10, 5)).orElseThrow().name());
        assertEquals("King's Birthday", Observed.calendar("AU-WA").holidayOn(d(2026, 9, 28)).orElseThrow().name());
        assertEquals("Western Australia Day", Observed.calendar("AU-WA").holidayOn(d(2026, 6, 1)).orElseThrow().name());
        assertEquals("Labour Day", Observed.calendar("AU-QLD").holidayOn(d(2026, 5, 4)).orElseThrow().name());
    }

    @Test
    void victoriaHasTheGrandFinalFridayOnlyInDeclaredYears() {
        var victoria = Observed.calendar("AU-VIC");

        assertTrue(victoria.isHoliday(d(2025, 9, 26)));
        assertTrue(victoria.isHoliday(d(2026, 9, 25)));
        assertTrue(victoria.isBusinessDay(d(2027, 9, 24)));
        assertTrue(victoria.isBusinessDay(d(2027, 10, 1)));
        assertEquals("Melbourne Cup Day", victoria.holidayOn(d(2026, 11, 3)).orElseThrow().name());
    }

    @Test
    void reconciliationDayIsTheMondayOnOrAfter27May() {
        var capitalTerritory = Observed.calendar("AU-ACT");

        assertTrue(capitalTerritory.isHoliday(d(2030, 5, 27)));
        assertTrue(capitalTerritory.isHoliday(d(2026, 6, 1)));
        assertTrue(capitalTerritory.isHoliday(d(2018, 5, 28)));
        assertFalse(capitalTerritory.isHoliday(d(2017, 5, 29)));
    }

    @Test
    void easterDaysStartWhenEachStateAddedThem() {
        assertFalse(Observed.calendar("AU-TAS").isHoliday(d(2026, 4, 5)));
        assertTrue(Observed.calendar("AU-TAS").isHoliday(d(2027, 3, 28)));
        assertFalse(Observed.calendar("AU-TAS").isHoliday(d(2027, 3, 27)));
        assertFalse(Observed.calendar("AU-WA").isHoliday(d(2027, 3, 27)));
        assertTrue(Observed.calendar("AU-WA").isHoliday(d(2028, 4, 15)));
        assertFalse(Observed.calendar("AU-WA").isHoliday(d(2021, 4, 4)));
        assertTrue(Observed.calendar("AU-WA").isHoliday(d(2022, 4, 17)));
        assertFalse(Observed.calendar("AU-SA").isHoliday(d(2023, 4, 9)));
        assertTrue(Observed.calendar("AU-SA").isHoliday(d(2024, 3, 31)));
        assertFalse(Observed.calendar("AU-QLD").isHoliday(d(2016, 3, 27)));
        assertTrue(Observed.calendar("AU-QLD").isHoliday(d(2017, 4, 16)));
    }

    /**
     * The Western Australian Government's "Changes to WA's public holidays to commence in 2028" and its
     * published dates for 2028 and 2029. Labour Day is the third Monday of March in a State election year;
     * only 2029 is in the pack, so later election years come out a week early. Show Day is left out until
     * it is proclaimed.
     */
    @Test
    void westernAustraliaMovesThreeHolidaysFrom2028() {
        var westernAustralia = Observed.calendar("AU-WA");

        assertEquals("Labour Day", westernAustralia.holidayOn(d(2027, 3, 1)).orElseThrow().name());
        assertEquals("Labour Day", westernAustralia.holidayOn(d(2028, 3, 13)).orElseThrow().name());
        assertFalse(westernAustralia.isHoliday(d(2028, 3, 6)));
        assertEquals("Labour Day", westernAustralia.holidayOn(d(2029, 3, 19)).orElseThrow().name());
        assertFalse(westernAustralia.isHoliday(d(2029, 3, 12)));
        assertEquals("Labour Day", westernAustralia.holidayOn(d(2030, 3, 11)).orElseThrow().name());
        assertEquals("Western Australia Day", westernAustralia.holidayOn(d(2027, 6, 7)).orElseThrow().name());
        assertEquals("Western Australia Day", westernAustralia.holidayOn(d(2028, 11, 13)).orElseThrow().name());
        assertEquals("Western Australia Day", westernAustralia.holidayOn(d(2029, 11, 12)).orElseThrow().name());
        assertFalse(westernAustralia.isHoliday(d(2028, 6, 5)));
        assertEquals("King's Birthday", westernAustralia.holidayOn(d(2027, 9, 27)).orElseThrow().name());
        assertEquals("King's Birthday", westernAustralia.holidayOn(d(2028, 6, 12)).orElseThrow().name());
        assertEquals("King's Birthday", westernAustralia.holidayOn(d(2029, 6, 11)).orElseThrow().name());
        assertFalse(westernAustralia.isHoliday(d(2028, 9, 25)));
    }
}
