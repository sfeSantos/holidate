package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

class HolidayTest {

    private static final EasterAlgorithm EASTER = EasterAlgorithm.gregorian();

    @Test
    void defaultsToPublicWithNoValidityBounds() {
        var christmas = Holiday.fixed("Christmas", Month.DECEMBER, 25);

        assertEquals(HolidayType.PUBLIC, christmas.type());
        assertEquals(YearRange.always(), christmas.validity());
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

        assertEquals(HolidayType.PUBLIC, original.type());
        assertEquals(HolidayType.BANK, bank.type());
        assertEquals(original.rule(), bank.rule());
    }

    @Test
    void customRuleHonoursValidity() {
        var electionDay = Holiday.custom("Election Day", year -> Optional.of(year.atMonth(Month.NOVEMBER).atDay(1)))
                .validFrom(Year.of(1845));

        assertTrue(electionDay.dateIn(Year.of(1844), EASTER).isEmpty());
        assertEquals(Optional.of(LocalDate.of(1845, 11, 1)), electionDay.dateIn(Year.of(1845), EASTER));
    }

    @Test
    void observanceIsInheritedUnlessOverridden() {
        var christmas = Holiday.fixed("Christmas", Month.DECEMBER, 25);
        var pinned = christmas.withObservance(ObservanceRule.none());

        assertTrue(christmas.inheritsObservance());
        assertFalse(pinned.inheritsObservance());
        assertEquals(ObservanceRule.none(), pinned.observance());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Holiday.fixed("  ", Month.MARCH, 1));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> Holiday.of(null, HolidayRule.fixed(Month.MARCH, 1)));
        assertThrows(NullPointerException.class, () -> Holiday.of("X", null));
    }
}
