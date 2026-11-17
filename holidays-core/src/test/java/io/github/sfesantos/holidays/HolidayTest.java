package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

class HolidayTest {

    private static final EasterAlgorithm EASTER = EasterAlgorithm.gregorian();

    @Test
    void defaultsToPublicWithNoValidityBounds() {
        var christmas = Holiday.fixed("Christmas", Month.DECEMBER, 25);

        assertEquals(HolidayType.PUBLIC, christmas.periods().get(0).type());
        assertEquals(YearRange.always(), christmas.periods().get(0).validity());
        assertEquals(Optional.of(LocalDate.of(1900, 12, 25)), christmas.dateIn(Year.of(1900), EASTER));
    }

    @Test
    void validFromExcludesEarlierYears() {
        var holiday = Holiday.fixed("Constitution Day", Month.DECEMBER, 6).validFrom(Year.of(1978));

        assertTrue(holiday.dateIn(Year.of(1977), EASTER).isEmpty());
        assertEquals(Optional.of(LocalDate.of(1978, 12, 6)), holiday.dateIn(Year.of(1978), EASTER));
    }

    @Test
    void validUntilExcludesLaterYears() {
        var holiday = Holiday.fixed("Old Holiday", Month.MARCH, 1).validUntil(Year.of(2000));

        assertEquals(Optional.of(LocalDate.of(2000, 3, 1)), holiday.dateIn(Year.of(2000), EASTER));
        assertTrue(holiday.dateIn(Year.of(2001), EASTER).isEmpty());
    }

    @Test
    void validBetweenKeepsBothBoundsInclusive() {
        var holiday = Holiday.fixed("Temporary", Month.MARCH, 1).validBetween(Year.of(2020), Year.of(2022));

        assertTrue(holiday.dateIn(Year.of(2019), EASTER).isEmpty());
        assertTrue(holiday.dateIn(Year.of(2020), EASTER).isPresent());
        assertTrue(holiday.dateIn(Year.of(2022), EASTER).isPresent());
        assertTrue(holiday.dateIn(Year.of(2023), EASTER).isEmpty());
    }

    @Test
    void validFromAfterValidUntilFailsFast() {
        var holiday = Holiday.fixed("Temporary", Month.MARCH, 1).validUntil(Year.of(2000));

        assertThrows(IllegalArgumentException.class, () -> holiday.validFrom(Year.of(2001)));
    }

    @Test
    void ofTypeReturnsACopy() {
        var original = Holiday.easterRelative("Good Friday", -2);
        var bank = original.ofType(HolidayType.BANK);

        assertEquals(HolidayType.PUBLIC, original.periods().get(0).type());
        assertEquals(HolidayType.BANK, bank.periods().get(0).type());
        assertEquals(original.periods().get(0).rule(), bank.periods().get(0).rule());
    }

    @Test
    void onIsTheDateInItsYearOnly() {
        var royalWedding = Holiday.on("Royal Wedding", LocalDate.of(2011, 4, 29));

        assertEquals(Optional.of(LocalDate.of(2011, 4, 29)), royalWedding.dateIn(Year.of(2011), EASTER));
        assertTrue(royalWedding.dateIn(Year.of(2012), EASTER).isEmpty());
        assertEquals(YearRange.always(), royalWedding.periods().get(0).validity());
    }

    @Test
    void customRuleHonoursValidity() {
        var electionDay = Holiday.custom("Election Day", year -> Optional.of(year.atMonth(Month.NOVEMBER).atDay(1)))
                .validFrom(Year.of(1845));

        assertTrue(electionDay.dateIn(Year.of(1844), EASTER).isEmpty());
        assertEquals(Optional.of(LocalDate.of(1845, 11, 1)), electionDay.dateIn(Year.of(1845), EASTER));
    }

