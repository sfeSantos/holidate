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

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

class HolidayPeriodTest {

    private static final EasterAlgorithm EASTER = EasterAlgorithm.gregorian();
    private static final HolidayRule MAY_DAY = HolidayRule.fixed(Month.MAY, 1);

    @Test
    void defaultsToPublicEveryYearInheritingObservance() {
        var period = HolidayPeriod.of(MAY_DAY);

        assertEquals(HolidayType.PUBLIC, period.type());
        assertEquals(YearRange.always(), period.validity());
        assertEquals(YearRange.always(), period.years());
        assertTrue(period.inheritsObservance());
        assertEquals(LocalTime.NOON, period.closesAt());
    }

    @Test
    void modifiersReturnCopies() {
        var period = HolidayPeriod.of(MAY_DAY);

        assertEquals(HolidayType.BANK, period.ofType(HolidayType.BANK).type());
        assertEquals(HolidayType.HALF_DAY, period.closingAt(LocalTime.of(14, 0)).type());
        assertEquals(LocalTime.of(14, 0), period.closingAt(LocalTime.of(14, 0)).closesAt());
        assertFalse(period.withObservance(ObservanceRule.none()).inheritsObservance());
        assertEquals(YearRange.between(Year.of(2000), Year.of(2010)), period.validBetween(Year.of(2000), Year.of(2010)).validity());
        assertEquals(YearRange.between(Year.of(2000), Year.of(2010)), period.validFrom(Year.of(2000)).validUntil(Year.of(2010)).validity());
        assertEquals(HolidayType.PUBLIC, period.type());
    }

    @Test
    void datesStopAtTheValidity() {
        var period = HolidayPeriod.of(MAY_DAY).validUntil(Year.of(2012));

        assertEquals(List.of(LocalDate.of(2012, 5, 1)), period.datesIn(Year.of(2012), EASTER));
        assertEquals(List.of(), period.datesIn(Year.of(2013), EASTER));
    }

    @Test
    void aSingleDateCoversItsYearOnly() {
        var date = HolidayRule.on(LocalDate.of(2025, 4, 26));
        var year = YearRange.between(Year.of(2025), Year.of(2025));

        assertEquals(year, HolidayPeriod.of(date).years());
        assertEquals(year, HolidayPeriod.of(date.plusDays(2)).years());
        assertEquals(year, HolidayPeriod.of(date).validFrom(Year.of(2020)).years());
    }

    @Test
    void aSingleDateOutsideItsValidityKeepsTheValidity() {
        var period = HolidayPeriod.of(HolidayRule.on(LocalDate.of(2025, 4, 26))).validUntil(Year.of(2020));

        assertEquals(YearRange.until(Year.of(2020)), period.years());
        assertEquals(List.of(), period.datesIn(Year.of(2025), EASTER));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> HolidayPeriod.of(null));
        assertThrows(NullPointerException.class, () -> HolidayPeriod.of(MAY_DAY).ofType(null));
        assertThrows(NullPointerException.class, () -> HolidayPeriod.of(MAY_DAY).withObservance(null));
        assertThrows(NullPointerException.class, () -> HolidayPeriod.of(MAY_DAY).closingAt(null));
        assertThrows(NullPointerException.class, () -> HolidayPeriod.of(MAY_DAY).datesIn(null, EASTER));
    }
}