    @Test
    void customRuleOutsideItsYearFailsNamingTheHolidayAndTheDate() {
        var far = Holiday.custom("Far", year -> Optional.of(LocalDate.of(year.getValue() + 3, 6, 1)));

        var thrown = assertThrows(IllegalStateException.class, () -> far.datesIn(Year.of(2026), EASTER));

        assertTrue(thrown.getMessage().contains("'Far'"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("2029-06-01"), thrown.getMessage());
    }

    @Test
    void observanceIsInheritedUnlessOverridden() {
        var christmas = Holiday.fixed("Christmas", Month.DECEMBER, 25);
        var pinned = christmas.withObservance(ObservanceRule.none());

        assertTrue(christmas.periods().get(0).inheritsObservance());
        assertFalse(pinned.periods().get(0).inheritsObservance());
        assertEquals(ObservanceRule.none(), pinned.periods().get(0).observance());
    }

    @Test
    void aRuleThatChangedOverTimeIsOneHolidayWithTwoPeriods() {
        var statehood = Holiday.of("Statehood Day",
                HolidayPeriod.of(HolidayRule.fixed(Month.JUNE, 25)).validUntil(Year.of(2019)),
                HolidayPeriod.of(HolidayRule.fixed(Month.MAY, 30)).validFrom(Year.of(2020)));

        assertEquals(Optional.of(LocalDate.of(2019, 6, 25)), statehood.dateIn(Year.of(2019), EASTER));
        assertEquals(Optional.of(LocalDate.of(2020, 5, 30)), statehood.dateIn(Year.of(2020), EASTER));
        assertEquals(statehood.periods().get(1), statehood.periodIn(Year.of(2031)).orElseThrow());
    }

    @Test
    void aGapBetweenPeriodsHasNoDate() {
        var allSaints = Holiday.of("All Saints", HolidayPeriod.of(HolidayRule.fixed(Month.NOVEMBER, 1)).validUntil(Year.of(2012)))
                .and(HolidayPeriod.of(HolidayRule.fixed(Month.NOVEMBER, 1)).validFrom(Year.of(2016)));

        assertTrue(allSaints.dateIn(Year.of(2012), EASTER).isPresent());
        assertTrue(allSaints.dateIn(Year.of(2013), EASTER).isEmpty());
        assertTrue(allSaints.periodIn(Year.of(2015)).isEmpty());
        assertTrue(allSaints.dateIn(Year.of(2016), EASTER).isPresent());
    }

    @Test
    void onDatesHasOnePeriodPerDate() {
        var matariki = Holiday.onDates("Matariki", LocalDate.of(2022, 6, 24), LocalDate.of(2023, 7, 14));

        assertEquals(2, matariki.periods().size());
        assertEquals(Optional.of(LocalDate.of(2023, 7, 14)), matariki.dateIn(Year.of(2023), EASTER));
        assertTrue(matariki.dateIn(Year.of(2024), EASTER).isEmpty());
    }

    @Test
    void aDatedYearCanSitInsideAGapOfTheRule() {
        var kingsDay = Holiday.of("King's Day",
                HolidayPeriod.of(HolidayRule.fixed(Month.APRIL, 27)).validUntil(Year.of(2024)),
                HolidayPeriod.of(HolidayRule.on(LocalDate.of(2025, 4, 26))),
                HolidayPeriod.of(HolidayRule.fixed(Month.APRIL, 27)).validFrom(Year.of(2026)));

        assertEquals(Optional.of(LocalDate.of(2025, 4, 26)), kingsDay.dateIn(Year.of(2025), EASTER));
        assertEquals(Optional.of(LocalDate.of(2026, 4, 27)), kingsDay.dateIn(Year.of(2026), EASTER));
    }

    @Test
    void overlappingPeriodsAreRejectedNamingTheYears() {
        var untilTwenty = HolidayPeriod.of(HolidayRule.fixed(Month.JUNE, 25)).validUntil(Year.of(2020));
        var fromTwenty = HolidayPeriod.of(HolidayRule.fixed(Month.MAY, 30)).validFrom(Year.of(2020));
        var always = HolidayPeriod.of(HolidayRule.fixed(Month.MAY, 30));
        var bounded = HolidayPeriod.of(HolidayRule.fixed(Month.MAY, 30)).validBetween(Year.of(2001), Year.of(2005));

        var ranges = assertThrows(IllegalArgumentException.class, () -> Holiday.of("Statehood Day", untilTwenty, fromTwenty));
        var open = assertThrows(IllegalArgumentException.class, () -> Holiday.of("Statehood Day", bounded).and(always));

        assertEquals("holiday 'Statehood Day' has overlapping periods: until 2020 and from 2020", ranges.getMessage());
        assertEquals("holiday 'Statehood Day' has overlapping periods: 2001 to 2005 and every year", open.getMessage());
    }

    @Test
    void twoDatesInOneYearOverlap() {
        var thrown = assertThrows(IllegalArgumentException.class,
                () -> Holiday.onDates("Matariki", LocalDate.of(2022, 6, 24), LocalDate.of(2022, 7, 1)));
        var againstARule = assertThrows(IllegalArgumentException.class, () -> Holiday.fixed("King's Day", Month.APRIL, 27)
                .and(HolidayPeriod.of(HolidayRule.on(LocalDate.of(2025, 4, 26)).plusDays(1))));

        assertEquals("holiday 'Matariki' has overlapping periods: 2022 and 2022", thrown.getMessage());
        assertEquals("holiday 'King's Day' has overlapping periods: every year and 2025", againstARule.getMessage());
    }

    @Test
    void typeObservanceAndClosingTimeApplyToEveryPeriod() {
        var matariki = Holiday.onDates("Matariki", LocalDate.of(2022, 6, 24), LocalDate.of(2023, 7, 14));

        var bank = matariki.ofType(HolidayType.BANK);
        var pinned = matariki.withObservance(ObservanceRule.none());
        var half = matariki.closingAt(LocalTime.of(13, 0));

        assertEquals(List.of(HolidayType.BANK, HolidayType.BANK), bank.periods().stream().map(HolidayPeriod::type).toList());
        assertTrue(pinned.periods().stream().noneMatch(HolidayPeriod::inheritsObservance));
        assertEquals(List.of(HolidayType.HALF_DAY, HolidayType.HALF_DAY), half.periods().stream().map(HolidayPeriod::type).toList());
        assertEquals(LocalTime.of(13, 0), half.periods().get(1).closesAt());
    }

    @Test
    void yearsAreSetPerPeriodOnceThereAreSeveral() {
        var matariki = Holiday.onDates("Matariki", LocalDate.of(2022, 6, 24), LocalDate.of(2023, 7, 14));

        assertThrows(IllegalStateException.class, () -> matariki.validFrom(Year.of(2022)));
        assertThrows(IllegalStateException.class, () -> matariki.validUntil(Year.of(2052)));
        assertThrows(IllegalStateException.class, () -> matariki.validBetween(Year.of(2022), Year.of(2052)));
    }

    @Test
    void needsAtLeastOnePeriod() {
        assertThrows(IllegalArgumentException.class, () -> Holiday.of("Empty"));
        assertThrows(IllegalArgumentException.class, () -> Holiday.onDates("Empty"));
        assertThrows(NullPointerException.class, () -> Holiday.fixed("X", Month.MARCH, 1).and(null));
    }

    @Test
    void periodsCannotBeChangedThroughTheList() {
        var christmas = Holiday.fixed("Christmas", Month.DECEMBER, 25);

        assertThrows(UnsupportedOperationException.class, () -> christmas.periods().clear());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Holiday.fixed("  ", Month.MARCH, 1));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> Holiday.of(null, HolidayRule.fixed(Month.MARCH, 1)));
        assertThrows(NullPointerException.class, () -> Holiday.of("X", (HolidayRule) null));
    }
}
